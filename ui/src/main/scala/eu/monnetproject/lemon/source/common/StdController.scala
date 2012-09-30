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
import eu.monnetproject.util.Logging

/**
 * 
 * @author John McCrae
 */
class StdController[Elem,Component,Container<:Component](val model : StdModel[Elem],val view : StdPanel[Elem,Component,Container]) {
  private val log = Logging.getLogger(this)
  protected var selected : Option[Elem] = None
  
  def add() : Result = {
    view.dialog(None, { elem => 
        model.add(elem)
        view.add(elem,panel,() => remove(elem), update, elem2 => change(elem,elem2))
      })
  }
  
  def remove(elem : Elem) : Result = {
    model.remove(elem)
    view.remove(elem,panel)
  }
  
  def update(elem : Elem) : Result = {
    view.dialog(Some(elem), { elem2 => 
        model.update(elem,elem2)
        view.update(elem,elem2,panel,() => remove(elem2), update, elem3 => change(elem2,elem3))
      })
  }
  
  // Changes are from the UI, so only the model is changed
  def change(oldElem : Elem, newElem : Elem) {
    model.update(oldElem,newElem)
  }
  
  def select(elem : Elem) : Result = {
    val result = selected match {
      case Some(elem) => view.deselect(elem)
      case None => NoResult
    }
    selected = Some(elem)
    result >> view.select(elem)
  }
  
  def unselect() : Result = {
    val result = selected match {
      case Some(elem) => view.deselect(elem)
      case None => NoResult
    }
    selected = None
    result
  }
  
  lazy val panel = try {
    view.panel(model.elements, add, remove, update, change)
  } catch {
    case x : Exception => log.stackTrace(x) ; view.panel(Nil, add, remove, update, change)
  }
  lazy val display = try {
    view.display(model.elements)
  } catch {
    case x : Exception => log.stackTrace(x) ; view.display(Nil)
  }
}
