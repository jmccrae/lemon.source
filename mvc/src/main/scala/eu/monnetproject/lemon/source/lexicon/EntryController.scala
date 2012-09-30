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
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.common._
import scala.math._
import scala.collection.JavaConversions._
import java.net.URI
import eu.monnetproject.util.Logging

/** The controler for lexical entry views
 * @author John McCrae
 */
class EntryController[C,O<:C](state : State, lexicon : Lexicon, uielems : UIElems[C,O],public : Boolean)
extends PagedController(20,new EntryModel(state.model,lexicon), new EntryView(state,uielems,public)) {
  override def add() = {
    view.dialog(None, { elem => 
        model.add(elem)
        view.add(elem,panel,() => remove(elem), update, elem2 => change(elem,elem2))
        state.app.navigator.entry(elem,lexicon,false)
      })
  }
}

class EntryModel(model : LemonModel,lexicon : Lexicon) extends PagedModel[LexicalEntry] {
  private val log = Logging.getLogger(this)
  def add(entry : LexicalEntry) = lexicon.addEntry(entry)
  def remove(entry : LexicalEntry) = {
    lexicon.removeEntry(entry)
  }
  def elements = lexicon.getEntrys.toList.sortBy(entry => entry.getURI.toString)
  def elements(offset : Int, step : Int) = {
    val time = System.currentTimeMillis
    val elems = LemonModels.getEntriesAlphabetic(model,lexicon,offset,step)
    elems.toSeq
  }
  
  private lazy val maxEntrys = lexicon.countEntrys()
  
  def max = maxEntrys
}

class EntryView[C,O<:C](state : State, val uielems : UIElems[C,O], public : Boolean)
extends HasState(state) with PagedPanel[LexicalEntry,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Lexical entries"
  val addText = "Add entry"
  val removeText = "Remove entry"
  val description = "The entry is the primary unit of a lemon lexicon. It may be a single word, a short phrase or a part of a word (affix)."
  
  def makeElem(entry : LexicalEntry,
               remove : () => Result,
               update : LexicalEntry => Result,
               change : LexicalEntry => Unit) = {
    new StdElement(Nil,remove,uielems,public) {
      override def cssClass = Some("lexicon_entry")
      override def main = List(
        button(
          nolocalize(app.namer.displayName(entry)) , style=Some("button_link")
        ) {
          app.navigator.entry(entry, lexicon, true)
        }
      )
    }
  }
  
  def makeDisplay(entry : LexicalEntry) = html("<li>"+app.namer.displayName(entry)+"</li>")
  
  lazy val newEntryWindow = new NewEntryWindow(app,
    app.namer.displayName(lexicon),
    model,lemonFactory,lexicon,uielems)
  
  def dialog(entry : Option[LexicalEntry], after : LexicalEntry => Result) = {
    newEntryWindow.show(after)
  }
}