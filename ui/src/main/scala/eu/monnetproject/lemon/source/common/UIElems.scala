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
package eu.monnetproject.lemon.source.common

import eu.monnetproject.lang.Language
import java.io.File
import scala.collection.mutable.StringBuilder
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.LemonEditorApp

/**
 * 
 * @author John McCrae
 */
case class LocalizableString(key : String, args : List[AnyRef])

object UIElems {
  implicit def localize(key : String) = LocalizableString(key,Nil)
  def toBuilder(x : String) = new StringBuilder(x)
  
  implicit def makeSB(base : String) = new {
    def ++=(next : String) = {
      val sb = new StringBuilder(base)
      sb ++= next
    }
  }
  implicit def sbToStr(builder : StringBuilder) : String = builder.toString
}

trait UIElems[Component,Container<:Component] {
  import UIElems._
  def app : LemonEditorApp
  
  def nolocalize(key : String) = LocalizableString(null,List(key))
  
  /////////////////////////////////////////////////////////////////////////////
  // Containers
  
  def frame()(components : Component*) : Container 
  
  def tabbedFrame(selected : String)(components : ((String,LocalizableString),Component)*) : Container 
  
  def vertical(spacing : Boolean = true, widthPercent : Int = -1, style : Option[String] = None, id : Option[String] = None)(components : Component*) : Container
  
  def horizontal(spacing : Boolean = true,style : Option[String] = None)(components : Component*) : Container 
  
  def horizontal100(spacing : Boolean = true,style : Option[String] = None)(components : Component*) : Container 
  
  def views(components : UIPanel[_,Component,Container]*) : Container 
    
  def border(spacing : Boolean = true, style : Option[String] = None)(_components : Tuple2[BorderLocation,Component]*) : Container 
  
  def columns(style : Option[String] = None, rightAlignLast : Boolean = false)(_components : Seq[Tuple2[Int,Component]]) : Container
  
  ////////////////////////////////////////////////////////////////////////////
  // Dialogs
  
  def dialog[E](caption : LocalizableString, elem : FormElem[E], widthPx : Int = 400)(action : E => Result) : Result 
  
  def compoundDialog[E](caption : LocalizableString, widthPx : Int = 600)(panes : (LocalizableString,FormElem[E])*)(action : E => Result) : Result 
  
  /////////////////////////////////////////////////////////////////////////////
  // Components 
  
  def html(html : String) : Component
  
  def spacer(widthPx : Int = -1, heightPx : Int = -1) : Component 
  
  def button(caption : LocalizableString = "", enabled : Boolean = true, 
             contextHelp : LocalizableString = "", description : LocalizableString = "",
             style : Option[String] = None)(onclick : => Result) : Component 
  
  def pageButtons(leftEnabled : Boolean = false, rightEnabled : Boolean = true, 
                  style : Option[String] = None)(leftAction : () => Result,
                                                 rightAction : () => Result,
                                                 status : () => (Boolean,Boolean)) : Component
  
  def imageButton(fileName : String, enabled : Boolean = true, contextHelp : LocalizableString = "",description : LocalizableString = "")(onclick : => Result) : Component 
  
  def imageButtonWithLink(fileName : String, link : String, enabled : Boolean = true, contextHelp : LocalizableString = "")(onclick : => Result) : Component
  
  def popupButton(caption : LocalizableString, popup : () => List[Component],
                  enabled : Boolean = true, 
                  contextHelp : LocalizableString = "", description : LocalizableString = "",
                  style : Option[String] = None)(onclick : => Result) : Component
   
  def label(caption : LocalizableString = "", style : Option[String] = None, widthPx : Int = -1) : Component
  
  def toggleButton(offCaption : LocalizableString = "", onCaption : LocalizableString = "",
                   enabled : Boolean = true, 
                   contextHelp : LocalizableString = "", description : LocalizableString = "")(toggle : Boolean => Result) : ToggleElem
  
  def imageToggleButton(offImage : String, onImage : String,
                        enabled : Boolean = true, 
                        contextHelp : LocalizableString = "", description : LocalizableString = "")(toggle : Boolean => Result) : ToggleElem 
   
  def searchField(caption : LocalizableString = "")(onsearch : (String) => Result) : Component
  
  def tree[E](head : E, treeFunction : E => List[E], namer : E => String) : Component 
  
  def immediateField(caption : LocalizableString, default : Option[String] = None)(onselect : => Result)(onchange : (String) => Result) : Component 
  
  /////////////////////////////////////////////////////////////////////////////
  // Form elements
  
  def field(caption : LocalizableString = "", default : Option[String] = None, validator : Option[String => Boolean] = None) : FormElem[String] 
  
  def textArea(caption : LocalizableString, contents : String = "", rows : Int = 5, cols : Int = 20, update : Option[String => Result] = None) : FormElem[String] 
  
  def select[E](caption : LocalizableString, defaultValue : Option[E] = None, descriptions : E => String = {e:E => ""})(values : (E,String)*)(onselect : (E) => Result) : FormElem[E] 
  
  def imageSelect[E](caption : LocalizableString = "", defaultValue : E, widthEm : Int = 15)(values : (E,String,LocalizableString)*) : FormElem[E] 
  
  def multiSelect[E](caption : LocalizableString = "")(values : (E,String)*) : FormElem[Seq[E]] 
  
  def twinCol[E](caption : LocalizableString = "")(values : (E,String)*) : FormElem[Seq[E]]
  
  def checkbox(caption : LocalizableString, default : Boolean = false) : FormElem[Boolean]
  
  def langSelect(caption : LocalizableString = "Language", defaultOption : Option[String] = None) : FormElem[Option[Language]] 
  
  def checkboxes[E](caption : LocalizableString, boxes : Seq[(String,E)]) : FormElem[Seq[E]] 
  
  def tiedSelects[E,F](caption1 : LocalizableString, caption2 : LocalizableString, values : Seq[(E,String)], 
                       subvalues : (E) => Seq[(F,String)], default : Option[(E,F)] = None, 
                       description : Option[(E,F) => (String,String)] = None) : FormElem[F]
  
  def upload[E](caption : LocalizableString, handler : (File) => E) : FormElem[E]
  
  def autoSuggest[E](caption : LocalizableString, suggester : String => Seq[E], serializer : Serializer[E], defaultValue : Option[E] = None) : FormElem[E]
  
  def oneOfForm[E](elems : (LocalizableString,FormElem[E])*) : FormElem[E]
  
  def multipleElems[E](elem : => FormElem[E]) : FormElem[Seq[E]]
  
  def formElems[E,F](elem1 : FormElem[E], elem2 : FormElem[F]) : FormElem[(E,F)] 
  
  def formElems[E,F,G](elem1 : FormElem[E], elem2 : FormElem[F], elem3 : FormElem[G]) : FormElem[(E,F,G)] 
  
  
  def formElems[E,F,G,H](elem1 : FormElem[E], elem2 : FormElem[F], elem3 : FormElem[G], elem4 : FormElem[H]) : FormElem[(E,F,G,H)]
  
  def mapForm[E,F](elem : FormElem[E], function : E => F) : FormElem[F]
  
  def mapForm[E,F,G](elem : FormElem[E], elem2 : FormElem[F], function : (E,F) => G) : FormElem[G]
  
  def mapForm[E,F,G,H](elem : FormElem[E], elem2: FormElem[F], elem3 : FormElem[G], function : (E,F,G) => H) : FormElem[H]
  
  def prompt() : AbstractPrompt
  
  /////////////////////////////////////////////////////////////////////////////
  // Special elements for reference select dialog
  
  def sindiceSearch : FormElem[String] 
  
  def propVal : FormElem[String] 
  
  def oilsScalar : FormElem[String]
  
  def oilsMulti : FormElem[String] 
  
  /////////////////////////////////////////////////////////////////////////////
  // State-dependent calls (use with caution!)
  
  def setVisible(component : Component, visible : Boolean) : Result
  
  def setEnabled(component : Component, enabled : Boolean) : Result
  
  def addToContainer(container : Container, component : Component) : Result
  
  def addElem(container : Container, element : ViewElement[Component,Container]) = {
    addToContainer(container,element.component)
  }
  
  def removeFromContainer(container : Container, component : Component) : Result
  
  def removeElem(container : Container, element : ViewElement[Component,Container]) = {
    removeFromContainer(container,element.component)
  }
  
  def replaceInContainer(container : Container, oldComponent : Component, newComponent : Component) : Result
  
  def replaceElem(container : Container, oldElement : ViewElement[Component,Container], newElement : ViewElement[Component,Container]) = {
    replaceInContainer(container,oldElement.component,newElement.component)
  }
  
  def clearContainer(container : Container) : Result
  
  def animate(component : Component, animation : Animation) : Result
  
 
  /////////////////////////////////////////////////////////////////////////////
  // Types 
  
  
  
  protected def l10n(string : LocalizableString) : String = try { 
    string match {
      case LocalizableString(null, List(arg1)) => arg1.toString
      case LocalizableString(key, Nil) => app.l10n(key)
      case LocalizableString(key, args) => app.l10n(key,args)
    } 
  } catch {
    case x : Exception => System.err.println(x.getMessage) ; string.key
  }
  
  sealed trait BorderLocation
  
  private class BorderLocation2(val loc : String) extends BorderLocation
  
  object BorderLocation  {
    val LEFT : BorderLocation = new BorderLocation2("LEFT")
    val RIGHT : BorderLocation = new BorderLocation2("RIGHT")
    val MIDDLE : BorderLocation = new BorderLocation2("MIDDLE")
    val TOP : BorderLocation = new BorderLocation2("TOP")
    val BOTTOM : BorderLocation = new BorderLocation2("BOTTOM")
  }
  
  sealed trait Animation
  
  private class AnimationImpl(val animType : String) extends Animation
  
  object Animations {
    val ROLL_UP : Animation = new AnimationImpl("UP")
    val ROLL_DOWN : Animation = new AnimationImpl("DOWN")
    val FADE_IN : Animation = new AnimationImpl("FADE_IN")
    val FADE_OUT : Animation = new AnimationImpl("FADE_OUT")
  }
  
  trait FormElem[E] {
    def value : E
    def components : Seq[Component]
    def validate = value != null
  }
  
  trait ToggleElem {
    def toggle : Result
    def component : Component
  }
  
  trait Serializer[E] {
    def display(e : E) : String
    def toString(e : E) : String
    def fromString(str : String) : E
  }
}

