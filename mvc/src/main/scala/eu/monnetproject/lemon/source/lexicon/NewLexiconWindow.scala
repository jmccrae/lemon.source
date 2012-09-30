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

import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lang._

/**
 * The window for adding new lexica
 * @author John McCrae
 */
class NewLexiconWindow[C,O<:C](val app : LemonEditorApp, model : LemonModel, uielems : UIElems[C,O]) {
  import uielems._
  import UIElems._
  
  def show(after : (Lexicon) => Result) : Result = {
    dialog("New lexicon", formElems(
        field("Lexicon Name"),
        langSelect()
      )
    ) {
      case (lexiconName, language) => makeElement(lexiconName,language.getOrElse(Language.ENGLISH)) match {
          case Some(lexicon) => after(lexicon)
          case None => NoResult
      }
    }
  }
  
  private def wrap(vals : List[String]) = vals match {
    case List(lexiconName,language) => {
        val lang = Language.get(language)
        Option(app.namer.nameLexicon(model, lexiconName, lang))
      }
    case _ => None
  }
  
  def makeElement(lexiconName : String, language : Language) : Option[Lexicon] = {
    wrap(List(lexiconName,language.toString))
  }
  /*
  val lexiconURI = textField(app,
                             "Lexicon Name",
                             "Specify the name of this lexicon.",
                             "NewLexicon",
                             Some({ (s : String) => new URI(s); true }));
  
  private var langName2ISO = Map[String,String]()
    
  val language = new Select("Language");
  for(lang <- LangFlagMapper.languages.toList.sortBy(_.getName())) {
    language.addItem(lang.getName())
    LangFlagMapper.flagForLang(lang) match {
      case Some(x) => language.setItemIcon(lang.getName(), new ThemeResource("flags/"+ x + ".gif"))
      case None =>
    }
    langName2ISO += (lang.getName() -> lang.getIso639_1())
  }
  language addValidator (new AbstractStringValidator(app.l10n("Invalid Value")) {
      def isValidString(s : String) : Boolean = {
        try { 
          langName2ISO.contains(s) || ({ Language.get(s) ; true })
        } catch {
          case x : LanguageCodeFormatException => setErrorMessage(x.getMessage()) ; false
        }
      }
    })
  language setNewItemsAllowed true
  language select "English"
    
  
  /* textField(app,
   "Language (ISO code)",
   "Specify the language as a two or three letter code. (English is 'en')",
   "",
   Some({ (s : String) =>  Language.get(s);true }));*/
  
  def addComponents(elem : Option[Lexicon], container : ComponentContainer) {
    container.addComponent(lexiconURI);
    container.addComponent(language);
  } 
  
  def title = app.l10n("New lexicon")*/
  
}
