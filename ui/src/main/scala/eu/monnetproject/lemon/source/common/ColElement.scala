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

import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.app.LemonEditorApp

/**
 * 
 * @author John McCrae
 */
class ColElement[Component,Container<:Component](val app : LemonEditorApp, val values : List[String], val editable : Boolean, val deleteAction : Option[() => Result],
                     selectAction2 : () => Result, val uielems : UIElems[Component,Container]) extends ViewElement[Component,Container] {
     
  import UIElems._
  import uielems._
  def selectAction() = selectAction2()
  protected var nameBoxes = List[Component]();
  val entryContainer = horizontal()()
  val subContainer = views()
  protected val expandButton : ToggleElem = imageToggleButton("expandarrow.png","unexpandarrow.png") {
    selected => if(selected) { 
      deselect
      //new DeselectElementResult(this) 
    } else { 
      select 
      //new SelectElementResult(this)
    }
  }
  //private val expandIcon = imageButton("expandarrow.png")
  //private val unexpandIcon = new ThemeResource("unexpandarrow.png")
  
  
  val container = if(editable) {
    val main = initMain
    val (hasActions,widthActions) = initActions match {
      case Nil => (false,Nil.padTo(main.size,100/main.size) zip main)
      case xs => (true,(Nil.padTo(main.size,90/main.size) zip main) :+ (10,(horizontal()(xs:_*))))
    }
    vertical(widthPercent=100)(
      columns(rightAlignLast=hasActions)(widthActions)
    )
  } else {
    vertical()(
      (initMain match {
          case null => spacer()
          case x => horizontal()(x:_*)
        })
    )
  }
  
  protected def initMain : List[Component] = {  
    (for(val1 <- values) yield {
      button(nolocalize(val1)) {
        select //>>
        //new SelectElementResult(this)
      }
    })
  }
  //app.contextHelp.addHelpForComponent(nameBox,help);
  
  protected def initActions = {
    deleteAction match {
      case Some(del) => 
          (button(nolocalize("\u00d7")) {
            prompt() delete (app, {
                () => { del ; NoResult/*; new DeleteElementResult(this)*/ }
              }) 
          })::
          expandButton.component :: Nil
      case None => expandButton.component :: Nil
    }
  }
   
  protected def initEnd {
  }
  
  def setDescription(desc : String)  = NoResult 
//  for(nameBox <- nameBoxes) {
//   nameBox setDescription desc
//   }
  
  def addComponent(component : Component) = {
    setVisible(expandButton.component,true)
    addToContainer(subContainer,component)
    if(!editable) {
      addToContainer(container,subContainer)
      setVisible(subContainer,false)
    }
    NoResult
    //component setWidth "100%"
  }
  
  def component = container
  
  private var selected = false
  
  // Make this appear "selected"
  def select : Result = { 
    if(!selected && editable) {
      selected = true
      val result = selectAction()
      expandButton.toggle 
      addToContainer(container, subContainer) >>
      animate(subContainer, Animations.ROLL_DOWN) >>
      result
      //app.animator.animate(subContainer, AnimType.ROLL_DOWN_OPEN).setDuration(500).setDelay(100)
      //expandButton setIcon unexpandIcon
    } else {
      NoResult
    }
  }
  
  // Make this appear not "selected"
  def deselect = {
    if(selected && editable) {
      selected = false
      animate(subContainer,Animations.ROLL_UP)
      removeFromContainer(container,subContainer)
      expandButton.toggle
//      app.animator.animate(subContainer, AnimType.ROLL_UP_CLOSE).setDuration(500).setDelay(100)
//      container removeComponent subContainer
//      expandButton setIcon expandIcon
    }
    NoResult
  }
}
