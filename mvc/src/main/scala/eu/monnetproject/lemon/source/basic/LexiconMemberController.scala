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

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import scala.collection.JavaConversions._

/**
 * @author John McCrae
 */
class LexiconMemberController[C,O<:C](state : State, entry : LexicalEntry, uielems : UIElems[C,O]) 
extends StdController[Lexicon,C,O](new LexiconMemberModel(state,entry), new LexiconMemberView[C,O](state,uielems))

class LexiconMemberModel(state : State, entry : LexicalEntry) extends StdModel[Lexicon] {
  def add(lexicon : Lexicon) = lexicon.addEntry(entry)
  def remove(lexicon : Lexicon) = lexicon.removeEntry(entry)
  def elements = LemonModels.getLexicaByEntry(state.model,entry)
}

class LexiconMemberView[C,O<:C](state : State, val uielems : UIElems[C,O]) extends 
HasState(state) with StdPanel[Lexicon,C,O] {
  import UIElems._
  import uielems._
    
  val title = "Member of lexica"
  val addText = "Add to a lexicon"
  val removeText = "Remove from a lexicon"
  val description = "The collection of lexica that this entry is a member of"
  
  protected def makeElem(lexicon : Lexicon, 
                         removeAction : () => Result,
                         updateAction : Lexicon => Result,
                         changeAction : Lexicon => Unit) = {
    new StdElement(Nil,removeAction,uielems) {
      override def main = List(
        button(
          nolocalize(app.namer.displayName(lexicon))
        ) {
          app.navigator.lexicon(lexicon,true)
        }
      )
    }
  }
  
  protected def makeDisplay(lexicon : Lexicon) = button(
    nolocalize(app.namer.displayName(lexicon))
  ) {
    app.navigator.lexicon(lexicon,true)
  }
        
  def dialog(lexicon : Option[Lexicon], after : Lexicon => Result) = NoResult
  
}
