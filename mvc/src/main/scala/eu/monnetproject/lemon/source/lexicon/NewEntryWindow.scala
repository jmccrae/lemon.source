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

import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lang.Language;
import java.net.URI;
import java.net.URLEncoder._


/**
 * The window for adding new lexical entries
 * @author John McCrae
 */
class NewEntryWindow[C,O<:C](val app : LemonEditorApp, lexiconName : String, 
                             model : LemonModel, 
                             lemonFactory : LemonFactory, lexicon : Lexicon, uielems : UIElems[C,O]) {
   
  import uielems._
  import UIElems._
  
  def show(after : (LexicalEntry) => Result) : Result = {
    prompt().show(app, "Use lemon patterns", "Do you want to use lemon patterns?",
    { 
      case false => dialog("Add a new lexical entry", formElems(
                      field("Canonical Form"),
                      field("Sense (optional)", validator=Some(s => URI.create(s) != null))
                    ))  {
      case (form,sense) => {
          makeElement(form,sense) match {
            case Some(x) => after(x)
            case None => NoResult
          }
        }
      }
      case true => compoundDialog("Add a new lexical entry using a pattern")(
         localize("Name") -> mapForm(field("Canonical Form"),field("Sense URI"), { (x:String,y:String) => x+y}),
         localize("Noun") -> mapForm(field("Canonical Form"),oneOfForm(
            localize("Class Noun") -> field("Sense URI"),
            localize("Relational Noun") -> field("Property URI"),
            localize("Multivariant Relational Noun") -> mapForm(multipleElems(
               mapForm(field("Property"),field("as"), { (x:String,y:String) => x+y})
            ), { x : Seq[String] => x.mkString(" ") }),
            localize("Class+Relational Noun") -> mapForm(field("Class Sense URI"),field("Property Sense URI"),
                                 { (x:String,y:String) => x+y})
            ), { (x:String,y:String) => x+y}),
         localize("Verb") -> field(),
         localize("Adjective") -> field(),
         localize("Enter expression") -> field()
         ) {
           case s => makeElement(s,s) match {
             case Some(x) => after(x)
             case None => NoResult
           }
         }
    })
  }
  
  def wrap(vals : List[String]) = {
    // Create the entry
    val newEntry = app.namer.nameEntry(model, vals(0), vals(1))
    lexicon.addEntry(newEntry);
    
    // Create the canonical form
    if(vals.length > 1 && vals(1).length > 0) {
      val form = lemonFactory.makeForm(app.namer.name(model,vals(0),vals(1),"form"));
      form.setWrittenRep(new Text(vals(1),
                                  lexicon.getLanguage()));
      newEntry.setCanonicalForm(form);
    }
    
    // Create the sense
    if(vals.length > 2 && vals(2).length > 0) {
      val sense = lemonFactory.makeSense(app.namer.name(model,vals(0),vals(1),"sense"));
      sense.setReference(java.net.URI.create(vals(2)));
      newEntry.addSense(sense);
    }
    
    Some(newEntry)
  }
  
  /** Make the specific element */
  def makeElement(form : String, sense : String) : Option[LexicalEntry] = {
    wrap(List(lexiconName,
                         form,
                         sense))
  }
}
