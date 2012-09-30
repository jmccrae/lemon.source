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
 * A UI Element (generally part of a panel)
 * @author John McCrae
 */
trait ViewElement[Component,Container<:Component] {
  def select : Result
  def deselect : Result
  def setDescription(desc : String) : Result
  def component : Container
  def addComponent(component : Component) : Result
  protected def initMain : List[Component]
  protected def initActions : List[Component]
  def values : List[String]
  def editable : Boolean
  def deleteAction : Option[() => Result]
  def selectAction() : Result
}

trait DefaultViewElement[Component,Container<:Component] extends ViewElement[Component,Container] {
  def select = NoResult
  def deselect = NoResult
  def setDescription(desc : String)  = NoResult
  def addComponent(component : Component) = NoResult
  protected def initMain = List(component)
  protected def initActions = Nil
  def values : List[String] = Nil
  def deleteAction = None
  def selectAction() = NoResult
}