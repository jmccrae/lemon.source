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

import java.io._
import eu.monnetproject.data._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.lexicon._


/**
 * The page that shows available lexica
 * @author John McCrae
 */
class SingleLexiconPage[C,O<:C](val app : LemonEditorApp, model : LemonModel, lexicon : Lexicon, uielems : UIElems[C,O], public : Boolean) {
  import uielems._
  import UIElems._
  val panel = frame()(
    border() (
      BorderLocation.MIDDLE -> label(app.namer.displayName(lexicon),style=Some("entry_title")),
      BorderLocation.RIGHT -> (if(public) {
        horizontal() (
          imageButtonWithLink("rdf_flyer.png",lexicon.getURI + ".rdf") {
            doRdf
          }
        )
      } else {
        horizontal() (
          imageButtonWithLink("rdf_flyer.png",lexicon.getURI + ".rdf") {
            doRdf
          },
          button("x",style=Some("delbutton")) {
            doDel
          }
        )
      })
    ),
    vertical()(
      new EntryController(app.state(Some(lexicon),model),lexicon,uielems,public).panel
    )
  )
  
  private def doRdf = {
    app.navigator.lexiconRDF(lexicon)
  }
  
  private def doDel = {
    prompt() delete (app, () => 
      {
        prompt() show (app, "Delete all entries?", "Do you also want to delete all entries in this lexicon?", {
            case true => model.purgeLexicon(lexicon,app.lingOnto) ; app.navigator.welcome
            case false => model.removeLexicon(lexicon) ; app.navigator.welcome
          })
      })
  }
  
}
