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

import eu.monnetproject.lang.Language
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.util.Logging
import scala.collection.JavaConversions._

class DefinitionController[C,O<:C](state : State, sense : LexicalSense, uielems : UIElems[C,O])
extends StdController[(Definition,SenseDefinition),C,O](new DefinitionModel(sense), new DefinitionView(state,uielems,sense))

class DefinitionModel(sense : LexicalSense) extends StdModel[(Definition,SenseDefinition)] {
  def add(defn : (Definition,SenseDefinition)) = {
    sense.addDefinition(defn._1, defn._2)
  }
  
  def remove(defn : (Definition,SenseDefinition)) = {
    sense.removeDefinition(defn._1, defn._2)
  }
  
  def elements = (for{
    (pred,defs) <- sense.getDefinitions.toSeq
    defn <- defs
  } yield(pred,defn))
}

class DefinitionView[C,O<:C](state : State, val uielems : UIElems[C,O], sense : LexicalSense)
extends HasState(state) with StdPanel[(Definition,SenseDefinition),C,O] {
  private val log = Logging.getLogger(this)
  import UIElems._
  import uielems._
  
  val title = "Definitions"
  val addText = "Add definition"
  val removeText = "Remove definition"
  val description = "A definition is a longer piece of text that describes the sense of the entry"
  
  protected def makeElem(defn : (Definition,SenseDefinition),
                         remove : () => Result,
                         update : ((Definition,SenseDefinition)) => Result,
                         change : ((Definition,SenseDefinition)) => Unit) = {
    new StdColElement(List(getFrag(defn._1),getValue(defn._2)),Nil,remove,() => update(defn),uielems) 
  }
  
  private def getFrag(elem : Definition) = elem.getURI match {
    case null => "null"
    case uri => uri.getFragment
  }
  
  private def getValue(sd : SenseDefinition) : String = sd.getValue match {
    case null => "Missing"
    case value => value.value
  }
  
  protected def makeDisplay(defn : (Definition,SenseDefinition)) = html("<li>(<i>"+getFrag(defn._1)+"</i>) " + getValue(defn._2)+"</li>")
  
  def dialog(defn : Option[(Definition,SenseDefinition)], after : ((Definition,SenseDefinition)) => Result) = {
    val defDefn = defn match {
      case Some((d,_)) => Some(d)
      case None => None
    }
    val defVal = defn match {
      case Some((_,v)) => v.getValue.value
      case None => ""
    }
    for(defn <- lingOnto.getDefinitions) {
      log.info(defn.toString)
    }
    for(defn <- lingOnto.getDefinitions.toList.sortBy(_.getURI.toString)) {
      log.info(defn.toString)
    }
    uielems.dialog("Set sense definiton", formElems(
        uielems.select("Defintion Type",defDefn)(
          (for(defnType <- lingOnto.getDefinitions) yield {
              (defnType,defnType.getURI.getFragment)
          }).toList.sortBy(_._2):_*
        )(e => NoResult),
        textArea("Definiton",defVal)
      )
    ) {
      case (defn3,defnVal) => {
          val defn2 = lemonFactory.makeDefinition(app.namer.name(sense,"defintion"))
          defn2.setValue(new Text(defnVal,lexicon.getLanguage))
          after((defn3,defn2))
      }
    }
  }
}
