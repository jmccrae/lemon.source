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
package eu.monnetproject.lemon.source.common;

import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.util._

/**
 * A view on a set of elements that supports add/remove/select 
 * @author John McCrae
 */
trait UIPanel[Elem,Component,Container<:Component] extends UIView[Elem] {
  private val log = Logging.getLogger(this)
  val uielems : UIElems[Component,Container]
  import uielems._
  import UIElems._
  var elems = Map[Elem,ViewElement[Component,Container]]()
  def removeAction = controller.removeAction match {
    case Some((_,action)) => Some(action)
    case None => None
  }
  def selectAction : (Elem) => Result = controller.selectAction 
  val pageForwardAction : Option[() => Unit] = None
  val pageBackwardAction : Option[() => Unit] = None
  val pageStatus : Option[() => (Boolean,Boolean)] = None
  def editable = true
  private var empty = true
  
  def controller : GenericController[Elem]
  
  def initialize {
    controller.initialize
    if(empty && !editable) {
      setVisible(panel,false)
    } 
  }
  
  // Called when an element needs to be selected
  def select(elem : Elem) = {
    elems.get(elem) match {
      case Some(c) => c.select //>> new SelectElementResult(c)
      case None => NoResult
    }
  }
  
  // Called when an element needs to be deselected
  def deselect(elem : Elem) = {
    elems.get(elem) match {
      case Some(c) => c.deselect /*; new DeselectElementResult(c)*/
      case None => NoResult
    }
  }  
  
  def setEnabled(isEnabled : Boolean) = uielems.setEnabled(panel,isEnabled)
  
  // Add an element with a given caption
  def add(elem : Elem) = {
    log.info("add " + elem)
    empty = false
    val e = makeElem(elem,controller.unwrap(elem))
    elems += (elem -> e)
    addElem(elemContainer,e)
    setVisible(panel,true) //>> new AddElementResult(this,e)
  }
  
  // Remove an element
  def remove(elem : Elem) = {
    elems.get(elem) match {
      case Some(c) => {
          removeElem(elemContainer,c)
          elems -= elem
          //new DeleteElementResult(c)
          NoResult
        } 
      case None => NoResult
    }
  }
  
  // Update an element
  def update(oldElem : Elem, newElem : Elem) = {
    elems.get(oldElem) match {
      case Some(c) => {
          val e = makeElem(newElem,controller.unwrap(newElem))
          elems += (newElem -> e)
          replaceElem(elemContainer,c,e)
          //new UpdateElementResult(c,e)
          NoResult
        } 
      case None => NoResult
    }
  }
  
  // Reset the container
  def reset() = {
    clearContainer(elemContainer)
  }
  
  val elemContainer = vertical()()
  
  lazy val panel = if(editable) {
    vertical()(
      border(style=Some("panel_top"))(
        BorderLocation.MIDDLE -> label(controller.title,style=Some("sub_title")),
        BorderLocation.RIGHT -> horizontal()(
          (if(pageForwardAction != None && pageBackwardAction != None) {
              pageButtons(style=Some("sub_title"))(
                () => { pageBackwardAction.get.apply() ; /*new UpdateViewContentsResult(this)*/NoResult },
                () => { pageForwardAction.get.apply() ; /*new UpdateViewContentsResult(this)*/NoResult },
                pageStatus.get)
            } else {
              spacer(widthPx=0)
            }),
          (controller.addAction match {
              case Some((desc,action)) => button(nolocalize("+"),description=desc,style=Some("sub_title")) {
                  action()
                }
              case None => spacer(widthPx=0)
            })
        )
      ),
      elemContainer
    )
  } else {
    vertical()(
      horizontal(style=Some("panel_top hidden"))(label(controller.title,style=Some("sub_title"))),
      elemContainer
    )
  }
  
  // Make the appropriate UI element
  protected def makeElem(elem : Elem, vals : List[String]) : ViewElement[Component,Container]
}

trait UIPanel2[Elem,Component,Container<:Component] {
  val uielems : UIElems[Component,Container]
  import uielems._
  import UIElems._
  
  protected var elems = Map[Elem,ViewElement[Component,Container]]()
  
  def select(elem : Elem) : Result = elems.getOrElse(elem,{throw new IllegalArgumentException()}).select
  def deselect(elem : Elem) : Result = elems.getOrElse(elem,{throw new IllegalArgumentException()}).deselect
  def add(elem : Elem, panel : Container, removeAction : () => Result) : Result = {
    val newElem = makeElem(elem,removeAction)
    elems += (elem -> newElem)
    uielems.addToContainer(panel, newElem.component)
  }
  
  def remove(elem : Elem, panel : Container) : Result = {
    uielems.removeFromContainer(panel, elems.getOrElse(elem,{throw new IllegalArgumentException()}).component)
  }
  
  def update(oldElem : Elem, newElem : Elem, panel : Container, removeAction : () => Result) : Result = {
    val newViewElem = makeElem(newElem,removeAction)
    val oldViewElem = elems.getOrElse(oldElem,{throw new IllegalArgumentException()})
    elems += (newElem -> newViewElem)
    elems -= oldElem
    uielems.replaceInContainer(panel, oldViewElem.component, newViewElem.component)
  }
  
  def panel(elements : Seq[Elem], addAction : () => Result, removeAction : Elem => Result) = vertical()(
    ((border(style=Some("panel_top"))(
          BorderLocation.MIDDLE -> label(title,style=Some("sub_title")),
          BorderLocation.RIGHT -> horizontal()(
            button(nolocalize("+"),style=Some("sub_title")) {
              addAction()
            }
          )
        )
      ) +: (for(elem <- elements) yield {
          val newElem = makeElem(elem,() => removeAction(elem))
          elems += (elem -> newElem)
          newElem.component
        })
    ):_*
  )
  
  def display(elements : Seq[Elem]) = vertical()(
    (for(elem <- elements) yield {
        makeDisplay(elem)
      }):_*
  )
  // To implement define these functions
  /** The title of the component */
  val title : String
  /** Tooltip for adding elements */
  val addText : String
  /** Tooltip for removing elements */
  val removeText : String
  /** Create an editable element in this view */
  protected def makeElem(elem : Elem, removeAction : () => Result) : ViewElement[Component,Container]
  /** Create an uneditable element in this view */
  protected def makeDisplay(elem : Elem) : Component
  /** Show the add dialog */
  def dialog(elem : Option[Elem], after : Elem => Result) : Result
}
