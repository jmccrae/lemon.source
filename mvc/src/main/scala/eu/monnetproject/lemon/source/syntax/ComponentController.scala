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
package eu.monnetproject.lemon.source.syntax

import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.basic.EntrySelectWindow
import java.net.URI
import scala.collection.JavaConversions._

/**
 * Controller for components
 * @author John McCrae
 */
class ComponentController[C,O<:C](state : State, entry : LexicalEntry, uielems : UIElems[C,O])
extends StdController[List[Component],C,O](new ComponentModel(entry), new ComponentView(state,uielems))

class ComponentModel(entry : LexicalEntry) extends StdModel[List[Component]] {
  def add(comps : List[Component]) = {
    entry.addDecomposition(comps)
  }
  
  def remove(comps : List[Component]) = {
    entry.removeDecomposition(comps)
  }
  
  def elements = (for(comps <- entry.getDecompositions()) yield {
      comps.toList
    }).toSeq
}

class ComponentView[C,O<:C](state : State, val uielems : UIElems[C,O])
extends HasState(state) with StdPanel[List[Component],C,O] {
  import UIElems._
  import uielems._

  val title = "Decompositions"
  val addText = "Add decompositions"
  val removeText = ""
  val description = "The decomposition is the set of words that constitute a multi-word phrase or compound word."
  
  protected def makeElem(compList : List[Component],
                         remove : () => Result,
                         update : List[Component] => Result,
                         change : List[Component] => Unit) = {
    val vals = (for(comp <- compList) yield {
        app.namer.displayName(comp.getElement())
      })
    new PopupColElement(vals,remove,uielems) {
      def components(value : String, idx : Int) = { 
        val comp = compList(vals.indexOf(value))
        comp.getElement() match {
          case entry : LexicalEntry => {
              (button(LocalizableString("Go to ", List(value))) {
                  app.navigator.entry(entry, lexicon)
                }) ::
              (button("Remove") {
                  val newComponent = compList.take(idx-1) ++ compList.drop(idx)
                  update(newComponent)
                }) ::
              (if(idx == 0) {
                  (button("Add") {
                      val newComp = lemonFactory.makeComponent(app.namer.name(entry,"component"))
                      newComp.setElement(entry)
                      val newComponents = compList.take(idx) ++ (newComp +: compList.drop(idx))
                      update(newComponents)
                    }) ::
                  (button("Modify") {
                      new EntrySelectWindow(uielems,state,model) show {
                        entry => {
                          val newComp = lemonFactory.makeComponent(app.namer.name(entry,"component"))
                          newComp.setElement(entry)
                          val newComponents = compList.take(idx) ++ (newComp +: compList.drop(idx+1))
                          update(newComponents)
                        }
                      }
                    }) :: Nil
                } else {
                  (button("Modify") {
                      new EntrySelectWindow(uielems,state,model) show {
                        entry => {
                          val newComp = lemonFactory.makeComponent(app.namer.name(entry,"component"))
                          newComp.setElement(entry)
                          val newComponents = compList.take(idx) ++ (newComp +: compList.drop(idx+1))
                          update(newComponents)
                        }
                      }
                    }) :: Nil
                })
            }
          case _ => Nil
        }
      }
    }
  }
  
  protected def makeDisplay(comps : List[Component]) = {
    var tag = new StringBuilder("<li>")
    for(comp <- comps) {
      val le = comp.getElement() 
      tag ++= "<a href=\"" + le.getURI()+"\">" + app.namer.displayName(le) + "</a>&#160;&#160;"
    }
    tag ++= "</li>"
    html(tag.toString)
  }
  
  private lazy val componentWindow = new ComponentEditorWindow(state,lexicon,uielems)
  
  def dialog(comps : Option[List[Component]], after : List[Component] => Result) = {
    componentWindow.show(comps,after)
  }

}
//class ComponentController(val view : UIView[List[LemonComponent]] with State, entry : LexicalEntry) 
//    extends HasState(view) with GenericController[List[LemonComponent]] {
//  
//  val title = "Decompositions"
//  val addText = "Add decompositions"
//  val removeText = ""
//  
//  def unwrap(components : List[LemonComponent]) = components map { 
//    component => {
//      component.getElement() match {
//        case arg : Argument => "Arg " + (if(entry.getURI() != null) { 
//            "<" + entry.getURI() + ">"
//          } else {
//            entry.getID().toString()
//          })
//        case entry : LexicalEntry => app.namer.displayName(entry)
//        case _ => "##ERR##"
//      }
//    }
//  }
//  
//  def wrap(vals : List[String]) : Option[List[LemonComponent]] = vals match {
//    case Nil => None
//    case _ => Some(vals map { value =>
//      val entry = lemonFactory.makeLexicalEntry(URI.create(value))
//      val component = lemonFactory.makeComponent()
//      component.setElement(entry)
//      component
//    })
//  }
//  
//  def load = {
//    entry.getDecompositions foreach {
//      decomp => view.add(decomp.toList)
//    }
//  }
//  
//  def add(comp : List[LemonComponent]) = {
//    entry.addDecomposition(comp)
//    view add comp
//  }
//  
//  def addAt(comp : List[LemonComponent], idx : Int, entry : LexicalEntry) = {
//    if(idx < 0 || idx >= comp.size) {
//      throw new IllegalArgumentException()
//    }
//    entry.removeDecomposition(comp)
//    val newComp = lemonFactory.makeComponent()
//    newComp setElement entry
//    val newComps = comp.take(idx) ::: (newComp :: comp.drop(idx))
//    entry.addDecomposition(newComps)
//    view.update(comp,newComps)
//  }
//  
//  def change(comp : List[LemonComponent], idx : Int, entry : LexicalEntry) = {
//    comp(idx).setElement(entry)
//    view.update(comp,comp)
//  } 
//  
//  def remove(comp : List[LemonComponent]) = {
//    entry.removeDecomposition(comp)
//    view remove comp
//  }
//  
//  def setDecomp(comps : List[LemonComponent]) {
//    for(decomps <- entry.getDecompositions) {
//      entry.removeDecomposition(decomps)
//    }
//    entry.addDecomposition(comps)
//  }
//}
