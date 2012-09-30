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
package eu.monnetproject.lemon.source.search

import java.net.URI
import eu.monnetproject.lang.Language
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.util.Logging
import scala.collection.JavaConversions._

case class SearchResult(val entry : LexicalEntry)

/**
 * Controller for search results
 * @author John McCrae
 */
class SearchResultController[C,O<:C](state : State, queryString : String, uielems : UIElems[C,O])
extends StdController[SearchResult,C,O](new SearchResultModel(state,queryString),new SearchResultView(state,uielems))

class SearchResultModel(state : State, queryString : String) extends StdModel[SearchResult] {
  val log = Logging.getLogger(this)
  
  def add(result : SearchResult) {}
  def remove(result : SearchResult) {}
  lazy val elements = {
    println("Starting search")
    val entries = LemonModels.getEntriesByForm(state.model, queryString.replaceAll("\"","\\\\\""), "en") ++
      LemonModels.getEntriesByForm(state.model, queryString.replaceAll("\"","\\\\\""), "de") ++
      LemonModels.getEntriesByForm(state.model, queryString.replaceAll("\"","\\\\\""), "fr") ++
      LemonModels.getEntriesByForm(state.model, queryString.replaceAll("\"","\\\\\""), "nl") ++
      LemonModels.getEntriesByForm(state.model, queryString.replaceAll("\"","\\\\\""), "es") ++
      LemonModels.getEntriesByForm(state.model, queryString.replaceAll("\"","\\\\\""), "ja")
    //val entries = LemonModels.getEntriesByFormApprox(state.model,queryString.replaceAll("\"","\\\\\""))
    println("Done searching")
    (entries map {
      entry => {
        SearchResult(entry)
//        log.info(entry.getURI.toString)
//        LemonModels.getLexicaByEntry(state.model,entry) map {
//          lexicon => {
//            val ll = lexicon.getLanguage()
//            SearchResult(entry, lexicon, Language.get(if(ll == null) { "und" } else { ll }))
//          }
//        }
      }
    }).toSeq
  }
}

class SearchResultView[C,O<:C](state : State, val uielems : UIElems[C,O])
extends HasState(state) with StdPanel[SearchResult,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Search Results"
  val addText = ""
  val removeText = ""
  val description = "The results of a search; click on a lexical entry to navigate there"
  
  override def canAdd = false
  
  protected def makeElem(elem : SearchResult, 
                         remove : () => Result,
                         update : SearchResult => Result,
                         change : SearchResult => Unit) = {
    new SearchResultElement(elem,remove,uielems)
  }
  
  protected def makeDisplay(elem : SearchResult) = html("")
  
  def dialog(elem : Option[SearchResult], after : SearchResult => Result) = NoResult
}
//class SearchResultController(val view : UIView[SearchResult] with State, queryString : String) 
//extends HasState(view) with GenericController[SearchResult] {
//  val title = app.l10n("Search results: ") + queryString
//  val addText = ""
//  val removeText = ""
//  

//  
//  def unwrap(result : SearchResult) = List(result.entry.getURI().getFragment(),
//  app.namer.displayName(result.lexicon), result.language.toString())
//  
//  def wrap(values : List[String]) = throw new UnsupportedOperationException()
//  
//  def load {
//    for(result <- results) {
//      view add result
//    }
//  }
//  
//  def add(result : SearchResult) = NoResult
//  
//  def remove(result : SearchResult) = NoResult
//    
//  override def addAction = None
//  override def removeAction = None
//}
//  
