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
import eu.monnetproject.lemon.source.app.LemonEditorApp

/**
 * 
 * @author John McCrae
 */
trait PagedPanel[Elem,Component,Container<:Component] extends StdPanel[Elem,Component,Container] {
  import UIElems._
  import uielems._
  
  def app : LemonEditorApp
  
  override def panel(elements : Seq[Elem], addAction : () => Result, removeAction : Elem => Result, updateAction : Elem => Result, changeAction : (Elem,Elem) => Unit) = {
    throw new RuntimeException("Paged panel used with std controller")
  }
  
  private class ButtonContainer {
    var c : Component = null.asInstanceOf[Component]
  }
  
  private val id = "paged" + new java.util.Random().nextInt
  
  def panel(elements : Seq[Elem], 
            addAction : () => Result, 
            removeAction : Elem => Result, 
            updateAction : Elem => Result, 
            changeAction : (Elem,Elem) => Unit,
            next : () => Result,
            prev : () => Result,
            hasPrev : Boolean,
            hasNext : Boolean) = {
    val prevContainer = new ButtonContainer()
    val nextContainer = new ButtonContainer()
    val prevButton = button(nolocalize("<<"),style=Some("sub_title"),enabled=hasPrev) {
      prev()
    }
    val nextButton = button(nolocalize(">>"),style=Some("sub_title"),enabled=hasNext) {
      next()
    }
    prevContainer.c = prevButton
    nextContainer.c = nextButton
                                                               
    vertical(true,-1,None,Some(id))(
      ((border(style=Some("panel_top"))(
            BorderLocation.MIDDLE -> label(title,style=Some("sub_title")),
            BorderLocation.RIGHT -> horizontal()(
              prevButton,
              if(canAdd) {
                button(nolocalize("+"),style=Some("sub_title")) {
                  addAction()
                }
              } else {
                spacer()
              },
              nextButton
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
      ):_*
    )
  }
}
