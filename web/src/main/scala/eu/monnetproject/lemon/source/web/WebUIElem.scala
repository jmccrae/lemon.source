/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.lemon.source.web

import eu.monnetproject.lang.Language
import eu.monnetproject.lemon.source.app._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.sindice._
import eu.monnetproject.util._
import java.net.URLEncoder
import java.io.File
import java.util.Random
import scala.xml._

/**
 * 
 * @author John McCrae
 */
class WebUIElem(val app : WebLemonEditor) extends UIElems[WebComponent,WebContainer] {
  import UIElems._
  val log = Logging.getLogger(this)
  
  val random = new Random()
  def randId() = "n" + scala.math.abs(random.nextLong)
  
  
  def withEnabled(elem : Elem,enabled : Boolean) : Elem = if(enabled) {
    elem
  } else {
    elem % Attribute("disabled",Text("disabled"),Null)
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Containers
  
  def frame()(components : WebComponent*) = {
    val id = randId()
    val tag = <div class="panel" id={id}/>
    new WebContainer(id,components.toList,tag)
  }
  
  def tabbedFrame(selected : String)(components : ((String,LocalizableString),WebComponent)*) = {
    val id = randId()
    val script : String = ("$(function() {\n" ++=
                           "  $('#" ++= id ++= "').tabs();\n" ++=
                           "});") 
    val comps = for((_,comp) <- components) yield { 
      comp 
    }
    val selectedComp = components.find(_._1._1 == selected).getOrElse(throw new RuntimeException())
    new WebContainer(id,
                     comps.toList,
                     null) {
      override def html : Node = {
        <span>
          <div class="tabbedFrame ui-tabs ui-widget ui-widget-content ui-corner-all" id={id}>
            {
              <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">{(for(comp <- components) yield {
                      if(comp._1._1 == selected) {
                        <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a href={"?action="+comp._1._1}>{l10n(comp._1._2)}</a></li>
                      } else {
                        <li class="ui-state-default ui-corner-top"><a href={"?action="+comp._1._1}>{l10n(comp._1._2)}</a></li>
                      }
                    })}</ul> +:
              {
                <div class="ui-tabs-panel ui-widget-content ui-corner-bottom">{selectedComp._2.html}</div>
              }
            }
          </div>
        </span>
      }
    }
  }
  
  def oldTabbedFrame()(components : (LocalizableString,WebComponent)*) = {
    val id = randId()
    val script : String = ("$(function() {\n" ++=
                           "  $('#" ++= id ++= "').tabs();\n" ++=
                           "});") 
    var i = 0 
    val comps = for((_,comp) <- components) yield { 
      comp 
    }
    new WebContainer(id,
                     comps.toList,
                     null) {
      override def html : Node = {
        <span>
          <script type="text/javascript">{script}</script>
          <div class="tabbedFrame" id={id}>
            {
              <ul>{(for(comp <- components) yield {
                      i += 1
                      <li><a href={"#"+id+"-tab"+i}>{l10n(comp._1)}</a></li>
                    })}</ul> +:
              {
                i = 0
                for(comp <- components) yield {
                  i+=1
                  <div id={id+"-tab"+i}>{comp._2.html}</div>
                }
              }
            }
          </div>
        </span>
      }
    }
  }
  
  def vertical(spacing : Boolean = true, widthPercent : Int = -1, style : Option[String] = None, id2 : Option[String] = None)(components : WebComponent*) = {
    val id = id2 match {
      case Some(i) => i
      case None => randId()
    }
    val classes = List("vertical") ++ (if(spacing) { Some("spacing") } else { None }) ++ style
    
    new WebContainer(id,components.toList,
                     <div id={id} class={classes.mkString(" ")}/>,
                     { i => Some(<div/>) })
  }
  
  def horizontal(spacing : Boolean = true,style : Option[String] = None)(components : WebComponent*) = {
    val id = randId()
    val classes = List("horizontal") ++ (if(spacing) { Some("spacing") } else { None }) ++ style
    new WebContainer(id,components.toList,
                     <div id={id} class={classes.mkString(" ")}/>)
  }
  
  def horizontal100(spacing : Boolean = true,style : Option[String] = None)(components : WebComponent*) = {
    val id = randId()
    val classes = List("horizontal100") ++ (if(spacing) { Some("spacing") } else { None }) ++ style
    new WebContainer(id,components.toList,
                     <table id={id} class={classes.mkString(" ")} width="90%"><tr></tr></table>,
                     { i => Some(<td/>) }
    )
  }
  
  def views(components : UIPanel[_,WebComponent,WebContainer]*) = {
    val id = randId()
    val comps = for(comp <- components) yield { 
      comp.initialize 
      // register the view id
      app.viewMap += (comp -> comp.panel.id)
      log.info(comp.panel.html.toString)
      comp.panel 
    }
    new WebContainer(id,
                     comps.toList,
                     <div/>)
  }
  
  def border(spacing : Boolean = true, style : Option[String] = None)(_components : Tuple2[BorderLocation,WebComponent]*) = {
    val id = randId()
    val classes = List("border") ++ (if(spacing) { Some("spacing") } else { None }) ++ style
    val components = _components.toMap
    val top = components.get(BorderLocation.TOP) match {
      case Some(top) => Some(<div class="border_top">{top.html}</div>)
      case None => None
    }
    val left = components.get(BorderLocation.LEFT) match {
      case Some(left) => Some(<span class="border_left">{left.html}</span>)
      case None => None
    } 
    val middle = components.get(BorderLocation.MIDDLE) match {
      case Some(middle) => Some(<span class="border_middle">{middle.html}</span>)
      case None => None
    }
    val right = components.get(BorderLocation.RIGHT) match {
      case Some(right) => Some(<div class="border_right">{right.html}</div>)
      case None => None
    }
    val bottom = components.get(BorderLocation.BOTTOM) match {
      case Some(bottom) => Some(<div class="border_bottom">{bottom.html}</div>)
      case None => None
    }
    val left_middle = if(left != None || middle != None) {
      Some(<span class="border_left_middle">{(left ++ middle)}</span>)
    } else {
      None
    }
    val x : NodeSeq = (top.toList :+
                       (<div width="100%">{(right ++ left_middle)}</div>)) ++ bottom
    val tag = <div id={id} class={classes.mkString(" ")}>{x}</div>
    new WebContainer(id,Nil,tag)
  }
  
  def columns(style : Option[String] = None, rightAlignLast : Boolean = false)(_components : Seq[Tuple2[Int,WebComponent]]) : WebContainer = {
    val id = randId()
    val tag = <table width="100%" style="margin:-10px;"><tr>{
          if(rightAlignLast) {
            (for((width,comp) <- _components.init) yield {
                <td width={width+"%"}>{comp.html}</td>
              }) :+ 
            <td width={_components.last._1+"%"} style="text-align:right;">{_components.last._2.html}</td>
          } else {
            (for((width,comp) <- _components) yield {
                <td width={width+"%"}>{comp.html}</td>
              })
          }
        }</tr></table>
    new WebContainer(id,Nil,tag)
  }
  
  ////////////////////////////////////////////////////////////////////////////
  // Dialogs
  
  def dialog[E](caption : LocalizableString, elem : FormElem[E], widthPx : Int = 400)(action : E => Result) = {
    val id = randId()
    val tag = <form id={id} class="dialog" width={widthPx+"px"} title={l10n(caption)}> {
        elem.components.map(_.html) :+
        <button class="dialog_ok lint_ignore" onclick={"doClose('"+id+"');doSubmit('"+id+"');return false"}>{l10n("OK")}</button> :+
        <button class="dialog_cancel" onclick={"doClose('"+id+"');return false"}>{l10n("Cancel")}</button>
      }</form>
      
    app.navigator.addSubmitHandler(id,elem.asInstanceOf[FormElemExtractor[E]],action)
    new LoadDialogResult(l10n(caption),id,tag,widthPx)
  }
  
  def cleanName(s : LocalizableString) = URLEncoder.encode(s.key.replaceAll("\\s",""),"UTF-8")
  
  def compoundDialog[E](caption : LocalizableString, widthPx : Int = 600)(panes : (LocalizableString,FormElem[E])*)(action : E => Result) = {
    val id = randId()
    assert(!panes.isEmpty)
    val tag = <div id={id} width={widthPx+"px"} title={l10n(caption)}>
      <div class="compound_button_box">{
          <button class="compound_button compound_button_down" id={id+cleanName(panes.head._1)} onclick={"call('"+id+cleanName(panes.head._1)+"');return false;"}>{l10n(panes.head._1)}</button> :+
          (for((paneName,_) <- panes.tail) yield {
              <button class="compound_button" id={id+cleanName(paneName)} onclick={"call('"+id+cleanName(paneName)+"');return false;"}>{l10n(paneName)}</button>
            })
        }</div>
      <div>
        <form id={id+"_form"}>
          <div id={id+"_main"+cleanName(panes.head._1)}>
            {panes.head._2.components.map(_.html)}
          </div>
          {<span>{for((paneName,paneElem) <- panes.tail) yield {
            <div id={id+"_main"+cleanName(paneName)} class="compound_hidden">{
              paneElem.components.map(_.html)
              }</div>
            }
          }</span>}
          <button class="dialog_ok lint_ignore" onclick={"doSubmit('"+id+"_form');doClose('"+id+"');return false;"}>{l10n("OK")}</button> 
          <button class="dialog_cancel" onclick={"doClose('"+id+"');return false;"}>{l10n("Cancel")}</button>
        </form>
      </div>
              </div>
      
    for((paneName,elem) <- panes) {
      app.navigator.addHandler(id+cleanName(paneName), None, {
          var result : Result = RemoveComponentStyle(id+"_main"+cleanName(paneName),"compound_hidden") 
          for((paneName2,_) <- panes if paneName != paneName2) {
            result = result >> RemoveComponentStyle(id+cleanName(paneName2),"compound_button_down") >>
              AddComponentStyle(id+"_main"+cleanName(paneName2),"compound_hidden")
          }
          result = result >> AddComponentStyle(id+cleanName(paneName),"compound_button_down")
          result
        })
    }
    
    app.navigator.addSubmitHandler(id+"_form",new FormElemExtractor[E] {
        def extract(elems : Map[String,String]) : Option[E]= {
          for((paneName,elem) <- panes) {
            log.info(paneName.key)
            if(elem.asInstanceOf[FormElemExtractor[E]].extract(elems) != None) {
              return elem.asInstanceOf[FormElemExtractor[E]].extract(elems) 
            }
          }
          return None
        }
      },action)   
    new LoadDialogResult(l10n(caption),id,tag,widthPx)
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Components 
  
  def html(html : String) = {
    val id = randId()
    val tag = <span>{XML.loadString(html)}</span>
    new SimpleComponent(id,tag)
  }
  
  def spacer(widthPx : Int = -1, heightPx : Int = -1) = {
    val id = randId()
    val tag = if(widthPx >= 0) {
      if(heightPx >= 0) {
        <span style={"padding-left:"+widthPx+"px;padding-top:"+heightPx+"px;"}/>
      } else {
        <span style={"padding-left:"+widthPx+"px;"}/>
      }
    } else {
      if(heightPx >= 0) {
        <span style={"padding-top:"+heightPx+"px;"}/>
      } else {
        <span/>
      }
    }
    new SimpleComponent(id,tag)
  }
  
  def button(caption : LocalizableString = "", enabled : Boolean = true, 
             contextHelp : LocalizableString = "", description : LocalizableString = "",
             style : Option[String] = None)(onclick : => Result) = {
    val id = randId()
    val classes = List("button") ++ style
    val tag = withEnabled(<button id={id} class={classes.mkString(" ")} title={l10n(description)}  onclick={"call('"+id+"')"}>{
        l10n(caption)
      }</button>,enabled)
    app.navigator.addHandler(id, None, onclick)
    new SimpleComponent(id,tag)
  }
  
  def pageButtons(leftEnabled : Boolean = false, rightEnabled : Boolean = true, 
                  style : Option[String] = None)(leftAction : () => Result,
                                                 rightAction : () => Result,
                                                 status : () => (Boolean,Boolean)) = {
    val id = randId()
    val classes = List("pageButtons") ++ style
    val leftClasses = List("pageButton_left") ++ style
    val rightClasses = List("pageButton_right") ++ style
    
    val tag = <span id={id} class={classes.mkString(" ")}>
      {withEnabled(<button id={id+"-left"} class={leftClasses.mkString(" ")} onclick={"callAction('"+id+"','left')"}>&lt;&lt;</button>,leftEnabled)}
      {withEnabled(<button id={id+"-right"} class={rightClasses.mkString(" ")} onclick={"callAction('"+id+"','right')"}>&gt;&gt;</button>,rightEnabled)}
              </span>
    
    def combFunction(action : () => Result)() = {
      val leftResult = action()
      val newStatus = status()
      leftResult >> new EnableComponent(id +"-left",newStatus._1) >>
      new EnableComponent(id+"-right",newStatus._2)
    }
    app.navigator.addHandler(id, Some("left"), combFunction(leftAction))
    app.navigator.addHandler(id, Some("right"), combFunction(rightAction))
    new SimpleComponent(id,tag)
  }
  
  def imageButton(fileName : String, enabled : Boolean = true, contextHelp : LocalizableString = "",description : LocalizableString = "")(onclick : => Result) = {
    val id = randId()
    val classes = List("imageButton")
    val tag = withEnabled(<img src={SourceWebServlet.path +"/images/"+fileName} alt={l10n(description)} onclick={"call('"+id+"')"} class={classes.mkString(" ")}/>,enabled)
    app.navigator.addHandler(id, None, onclick)
    new SimpleComponent(id,tag)
  }
  
  def imageButtonWithLink(fileName : String, link : String, enabled : Boolean = true, contextHelp : LocalizableString = "")(onclick : => Result) = {
    val id = randId()
    val classes = List("imageButton")
    val tag = withEnabled(<a href={link}><img src={SourceWebServlet.path +"/images/"+fileName} class={classes.mkString(" ")}/></a>,enabled)
    new SimpleComponent(id,tag)
  }
  
  def popupButton(caption : LocalizableString, popup : () => List[WebComponent],
                  enabled : Boolean = true, 
                  contextHelp : LocalizableString = "", description : LocalizableString = "",
                  style : Option[String] = None)(onclick : => Result) = {
    val id = randId()
    val classes = List("popupButton") ++ style
    val tag = withEnabled(<span>
                          <button id={id} class={classes.mkString(" ")} onclick={"showPopup('"+id+"-popup')"} title={l10n(description)}>
        {l10n(caption)}
                          </button>
                          <ul class="popup" id={id+"-popup"}>{
          for(comp <- popup()) yield {
            <li onclick={"hidePopup('"+id+"-popup')"}>{comp.html}</li>
          }
        }</ul>
                          </span>  
                          ,enabled)
    new SimpleComponent(id,tag)
  }
   
  def label(caption : LocalizableString = "", style : Option[String] = None, widthPx : Int = -1) = {
    val id = randId()
    val classes = List("label") ++ style
    val tag = <span id={id} class={classes.mkString(" ")}>{l10n(caption)}</span>
    new SimpleComponent(id,tag)
  }
  
  def toggleButton(offCaption : LocalizableString = "", onCaption : LocalizableString = "",
                   enabled : Boolean = true, 
                   contextHelp : LocalizableString = "", description : LocalizableString = "")(toggleFunc : Boolean => Result) = {
    val id = randId()
    val classes = List("toggleButton") 
    val tag = withEnabled(<button id={id} class={classes.mkString(" ")} title={l10n(description)} onclick={"call('"+id+"')"}>{l10n(offCaption)}</button>,enabled)
    
    val elem = new ToggleElem {
      var on = false
      def toggle = NoResult
      def component = new SimpleComponent(id,tag)
      def handler : Result = {
        on = !on
        toggleFunc(on) >> new ChangeContents(id,if(on) { Text(l10n(onCaption))} else { Text(l10n(offCaption)) })
      }
    }
    app.navigator.addHandler(id, None, elem.handler)
    elem
  }
  
  def imageToggleButton(offImage : String, onImage : String,
                        enabled : Boolean = true, 
                        contextHelp : LocalizableString = "", description : LocalizableString = "")(toggleFunc : Boolean => Result) = {
    val id = randId()
    val classes = List("imageToggleButton")
    val tag = withEnabled(<img src={SourceWebServlet.path +"/images/"+offImage} class={classes.mkString(" ")} alt={l10n(description)} onclick={"call('"+id+"')"}/>,enabled)
    val elem = new ToggleElem {
      var on = false
      def toggle = NoResult
      def component = new SimpleComponent(id,tag)
      def handler : Result = {
        on = !on
        toggleFunc(on) >> new ChangeProperty(id,"src",SourceWebServlet.path +"/images/" + (if(on) { onImage } else { offImage }))
      }
    }
    app.navigator.addHandler(id,None,elem.handler)
    elem
  }
   
  def searchField(caption : LocalizableString = "")(onsearch : (String) => Result) = {
    val id = randId()
    val tag = <form onsubmit={"fieldSubmit('"+id+"')"}>
      <input id={id} class="search"/>
              </form>
    app.navigator.addFieldHandler(id, onsearch)
    new SimpleComponent(id,tag)
  }
  
  def tree[E](head : E, treeFunction : E => List[E], namer : E => String) = {
    def buildTree(nodes : List[E]) : Node = nodes match {
      case Nil => <span/>
      case _ => {
          <ul>{
              for(node <- nodes) yield {
                <li>{namer(node)}</li> +:
                buildTree(treeFunction(node))
              }
            }</ul>
        }
    }
    val id = randId()
    val tag = <div id={id}>
      {namer(head)}
      {buildTree(treeFunction(head))}
              </div>
    new SimpleComponent(id,tag)
  }
  
  def immediateField(caption : LocalizableString, default : Option[String] = None)(onselect : => Result)(onchange : (String) => Result)  = {
    val id = randId()
    val tag = <span>{l10n(caption)}
      <input type="text" name={id} id={id} value={default.getOrElse("")} onfocus={"call('"+id+"');"} onchange={"elemCall('"+id+"');"}/>
              </span>
    app.navigator.addHandler(id, None, onselect)
    app.navigator.addFieldHandler(id, onchange)
    new SimpleComponent(id,tag)
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Form elements
  
  private implicit def str2ToAttr(value : (String,String)) = Attribute(value._1,Text(value._2),Null)
  
  def field(caption : LocalizableString = "", default : Option[String] = None, validator : Option[String => Boolean] = None) = {
    val id = randId()
    val tag = <span>{
        Text(l10n(caption)) :+
        <input id={id} class="field" type="text" name={id} value={default.getOrElse("")}/> :+
        <br/>
      }</span>
    new FormElem[String] with DefFEE[String] {
      def components = List(new SimpleComponent(id,tag))
      def fieldId = id
      def parse(str : String) = Option(str)
    }
  }
  
  def textArea(caption : LocalizableString, default : String, rows : Int = 5, cols : Int = 55, update : Option[String => Result] = None) = {
    val id = randId()
    var tag = <textarea  id={id} class="textArea" name={id} rows={rows.toString} cols={cols.toString}>{default}</textarea>
    if(update != None) {
      tag = tag % (("onchange","call('"+id+"')"))
      app.navigator.addFieldHandler(id, update.get)
    }
    new FormElem[String] with DefFEE[String] {
      val compTag = <span>{
          Text(l10n(caption)) :+
          tag :+
          <br/>
        }</span>
      def components = List(new SimpleComponent(id,compTag))
      def fieldId = id
      def parse(str : String) = Option(str)
    }
  }
  
  private def cleanOption(opt : String) = URLEncoder.encode(opt,"UTF-8")
  
  def select[E](caption : LocalizableString, defaultValue : Option[E] = None, descriptions : E => String = {e:E => ""})(values : (E,String)*)(onselect : (E) => Result) = {
    val id = randId()
    val tag = <select id={id} class="select" name={id} onchange={"elemCall('"+id+"')"}>{
        (for((e,name) <- values) yield {
            if(defaultValue != None && defaultValue.get == e) {
              <option value={cleanOption(name)} selected="selected">{name}</option>
            } else {
              <option value={cleanOption(name)}>{name}</option>
            }
          })
      }</select>
    app.navigator.addFieldHandler(id, s => {
        values find ( _._2 == s) match {
          case Some((e,_)) => {
              if(app.showHelp) {
                val helpTag = <span>{descriptions(e)}</span>
                onselect(e) >> ChangeContents(id+"-help",helpTag)
              } else {
                onselect(e) 
              }
            }
          case None => NoResult
        }
      })
    new FormElem[E] with DefFEE[E] {
      val compTag = <span>{
          Text(l10n(caption)) :+
          tag :+
          <br/> :+
          {
            if(app.showHelp && defaultValue != None) {
              <span><span id={id+"-help"}>{descriptions(defaultValue.get)}</span><br/></span>
            } else {
              <span><span id={id+"-help"}/><br/></span>
            }
          }
        }</span>
      def components = List(new SimpleComponent(id,compTag))
      def fieldId = id
      def parse(str : String) = values find {
        value => cleanOption(value._2) == str
      } match {
        case Some((e,_)) => Some(e)
        case None => None
      }
    }
  }
  
  def imageSelect[E](caption : LocalizableString = "", defaultValue : E, widthEm : Int = 15)(values : (E,String,LocalizableString)*) = {
    val id = randId()
    val tag = <span>
      {l10n(caption)}
      <select id={id} class="select" name={id}>{
          for((e,image,caption) <- values) yield {
            if(defaultValue == e) {
              <option value={cleanName(image)} selected="selected">{l10n(caption)}</option>
            } else {
              <option value={cleanName(image)}>{l10n(caption)}</option>
            }
          } 
        }</select>
              </span>
    new FormElem[E] with DefFEE[E] {
      val compTag = tag
      def components = List(new SimpleComponent(id,tag))
      def fieldId = id
      def parse(str : String) = values find {
        value => cleanName(value._2) == str
      } map {
        value => value._1
      }
    }
  }
    
  
  def multiSelect[E](caption : LocalizableString = "")(values : (E,String)*) = {
    val id = randId()
    var i = 0;
    val tag = <span>
      <input type="text" name={id+"-formelem"} style="visibility:hidden;height:0px;"/>
      <div class="multiselect">{
          for((e,caption) <- values) yield {
            i += 1
            <div onclick={"multiSelectItem('"+id+"',"+i+",'"+cleanOption(caption)+"')"} id={id+"-"+i}>{caption}</div>
          }
        }</div>
              </span>
    new FormElem[Seq[E]] with DefFEE[Seq[E]] {
      val compTag = tag
      def components = List(new SimpleComponent(id,compTag))
      def fieldId = id
      def parse(str : String) = Some(str split "+" flatMap {
          s => values find { x => cleanOption(x._2) == s } map (_._1)
        })
    }
  }
 
  
  def twinCol[E](caption : LocalizableString = "")(values : (E,String)*) = {
    val id = randId()
    var i = 0;
    val tag = <div class="twinselect">
      <div class="twinselect_select" id={id+"-left"}>{
          for((e,caption) <- values) yield {
            i += 1
            <div onclick={"twinSelectItemL('"+id+"',"+i+")"} id={id+"-"+i} value={cleanOption(caption)}>{caption}</div>
          }
        }</div>
      <div class="twinselect_buttons">
        <br/>
        <button onclick={"twinSelectL2R('"+id+"');return false"} disabled="disabled" id={id+"-l2rbutton"}>&gt;&gt;</button>
        <button onclick={"twinSelectR2L('"+id+"');return false"} disabled="disabled" id={id+"-r2lbutton"}>&lt;&lt;</button>
      </div>
      <div class="twinselect_select" id={id+"-right"}>
      </div>
      <input type="text" name={id+"-formelem"} value="" id={id+"-formelem"}/>
              </div>
    new FormElem[Seq[E]] with DefFEE[Seq[E]] {
      def components = List(new SimpleComponent(randId,tag))
      def fieldId = id+"-formelem"
      def parse(str : String) = Some(str split "\\+" flatMap {
          s => values find { x => cleanOption(x._2) == s } map (_._1)
        })
    }
  }
  
  def checkbox(caption : LocalizableString, default : Boolean = false) = {
    val id = randId()
    val compTag = <span><input type="checkbox" name={id} value="true"/>{l10n(caption)}<br/></span>
    new FormElem[Boolean] with FormElemExtractor[Boolean] {      
      def components = List(new SimpleComponent(id,compTag))
      var _value : Option[Boolean] = None
      def value = _value.getOrElse(throw new RuntimeException("Value not set"))
      def extract(params : Map[String,String]) = {
        _value = Some(params.contains(id))
        _value
      }
    }
  }
  
  def langSelect(caption : LocalizableString = "Language", defaultOption : Option[String] = None) = {
    val id = randId()
    val tag = <div>
      {l10n("Language")}<select id={id} class="select" name={id}>{
          (if(defaultOption == None) {
              Nil
            } else {
              List(<option value={defaultOption.get}>{defaultOption.get}</option>)
            }) :+
          (for(lang <- LangFlagMapper.languages.toList.sortBy(_.getName)) yield {
              <option value={lang.getIso639_1}>{lang.getName}</option>
            })
        }</select>
              </div>
    new FormElem[Option[Language]] with DefFEE[Option[Language]] {
      def components = List(new SimpleComponent(randId,tag))
      def fieldId = id
      def parse(str : String) = if(defaultOption != None && defaultOption.get == str) {
        Some(None)
      } else {
        Some(Option(Language.get(str)))
      }
    }
  }
  
  def checkboxes[E](caption : LocalizableString, boxes : Seq[(String,E)])= {
    val id = randId()
    val tag = <div id={id}>
      <div class="checkbox_caption>">{l10n(caption)}</div>
      {
        for((cap,value) <- boxes) yield {
          <span><input type="checkbox" name={cleanOption(cap)} value="yes"></input>{l10n(cap)}<br/></span>
        }
      }
              </div>
    new FormElem[Seq[E]] with FormElemExtractor[Seq[E]] {
      var value : Seq[E] = Nil
      def components = List(new SimpleComponent(id,tag))
      def extract(params : Map[String,String]) = {
        value = boxes filter {
          case (cap,value) => params.contains(cleanOption(cap))
        } map (_._2)
        Some(value)
      }
    }
  }
  
  def tiedSelects[E,F](caption1 : LocalizableString, caption2 : LocalizableString, values : Seq[(E,String)], 
                       subvalues : (E) => Seq[(F,String)], default : Option[(E,F)] = None,
                       description : Option[(E,F) => (String,String)] = None) = {
    val (defE,defF) = default match {
      case Some((e,f)) => (e,f)
      case None => (values(0)._1,subvalues(values(0)._1)(0)._1)
    }
    val firstId = randId()
    val secondId = randId()
    def secondSelect(e : E) = 
      for((f,s) <- subvalues(e)) yield {
        if(f == defF) {
          <option selected="selected">{s}</option>
        } else {
          <option>{s}</option>
        }
      }
    val (defEDesc, defFDesc) = description match {
      case Some(descer) => descer(defE,defF)
      case None => ("","")
    }
    
    app.navigator.addFieldHandler(firstId, s => {
        values find ( _._2 == s) match {
          case Some((e,s)) => {
              var result : Result = ChangeContents(secondId,secondSelect(e)) 
              if(app.showHelp) {
                if(description != None) {
                  val helpUpdateTag = <span>{description.get.apply(e,defF)._1}</span>
                  result = result >> ChangeContents(firstId+"-help",helpUpdateTag)
                }
              }
              result
            }
          case None => NoResult
        }
      })
    app.navigator.addFieldHandler(secondId, s=> {
        if(app.showHelp && description != None) {
          (values flatMap {
              value => {
                subvalues(value._1) find {
                  _._2 == s
                }
              }
            }).headOption match {
            case Some((f,_)) => {
                val helpUpdateTag = <span>{description.get.apply(defE,f)._2}</span>
                log.info(helpUpdateTag.toString)
                ChangeContents(secondId+"-help",helpUpdateTag)
              }
            case None => log.warning("Could not find " + s) ; NoResult
          }
        } else {
          OKResult
        }
      })
    new FormElem[F] with FormElemExtractor[F] {
      val compTag = <div>
        {l10n(caption1)}<select id={firstId} class="select" name={firstId} onchange={"elemCall('"+firstId+"');"}>{
            for((e,s) <- values) yield {
              if(e == defE) {
                <option selected="selected">{s}</option>
              } else {
                <option>{s}</option>
              }
            }
          }</select><br/>
        <span id={firstId+"-help"}>{if(app.showHelp) { defEDesc } else { "" }}</span><br/>
        {l10n(caption2)}<select id={secondId} class="select" name={secondId} onchange={"elemCall('"+secondId+"');"}> {secondSelect(defE)
          }
                        </select><br/>
        <span id={secondId+"-help"}>{if(app.showHelp) { defFDesc } else { "" }}</span>
                    </div>
      def components = List(new SimpleComponent(firstId,compTag))
      var _value : Option[F] = None
      def value = _value.get
      def extract(params : Map[String,String]) = {
        params.get(firstId) match {
          case Some(eval) => {
              values find ( _._2 == eval) match {
                case Some((e,_)) => {
                    params.get(secondId) match {
                      case Some(fval) => {
                          subvalues(e) find ( _._2 == fval) match {
                            case Some((f,_)) => _value = Some(f) ; Some(f)
                            case None => None
                          }
                        }
                      case None => None
                    }
                  }
                case None => None
              }
            }
          case None => None
        }
      }
    }
  }
  
  private class UploadFEE[E](id : String, tag : Node) extends FormElem[E] with FormElemExtractor[E] {
    def components = List(new SimpleComponent(id,tag))
    var value = null.asInstanceOf[E]
    def extract(params : Map[String,String]) = {
      Option(value)
    }
  }
  
  def upload[E](caption : LocalizableString, handler : (File) => E) : FormElem[E] = {
    val id = randId()
    val tag = <span>
      <iframe name={id+"-submit"} id={id+"-submit"} style="display:none;"></iframe>
      <form method="POST" action={SourceWebServlet.path + "/upload"} enctype="multipart/form-data" target={id+"-submit"}>
        <input type="file" name={id}/>
        <input type="submit" value={l10n("Upload")}/>
      </form>
              </span>
    val formelem = new UploadFEE[E](id,tag)
    app.navigator.addUploadHandler(id,file => {
        formelem.value = handler(file)
      })
    formelem
  }
  
  def autoSuggest[E](caption : LocalizableString, suggester : String => Seq[E], serializer : Serializer[E], defaultValue : Option[E] = None) : FormElem[E] = {
    val id = randId()
    val tag = <span>{l10n(caption)}:
      <input type="text" name={id} id={id} onkeyup={"elemCall('"+id+"');"}/>
      <input type="text" name={id+"-uri"} id={id+"-uri"} style="display:none;"/>
      <div class="autosuggest_area" id ={id+"-area"}></div>
              </span>
    app.navigator.addFieldHandler(id, query => {
        if(query.length >= 3) {
          val newContents = for(suggestion <- suggester(query).take(20)) yield {
            <div onclick={"$('#"+id+"').val('"+serializer.display(suggestion)+"');$('#"+id+"-uri').val('"+serializer.toString(suggestion)+"');"}>{serializer.display(suggestion)}</div>
          }
          val newTag = <span>{newContents}</span>
          ChangeContents(id+"-area",newTag)
        } else {
          OKResult
        }
      })
    new FormElem[E] with FormElemExtractor[E] {
      var value : E = null.asInstanceOf[E]
      def components = List(new SimpleComponent(id,tag))
      def extract(params : Map[String,String]) = params.get(id+"-uri") match {
        case Some(value1) => if(value1 != null && value1 != "") {
          Some(serializer.fromString(value1))
        } else {
          params.get(id) match {
           case Some(value) => suggester(value).headOption match {
              case Some(e) => Some(e)
              case None => None
            }
            case None => None
          }
        }
        case None => None
      }
    }
  }
  
  def oneOfForm[E](elems : (LocalizableString,FormElem[E])*) : FormElem[E] = {
    val id = randId()
    val ids = (for(elem <- elems) yield { elem._2 -> randId() }).toMap
    val tag = <span>{
      for((caption,elem) <- elems) yield {
        <input type="radio" name={id} onclick={(elems filter (_ != (caption,elem)) map { 
           e => "$('#"+ids(e._2)+"_frame').addClass('disabled_area')"}).mkString(";") + ";$('#"+ids(elem)+"_frame').removeClass('disabled_area');"}/> :+
        {l10n(caption)} :+
        <br/> :+
        <div id={ids(elem)+"_frame"} class="disabled_area">{
          for(ec <- elem.components) yield {
            ec.html
          }
        }</div>
      }
    }</span>
    new FormElem[E] with FormElemExtractor[E] {
      var value : E = null.asInstanceOf[E]
      def components = List(new SimpleComponent(id,tag))
      def extract(params : Map[String,String]) = None
    }
  }
  
  def multipleElems[E](elem : => FormElem[E]) : FormElem[Seq[E]] = {
     val id = randId()
     val tag = <span id={id}>
        <span id={id+"_landing"}/>
        <br/>
        <button onclick={"callAction('"+id+"','add');return false;"}>+</button>
        <button onclick={"callAction('"+id+"','remove');return false;"}>-</button>
     </span>
     def addHandler = AppendContents(id+"_landing", for(ec <- elem.components) yield {
       ec.html
     })
     app.navigator.addHandler(id,Some("add"),addHandler)
     new FormElem[Seq[E]] with FormElemExtractor[Seq[E]] {
       var value : Seq[E] = Nil
       def components = List(new SimpleComponent(id,tag))
       def extract(params : Map[String,String]) = None
     }
  }
  
  def formElems[E,F](elem1 : FormElem[E], elem2 : FormElem[F]) = new FormElem[(E,F)] with FormElemExtractor[(E,F)] {
    var _value : Option[(E,F)] = None
    val compTag = <span>{
        elem1.components.map(_.html) ++ elem2.components.map(_.html)
      }</span>
    def components = List(new SimpleComponent(randId(),compTag))
    def value = _value.getOrElse(throw new RuntimeException("value not set"))
    def extract(params : Map[String,String]) = elem1.asInstanceOf[FormElemExtractor[E]].extract(params) match {
      case Some(e) => elem2.asInstanceOf[FormElemExtractor[F]].extract(params) match {
          case Some(f) => Some((e,f))
          case None => None
        }
      case None => None
    }
  }
  
  def formElems[E,F,G](elem1 : FormElem[E], elem2 : FormElem[F], elem3 : FormElem[G]) = new FormElem[(E,F,G)] with FormElemExtractor[(E,F,G)] {
    var _value : Option[(E,F,G)] = None
    val compTag = <span>{
        elem1.components.map(_.html) ++ 
        elem2.components.map(_.html) ++ 
        elem3.components.map(_.html)
      }</span>
    def components = List(new SimpleComponent(randId(),compTag))
    def value = _value.getOrElse(throw new RuntimeException("value not set"))
    def extract(params : Map[String,String]) = elem1.asInstanceOf[FormElemExtractor[E]].extract(params) match {
      case Some(e) => elem2.asInstanceOf[FormElemExtractor[F]].extract(params) match {
          case Some(f) => elem3.asInstanceOf[FormElemExtractor[G]].extract(params) match {
              case Some(g) => Some((e,f,g))
              case None => None
            }
          case None => None
        }
      case None => None
    }
  }
  
  
  def formElems[E,F,G,H](elem1 : FormElem[E], elem2 : FormElem[F], elem3 : FormElem[G], elem4 : FormElem[H]) = new FormElem[(E,F,G,H)] with FormElemExtractor[(E,F,G,H)] {
    var _value : Option[(E,F,G,H)] = None
    val compTag = <span>{
        elem1.components.map(_.html) ++ 
        elem2.components.map(_.html) ++ 
        elem3.components.map(_.html) ++ 
        elem4.components.map(_.html)
      }</span>
    def components = List(new SimpleComponent(randId(),compTag))
    def value = _value.getOrElse(throw new RuntimeException("value not set"))
    def extract(params : Map[String,String]) = elem1.asInstanceOf[FormElemExtractor[E]].extract(params) match {
      case Some(e) => elem2.asInstanceOf[FormElemExtractor[F]].extract(params) match {
          case Some(f) => elem3.asInstanceOf[FormElemExtractor[G]].extract(params) match {
              case Some(g) => elem4.asInstanceOf[FormElemExtractor[H]].extract(params) match {
                  case Some(h) => Some((e,f,g,h))
                  case None => log.warning("Failed to understand 4") ; None
                }
              case None => log.warning("Failed to understand 3") ; None
            }
          case None => log.warning("Failed to understand 2") ; None
        }
      case None => log.warning("Failed to understand 1") ; None
    }
  }
 
  
  def mapForm[E,F](elem : FormElem[E], function : E => F) : FormElem[F] = new FormElem[F] with FormElemExtractor[F] {
    assert(elem.isInstanceOf[FormElemExtractor[E]])
    def value = function(elem.value)
    def components = elem.components
    override def validate = elem.validate && function(elem.value) != null
    def extract(params : Map[String,String]) = elem.asInstanceOf[FormElemExtractor[E]].extract(params) match {
      case Some(e) => Some(function(e))
      case None => None
    }
  }
  
  def mapForm[E,F,G](elem : FormElem[E], elem2 : FormElem[F], function : (E,F) => G) = new FormElem[G] with FormElemExtractor[G] {
    assert(elem.isInstanceOf[FormElemExtractor[E]])
    assert(elem2.isInstanceOf[FormElemExtractor[F]])
    def value = function(elem.value, elem2.value)
    def components = elem.components ++ elem2.components
    override def validate = elem.validate && elem2.validate && function(elem.value,elem2.value) != null
    def extract(params : Map[String,String]) = elem.asInstanceOf[FormElemExtractor[E]].extract(params) match {
      case Some(e) => elem2.asInstanceOf[FormElemExtractor[F]].extract(params) match {
        case Some(e2) => Some(function(e,e2))
        case None => None
      }
      case None => None
    }
  }
  
  def mapForm[E,F,G,H](elem : FormElem[E], elem2: FormElem[F], elem3 : FormElem[G], function : (E,F,G) => H)  = new FormElem[H] with FormElemExtractor[H] {
    assert(elem.isInstanceOf[FormElemExtractor[E]])
    assert(elem2.isInstanceOf[FormElemExtractor[F]])
    assert(elem3.isInstanceOf[FormElemExtractor[G]])
    def value = function(elem.value, elem2.value, elem3.value)
    def components = elem.components ++ elem2.components ++ elem3.components
    override def validate = elem.validate && elem2.validate && elem3.validate && function(elem.value,elem2.value,elem3.value) != null
    def extract(params : Map[String,String]) = elem.asInstanceOf[FormElemExtractor[E]].extract(params) match {
      case Some(e) => elem2.asInstanceOf[FormElemExtractor[F]].extract(params) match {
        case Some(e2) => elem2.asInstanceOf[FormElemExtractor[G]].extract(params) match {
          case Some(e3) => Some(function(e,e2,e3))
          case None => None
        }
        case None => None
      }
      case None => None
    }
  }
  
  lazy val _prompt = new Prompt(this)
  def prompt() = _prompt
  
/////////////////////////////////////////////////////////////////////////////
// Special elements for reference select dialog
  
  def sindiceSearch = {
    
    val id = randId()
    val tag = <div id={id}>
      <div>
        {l10n("Search")}<input id={id+"_field"} type="text" class="search"/><button onclick={"elemCall('"+id+"_field');return false;"} id={id+"_button"}>{l10n("Go")}</button>
      </div>
      <form id={id+"_results"}>
      </form>
              </div>
    val searchState = new {
      var page = 0
      def doSearch(query : String) = {
        page += 1
        SindiceSearch.search(query,page) match {
          case Some(results) => {
              val tag = for(result <- results) yield {
                <span><input type="radio" name={id+"_result"} value={result.toString}></input><a href={result.toString}>{result.toString}</a><br/></span>
              }
              AppendContents(id+"_results",tag)
            }
          case None => {
              EnableComponent(id+"_button",false)
            }
        }
      }
    }
    app.navigator.addFieldHandler(id+"_field",searchState.doSearch)
    new FormElem[String] with FormElemExtractor[String] {
      def components = List(new SimpleComponent(randId,tag))
      var value = null
      def extract(params : Map[String,String]) = {
        params.get(id+"_result")
      }
    }
  }
  
  def propVal = {
    val id = randId()
    val tag = 
      <div>
        {l10n("Property")}<input type="text" value="prop"/><br/>
        {l10n("Property value")}<input type="text" value="propval"/>
      </div>
    new FormElem[String] with FormElemExtractor[String] {
      def components = List(new SimpleComponent(id,tag))
      def value = null.asInstanceOf[String]
      def extract(params : Map[String,String]) = {
        val prop = params.get("prop")
        val propVal = params.get("propVal")
        if(prop != None && propVal != None) {
          Some("todo " + prop.get + " " + propVal.get)
        } else {
          None
        }
      }
    }
  }
  
  def oilsScalar = new FormElem[String] with FormElemExtractor[String] {
    val tag = <span/>
    def components = List(new SimpleComponent(randId,tag))
    def value = null.asInstanceOf[String]
    def extract(params : Map[String,String]) = None
  }
  
  def oilsMulti = new FormElem[String] with FormElemExtractor[String] {
    val tag = <span/>
    def components = List(new SimpleComponent(randId,tag))
    def value = null.asInstanceOf[String]
    def extract(params : Map[String,String]) = None
  }
/////////////////////////////////////////////////////////////////////////////
// State-dependent calls (use with caution!)
  
  def setVisible(component : WebComponent, visible : Boolean) = if(visible) {
    new RemoveComponentStyle(component.id,"hidden")
  } else {
    new AddComponentStyle(component.id,"hidden")
  }
  
  def setEnabled(component : WebComponent, enabled : Boolean) = new EnableComponent(component.id,enabled)
  
  def addToContainer(container : WebContainer, component : WebComponent) = {
    container add component
    AppendContents(container.id,component.html)
  }
  
  def removeFromContainer(container : WebContainer, component : WebComponent) = {
    container remove component
    RemoveElement(component.id)
  }
  
  def replaceInContainer(container : WebContainer, oldComponent : WebComponent, newComponent : WebComponent) = {
    container update (oldComponent, newComponent)
    ChangeContents(oldComponent.id,newComponent.html)
  }
  
  def clearContainer(container : WebContainer) = {
    container.clear
    NoResult
  }
  
  def animate(component : WebComponent, animation : Animation) = {
    NoResult
  }
}
