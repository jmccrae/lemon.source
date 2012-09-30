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
package eu.monnetproject.lemon.source.lexicon

import eu.monnetproject.lang.Language
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.util.Logging
import scala.collection.JavaConversions._

/**
 * Controls the display of lexica
 * @author John McCrae
 */
class LexiconController[C,O<:C](state : State, uielems : UIElems[C,O], public : Boolean)
extends StdController[Lexicon,C,O](new LexiconModel(state), new LexiconView(state,uielems,public)) {
  
  def purge(lexicon : Lexicon) = {
    model.asInstanceOf[LexiconModel].purge(lexicon)
    view.remove(lexicon,panel)
  }
  view.asInstanceOf[LexiconView[C,O]].setPurge(purge)
}

class LexiconModel(state : State) extends StdModel[Lexicon] {
  private val log = Logging.getLogger(this)
  def add(lexicon : Lexicon) {}
  def remove(lexicon : Lexicon) {
    state.model removeLexicon lexicon
  }
  def purge(lexicon : Lexicon) {
    state.model purgeLexicon (lexicon,state.lingOnto)
  }
  def elements = state.model.getLexica.toSeq
}

class LexiconView[C,O<:C](state : State, val uielems : UIElems[C,O], public : Boolean)
extends HasState(state) with StdPanel[Lexicon,C,O] {
  val title = "Lexica"
  val addText = "Add lexicon"
  val removeText = "Remove lexicon"
  val description = "lemon entries are grouped into monolingual lexica. Here you can see the lexica in the current model."
  
  private var purge : Lexicon => Result = null.asInstanceOf[Lexicon => Result]
  
  def setPurge(purgeFunc : Lexicon=>Result) = purge = purgeFunc
  
  def makeElem(lexicon : Lexicon,
               remove : () => Result,
               update : Lexicon => Result,
               change : Lexicon => Unit) = {
    new LexiconElement(app,lexicon,remove,() => purge(lexicon),uielems,public)
  }
  
  def makeDisplay(lexicon : Lexicon) = uielems.html("<li>" + app.namer.displayName(lexicon) + "</li>")
  
  lazy val newLexiconWindow = new NewLexiconWindow(app,model,uielems)
  
  def dialog(lexicon : Option[Lexicon], after : Lexicon => Result) = newLexiconWindow.show(after)
}