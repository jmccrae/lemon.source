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
package eu.monnetproject.lemon.source.semantics

import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.common._
import scala.collection.JavaConversions._

/**
 * Controller for contexts
 * @author John McCrae
 */
class ContextController[C,O<:C](state : State, sense : LexicalSense, uielems : UIElems[C,O])
extends StdController[SenseContext,C,O](new ContextModel(sense), new ContextView(state,uielems))

class ContextModel(sense : LexicalSense) extends StdModel[SenseContext] {
  def add(context : SenseContext) = {
    sense.addContext(context)
  }
  
  def remove(context : SenseContext) = {
    sense.removeContext(context)
  }
  
  def elements = sense.getContexts.toSeq
}

class ContextView[C,O<:C](state : State, val uielems : UIElems[C,O])
extends HasState(state) with StdPanel[SenseContext,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Contexts"
  val addText = "Add context"
  val removeText = "Remove context"
  val description = "A context describes the pragmatic, geographic or diachronic usage of a sense of a word."
  
  protected def displayContext(context : SenseContext) : String = {
    if(context.getValue != null) {
      context.getValue.value;
    } else if(context.getURI != null) {
      context.getURI.getFragment
    } else {
      "null"
    }
  }
  
  protected def makeElem(context : SenseContext,
                         remove : () => Result,
                         update : SenseContext => Result,
                         change : SenseContext => Unit) = {
    new StdColElement(List(displayContext(context)),Nil,remove, () => update(context),uielems)
  }
  
  protected def makeDisplay(context : SenseContext) = html("<li>" + displayContext(context) + "</li>")
  
  def dialog(context : Option[SenseContext], after : SenseContext => Result) = {
    val defContext = context match {
      case Some(ctxt) => Some(displayContext(ctxt))
      case None => None
    }
    prompt().show(app, "Select sense context", "Context", 
                  lingOnto.getContexts.map(displayContext(_)).toList.sorted,
                  defContext, s => {
        val sc = lingOnto.getContext(s)
        after(sc)
      }
    )
  }
}
//class ContextController(val view : UIView[SenseContext] with State, sense : LexicalSense)
//    extends HasState(view) with GenericController[SenseContext] {
//  val title = "Contexts"
//  val addText = "Add context"
//  val removeText = "Remove context"
//  
//  def unwrap(context : SenseContext) = List(context.getURI().getFragment())
//  
//  def wrap(values : List[String]) = values match {
//    case List(value) => Some(lingOnto.getContext(value))
//    case _ => None
//  }
//  
//  def load {
//    for(context <- sense.getContexts) {
//      view add context
//    }
//  }
//  
//  def add(context : SenseContext) = {
//    sense addContext context
//    view add context
//  }
//  
//  def remove(context : SenseContext) = {
//    sense removeContext context
//    view remove context
//  }
//}  
//
