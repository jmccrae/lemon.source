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

import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lang.Language
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.common._
import scala.collection.JavaConversions._
import eu.monnetproject.util.Logging
import java.net.URI

/**
 * A window for selecting entries
 * @author John McCrae
 */
class EntrySelectWindow[Component,Container<:Component](val uielems : UIElems[Component,Container], state : State, model : LemonModel)  extends HasState(state) {
  import uielems._
  import UIElems._
  private val log = Logging.getLogger(this)
  
  def show(after : (LexicalEntry) => Result) : Result = {
    compoundDialog("Select a lexical entry")(
      localize("Search") ->  autoSuggest("LexicalEntry",suggestEntry, EntrySerializer)
      /*tiedSelects(
          "Lexicon",
          "Lexical Entry",
          lexica,
          entries
        )*/,
      localize("New entry") -> mapForm(formElems(
        field("Canonical Form"),
        field("Sense (optional)", validator=Some(s => URI.create(s) != null))
      ), newEntry)
    )(after)
  }
  
  private def newEntry(arg : (String,String)) = {
    // Create the entry
    val newEntry = app.namer.nameEntry(model, arg._1, arg._2)
    lexicon.addEntry(newEntry);
    
    // Create the canonical form
    if(arg._1 != "" && arg._1 != null) {
      val form = lemonFactory.makeForm(app.namer.name(newEntry,"form"));
      form.setWrittenRep(new Text(arg._1,
                                  lexicon.getLanguage()));
      newEntry.setCanonicalForm(form);
    }
    
    // Create the sense
    if(arg._2 != "" && arg._2 != null) {
      val sense = lemonFactory.makeSense(app.namer.name(newEntry,"sense"));
      sense.setReference(java.net.URI.create(arg._2));
      newEntry.addSense(sense);
    }
    
    newEntry
  }
  
  private def suggestEntry(queryString : String) : Seq[LexicalEntry] = {
    LemonModels.getEntriesByFormApprox(state.model,queryString.replaceAll("\"","\\\\\""))
  }
         
  private def lexica = (model.getLexica map {
    lexicon : Lexicon => (lexicon,app.namer.displayName(lexicon))
  }).toSeq.sortBy(_._2)
  
  
 // private def entries(lexicon : Lexicon) = (lexicon.getEntrys map {
   // entry => (entry,app.namer.displayName(entry))
  //}).toSeq.sortBy(_._2)
  
  private object EntrySerializer extends Serializer[LexicalEntry] {
    def display(entry : LexicalEntry) = app.namer.displayName(entry)
    def toString(entry : LexicalEntry) = entry.getURI.toString
    def fromString(str : String) = lemonFactory.makeLexicalEntry(URI.create(str))
  }
}

