/****************************************************************************
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
 ********************************************************************************/

package eu.monnetproject.lemon.source.common

import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult

/**
 * 
 * @author John McCrae
 */
trait StdPanel[Elem,Component,Container<:Component] {
  val uielems : UIElems[Component,Container]
  import uielems._
  import UIElems._
  
  protected var elems = Map[Elem,StdElement[Component,Container]]()
  
  def select(elem : Elem) : Result = elems.getOrElse(elem,{throw new IllegalArgumentException()}).select
  def deselect(elem : Elem) : Result = elems.getOrElse(elem,{throw new IllegalArgumentException()}).deselect
  def add(elem : Elem, panel : Container, removeAction : () => Result, updateAction : Elem => Result, changeAction : Elem => Unit) : Result = {
    val newElem = makeElem(elem,removeAction,updateAction,changeAction)
    elems += (elem -> newElem)
    uielems.addToContainer(panel, newElem.component)
  }
  
  def remove(elem : Elem, panel : Container) : Result = {
    uielems.removeFromContainer(panel, elems.getOrElse(elem,{throw new IllegalArgumentException()}).component)
  }
  
  def clear(panel : Container) : Result = uielems.clearContainer(panel)
  
  def update(oldElem : Elem, newElem : Elem, panel : Container, removeAction : () => Result, updateAction : Elem => Result, changeAction : Elem => Unit) : Result = {
    val newViewElem = makeElem(newElem,removeAction,updateAction,changeAction)
    val oldViewElem = elems.getOrElse(oldElem,{throw new IllegalArgumentException()})
    elems += (newElem -> newViewElem)
    elems -= oldElem
    uielems.replaceInContainer(panel, oldViewElem.component, newViewElem.component)
  }
  
  protected def canAdd = true
  
  def panel(elements : Seq[Elem], addAction : () => Result, removeAction : Elem => Result, updateAction : Elem => Result, changeAction : (Elem,Elem) => Unit) = {
    val main : Seq[Component] = (border(style=Some("panel_top"))(
        BorderLocation.MIDDLE -> label(title,style=Some("sub_title")),
        BorderLocation.RIGHT -> horizontal()(
          if(canAdd) {
            button(nolocalize("+"),style=Some("sub_title")) {
              addAction()
            }
          } else {
            spacer()
          }
        ),
        BorderLocation.BOTTOM -> (if(app.showHelp) {
            label(description,style=Some("help"))
          } else {
            spacer(0, 0)
          })
      )
    ) +: (for(elem <- elements) yield {
        val newElem = makeElem(elem,() => removeAction(elem), updateAction, elem2 => changeAction(elem,elem2))
        elems += (elem -> newElem)
        newElem.component
      })
    vertical(true,-1,Some("view"),None)(main:_*)
  }
  
  protected def views(elem : Elem) : Seq[StdController[_,Component,Container]] = Nil
  
  def display(elements : Seq[Elem]) : Component = if(elements.isEmpty) {
    vertical()()
  } else {
    vertical()(
      (label(title,style=Some("sub_title"))  +:
       (for(elem <- elements) yield {
            vertical()(
              (
                (makeDisplay(elem)  +: (
                    for(view <- views(elem)) yield {
                      view.display
                    }
                  )
                ):_*
              )
            )
          }
        )
      ):_*
    )
  }
  // To implement define these functions
  /** The title of the component */
  val title : String
  /** Tooltip for adding elements */
  val addText : String
  /** Tooltip for removing elements */
  val removeText : String
  /** Long description of the concept */
  val description : String
  /** Create an editable element in this view */
  protected def makeElem(elem : Elem, removeAction : () => Result, updateAction : Elem => Result, changeAction : Elem => Unit) : StdElement[Component,Container]
  /** Create an uneditable element in this view */
  protected def makeDisplay(elem : Elem) : Component
  /** Show the add dialog */
  def dialog(elem : Option[Elem], after : Elem => Result) : Result
}