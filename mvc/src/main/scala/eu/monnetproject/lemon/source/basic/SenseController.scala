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
import eu.monnetproject.lemon.source.relations.SenseRelationController
import eu.monnetproject.lemon.source.semantics._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import java.net.URI
import scala.collection.JavaConversions._

/**
 * Controls the display of senses
 * @author John McCrae
 */
class SenseController[C,O<:C](state : State, entry : LexicalEntry, uielems : UIElems[C,O]) 
extends StdController[LexicalSense,C,O](new SenseModel(entry),new SenseView(state,uielems,entry))

class SenseModel(entry : LexicalEntry) extends StdModel[LexicalSense] {
  def add(sense : LexicalSense) = {
    entry.addSense(sense)
  }
  
  def remove(sense : LexicalSense) = {
    entry.removeSense(sense)
  }
  
  def elements = entry.getSenses.toSeq
}

class SenseView[C,O<:C](state : State, val uielems : UIElems[C,O], entry : LexicalEntry)
extends HasState(state) with StdPanel[LexicalSense,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Senses"
  val addText = "Add Sense"
  val removeText = "Remove Sense"
  val description = "A sense is a meaning of a given word. In lemon senses are given by reference to resources on the web."

  protected def makeElem(sense : LexicalSense,
                         remove : () => Result,
                         update : LexicalSense => Result,
                         change : LexicalSense => Unit) = {
    new StdColElement(List(getReference(sense)),views(sense),remove,() => update(sense),uielems)
  }
  
  private def getReference(sense : LexicalSense) = sense.getReference match {
    case null => "No URI"
    case ref => ref.toString
  }
  
  protected def makeDisplay(sense : LexicalSense) = sense.getReference match {
    case null => html("<li>???</li>")
    case ref => html("<li><a href=\"" + getReference(sense) + "\">"+getReference(sense)+"</a></li>")
  }
  
  def dialog(sense : Option[LexicalSense], after : LexicalSense => Result) = {
    new ReferenceSelectWindow(this,uielems).show((s : String) => {
        val newSense = lemonFactory.makeSense(app.namer.name(entry,"sense"))
        newSense.setReference(URI.create(s))
        after(newSense)
      })
  }
  
  protected override def views(sense : LexicalSense) : Seq[StdController[_,C,O]] = 
    List(
         new ContextController(state,sense,uielems),
         new DefinitionController(state,sense,uielems),
         new ExampleController(state,sense,uielems),
         new SenseRelationController(state,sense,uielems)
    )
}

//class SenseController(val view : UIView[LexicalSense] with State, entry : LexicalEntry)
//    extends HasState(view) with GenericController[LexicalSense] {
//   
//  val title = "Senses"
//  val addText = "Add Sense"
//  val removeText = "Remove Sense"
//  
//  def unwrap(sense : LexicalSense) = List(getReference(sense))
//  
//  def wrap(vals : List[String]) : Option[LexicalSense] = {
//    val newSenseRef = new URI(vals(0))
//    val newSense = lemonFactory.makeSense
//    newSense setReference newSenseRef
//    Some(newSense)
//  }
//  
//  def load = {
//    for(sense <- entry.getSenses) {
//      view.add(sense)
//    }
//  }
//  
//  def add(sense : LexicalSense) = {
//    entry.addSense(sense)
//    view add sense
//  }
//  
//  def remove(sense : LexicalSense) = {
//    entry.removeSense(sense)
//    view remove sense
//  }
//}
