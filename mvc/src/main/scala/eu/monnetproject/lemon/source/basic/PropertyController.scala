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
package eu.monnetproject.lemon.source.basic

import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import java.net.URI
import scala.collection.JavaConversions._

case class PropertyPair(val prop : Property, val propVal : PropertyValue)

/**
 * Controls the display of properties
 * @author John McCrae
 */
class PropertyController[Elem <: LemonElement,C,O<:C](state : State, element : Elem, uielems : UIElems[C,O], elemClass : URI) extends 
StdController[PropertyPair,C,O](new PropertyModel(element), new PropertyView[C,O](state,uielems, elemClass))

class PropertyModel[Elem <: LemonElement](element : Elem) extends StdModel[PropertyPair] {
  def add(pp : PropertyPair) = element.addProperty(pp.prop, pp.propVal)
  def remove(pp : PropertyPair) = element.removeProperty(pp.prop, pp.propVal)
  def elements = (for{ (key,values) <- element.getPropertys
                     value <- values 
  } yield PropertyPair(key,value)).toSeq
}

class PropertyView[C,O<:C](state : State, val uielems : UIElems[C,O], elemClass : URI) extends 
HasState(state) with StdPanel[PropertyPair,C,O] {
  import UIElems._
  import uielems._
  
  val title = app.l10n("Properties",List(elemClass))   
  val addText = "Add Lexical Property"
  val removeText = "Remove Lexical Property"
  val description = "A property is a category that this lemon element can be assigned to; this allows you to model part of speech, number, gender etc."
  protected def makeElem(pair : PropertyPair, removeAction : () => Result, updateAction : PropertyPair => Result, changeAction : PropertyPair => Unit) = {
    new StdColElement(List(pair.prop.getURI.getFragment, pair.propVal.getURI.getFragment),
                      Nil,
                      removeAction,
                      () => updateAction(pair),
                      uielems)
  }
  protected def makeDisplay(pair : PropertyPair) = html("<li>"+pair.prop.getURI.getFragment + " = " + pair.propVal.getURI.getFragment + "</li>")
  
  val propEditorWindow = new PropertyEditorWindow(app,lingOnto,uielems)
  
  def dialog(propPair : Option[PropertyPair], after : (PropertyPair) => Result) : Result = {
    propEditorWindow.show(propPair,after)
  }
}