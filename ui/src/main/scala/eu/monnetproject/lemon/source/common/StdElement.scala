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
abstract class StdElement[Component,Container<:Component](val views : Seq[StdController[_,Component,Container]], 
                                                          remove : () => Result,
                                                          uielems : UIElems[Component,Container],
                                                          hideDel : Boolean = false) {
  import UIElems._
  import uielems._
  
  private var selected = false
  
  def select = if(!selected) {
    selected = true
    val expandButtonAction = expandButton match {
      case Some(button) => button.toggle
      case None => NoResult
    }
    setVisible(viewsPanel, true) >> animate(viewsPanel, Animations.ROLL_DOWN) >> expandButtonAction
  } else {
    NoResult
  }
  def deselect = if(selected) {
    selected = false
    val expandButtonAction = expandButton match {
      case Some(button) => button.toggle
      case None => NoResult
    }
    setVisible(viewsPanel, false) >> animate(viewsPanel, Animations.ROLL_UP) >> expandButtonAction
  } else {
    NoResult
  }
    
  private val viewsPanel : Container = vertical()(
    (for(view <- views) yield {
        view.panel
      }):_*
  )
  
  private val delButton : Component = (button(nolocalize("\u00d7"),style=Some("lint_ignore")) {
      prompt() delete (app, {
          () => { remove()  }
        }) 
    })
  
  private val expandButton : Option[ToggleElem] = if(views.isEmpty) {
    None
  } else {
    Some(imageToggleButton("expandarrow.png","unexpandarrow.png") {
        selected => if(selected) { 
          deselect 
        } else { 
          select 
        }
      })
  }
  
  protected def cssClass : Option[String] = None
  
  val component = {
    val (hasActions,widthActions) = actions match {
      case Nil => (false,Nil.padTo(main.size,100/main.size) zip main)
      case xs => (true,(Nil.padTo(main.size,90/main.size) zip main) :+ (10,(horizontal()(xs:_*))))
    }
    vertical(widthPercent=100,style=cssClass)(
      columns(rightAlignLast=hasActions)(widthActions),
      viewsPanel
    )
  }
  
  protected def main : Seq[Component]
  protected def actions : Seq[Component] = if(hideDel) {
    (expandButton map (_.component)).toSeq
  } else {
    Seq(delButton) ++ (expandButton map (_.component))
  }
}
