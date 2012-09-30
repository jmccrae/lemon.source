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

import eu.monnetproject.data._
import eu.monnetproject.lang.Language
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.OKResult
import eu.monnetproject.lemon.source.common._
import java.io._


/**
 * 
 * @author John McCrae
 */

class LexiconElement[C,O<:C](app : LemonEditorApp, lexicon : Lexicon, remove : () => Result, purge : () => Result, uielems : UIElems[C,O], hideDelete : Boolean) 
extends StdElement[C,O](Nil,remove,uielems,hideDelete) {
                     
  import uielems._
  import UIElems._
  
  protected override def cssClass = Some("lexicon_entry")
  
  protected override def main = {
    val  lexiconName = app.namer.displayName(lexicon)
    val language = lexicon.getLanguage
    val lang = Language.get(language)
    val flag = LangFlagMapper.flagForLang(lang).getOrElse("und")
    (button(nolocalize(lexiconName),style=Some("button_link")) { 
        app.navigator.lexicon(lexicon, true)
      }) ::
    (horizontal()(
        imageButton("flags/"+flag+".gif"){ OKResult },
        label(nolocalize(lang.getName))
      )) :: Nil
  }
    
  protected override def actions = if(!hideDelete) {
    (imageButtonWithLink("rdf_flyer.png",lexicon.getURI + ".rdf") {
      app.navigator.lexiconRDF(lexicon)
    }) ::
  (imageButtonWithLink("download.png",lexicon.getURI.toString.replace("/lemonsource", "/lemonsource/Special:Download")) {
      app.navigator.downloadRDF(lexicon)
    }) ::
    button(nolocalize("\u00d7")) {
      prompt() delete (app, () => 
        {
          prompt().show(app, "Delete all entries?", "Do you also want to delete all entries in this lexicon?", {
            case true => purge()
            case false => remove()
          })
        })
    } :: Nil
  } else {
    imageButtonWithLink("rdf_flyer.png",lexicon.getURI + ".rdf") {
      app.navigator.lexiconRDF(lexicon)
    } :: Nil
  }
        
}

