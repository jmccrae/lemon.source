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

package eu.monnetproject.lemon.source.relations

import eu.monnetproject.lemon.LemonModels
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.basic.EntrySelectWindow
import eu.monnetproject.lemon.model._
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class SenseRelationController[C,O<:C](state : State, sense : LexicalSense, uielems : UIElems[C,O])
extends StdController[(SenseRelation,LexicalEntry,LexicalSense),C,O](new SenseRelationModel(state,sense), new SenseRelationView(state,uielems)) 

class SenseRelationModel(state : State, sense : LexicalSense) extends StdModel[(SenseRelation,LexicalEntry,LexicalSense)] {
  def add(x : (SenseRelation,LexicalEntry,LexicalSense)) = {
    sense.addSenseRelation(x._1, x._3)
  }
  
  def remove(x : (SenseRelation,LexicalEntry,LexicalSense)) = {
    sense.removeSenseRelation(x._1, x._3)
  }
  
  def entryForSense(sense : LexicalSense) : LexicalEntry = {
    Option(LemonModels.getEntryBySense(state.model,sense)).getOrElse(throw new RuntimeException("No entry for sense " + sense.getURI))
  }
  
  def elements = (for{
      (variant,senses) <- sense.getSenseRelations().toSeq
      sense <- senses
    } yield (variant,entryForSense(sense),sense)).toSeq.sortBy(_._1.getURI.toString)
}

class SenseRelationView[C,O<:C](state : State, val uielems : UIElems[C,O]) 
extends HasState(state) with StdPanel[(SenseRelation,LexicalEntry,LexicalSense),C,O] {
  import UIElems._
  import uielems._
  
  val title = "Sense relations"
  val addText = "Add sense relation"
  val removeText = "Remove sense relation"
  val description = "Indicates a relation between entries that is sensitive to the meaning of the word, such as translation or antonymy."
  
  protected def makeElem(x : (SenseRelation, LexicalEntry,LexicalSense),
                         remove : () => Result,
                         update : ((SenseRelation,LexicalEntry,LexicalSense)) => Result,
                         change : ((SenseRelation,LexicalEntry,LexicalSense)) => Unit) = {
    new StdColElement(List(app.namer.displayName(x._2),x._1.getURI.getFragment,  x._3.getReference.toString),views(x),remove,() => update(x),uielems)
  }
  
  protected def makeDisplay(x : (SenseRelation,LexicalEntry,LexicalSense)) = html("<li>" + x._1.getURI.getFragment + " -&gt; <a href=\"" + x._2.getURI + "\">"+app.namer.displayName(x._2)+"</a> ("+x._3.getReference+")</li>")
  
  lazy val entrySelect = new EntrySelectWindow(uielems,state,model)
  
  def dialog(x : Option[(SenseRelation,LexicalEntry,LexicalSense)], after : ((SenseRelation,LexicalEntry,LexicalSense)) => Result) = {
    val default = x match {
      case Some((sr,_,_)) => Some(sr.getURI.getFragment)
      case None => None
    }
    val defaultSense = x match {
      case Some((_,_,ls)) => Some(ls.getReference.toString)
      case None => None
    }
    prompt().show(app, "Select sense relation type", "Sense relation type",
                  (lingOnto.getSenseRelation() map { _.getURI().getFragment() }).toList.sorted,
                  default, s => {
        val sr = lingOnto.getSenseRelation(s)
        entrySelect.show(le => {
            prompt().show(app, "Select sense", "Sense",(le.getSenses map { _.getReference().toString() }).toList.sorted,
                        defaultSense, lsRef => {
                val sense = le.getSenses.find(_.getReference().toString() == lsRef).getOrElse(throw new RuntimeException())
                after((sr,le,sense))
              }
            )
          })
      });
  }
}