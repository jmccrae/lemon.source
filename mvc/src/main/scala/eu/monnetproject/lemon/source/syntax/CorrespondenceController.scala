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

package eu.monnetproject.lemon.source.syntax

import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.util.Logging
import scala.collection.JavaConversions._

/**
 * Syn-Sem correspondence contoller
 * 
 * @author John McCrae
 */
class CorrespondenceController[C,O<:C](state : State, frame : Frame, entry : LexicalEntry, uielems : UIElems[C,O])
extends StdController[LexicalSense,C,O](new CorrespondenceModel(frame,entry), new CorrespondenceView(state,frame,entry,uielems))

class CorrespondenceModel(frame : Frame, entry : LexicalEntry) extends StdModel[LexicalSense] {
  private val log = Logging.getLogger(this)
  
  def add(sense : LexicalSense) = {
    if(frame.getSynArgs.size == 1) {
      sense.addIsA(frame.getSynArgs.head._2.head)
    } else if(frame.getSynArgs.size == 2) {
      val (typ1,arg1) = frame.getSynArgs.head
      val (typ2,arg2) = frame.getSynArgs.tail.head
      if(typ2.getURI.toString.contains("obj")) {
        sense.addSubjOfProp(arg1.head)
        sense.addObjOfProp(arg2.head)
      } else {
        sense.addSubjOfProp(arg2.head)
        sense.addObjOfProp(arg1.head)
      }
    } else {
      log.warning("Cannot handle multivariate senses")
    }
  }
  
  def remove(sense : LexicalSense) = {
    for((_,args) <- frame.getSynArgs ; arg <- args) {
      if(sense.getIsAs.contains(arg)) {
        sense.removeIsA(arg)
      } else if(sense.getSubjOfProps.contains(arg)) {
        sense.removeSubjOfProp(arg)
      } else if(sense.getObjOfProps.contains(arg)) {
        sense.removeObjOfProp(arg)
      }
    }
  }
  
  private def shareArg(frame : Frame, sense : LexicalSense) : Boolean = {
    for((_,args) <- frame.getSynArgs ; arg <- args) {
      if(sense.getIsAs.contains(arg)) {
        return true
      } else if(sense.getSubjOfProps.contains(arg)) {
        return true
      } else if(sense.getObjOfProps.contains(arg)) {
        return true
      }
    }
    return false
  }
  
  def elements = (for(sense <- entry.getSenses ; if shareArg(frame,sense)) yield sense).toSeq
}

class CorrespondenceView[C,O<:C](state : State, frame : Frame, entry : LexicalEntry, val uielems : UIElems[C,O])
extends HasState(state) with StdPanel[LexicalSense,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Correspondences"
  val addText = "Add correspondence"
  val removeText = "Remove correspondence"
  val description = "The correspondence indicates the mapping between the syntactic usage of the entry and the usage of the ontology entity."
  
  override def views(sense : LexicalSense) = List(
    new ArgumentController(state,frame,sense,uielems)
  )
  
  protected def makeElem(sense : LexicalSense,
                         remove : () => Result,
                         update : LexicalSense => Result,
                         change : LexicalSense => Unit) = {
    new StdColElement(List(sense.getReference.toString),views(sense),remove, () => update(sense), uielems)
  }
  
  protected def makeDisplay(sense : LexicalSense) = html("<li>" + sense.getReference + "</li>")
  
  def dialog(sense : Option[LexicalSense], after : LexicalSense => Result) = {
    val srs = (for(sense2 <- entry.getSenses()) yield {
        (sense2,sense2.getReference.toString)
      }).toSeq
    uielems.dialog("Syntactic-Semantic Correspondence", uielems.select("Correspondence",sense)(srs:_*)(e => NoResult))(after)
  }
}