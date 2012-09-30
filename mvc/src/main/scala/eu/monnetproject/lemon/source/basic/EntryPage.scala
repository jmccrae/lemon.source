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

import eu.monnetproject.data._
import eu.monnetproject.lang.Language
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.journal._
import eu.monnetproject.lemon.source.relations._
import eu.monnetproject.lemon.source.syntax._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.basic._
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.util.Logging
import java.net.{URI,URLDecoder}
import java.io._
import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer

/**
 * This is the standard lexical entry view
 * @author John McCrae
 */
class EntryPage[C,O<:C](val app : LemonEditorApp, entry : LexicalEntry, language : Language, 
                        lexicon2 : Lexicon, model2 : LemonModel, lingOnto2 : LinguisticOntology, 
                        uielems : UIElems[C,O], public : Boolean) {
  import uielems._
  import UIElems._
  
  
  private val log = Logging.getLogger(this)
  
  
  if(entry.getCanonicalForm == null) {
    log.info("Entry has no canonical form")
  } else if(entry.getCanonicalForm.getWrittenRep == null) {
    log.info("Entry has can form but not written rep")
  } else {
    log.info("entry: " + entry.getCanonicalForm.getWrittenRep.value)
  }
  // The state
  val state = app.state(Some(lexicon2),model2)
  lazy val prevEntry : Option[LexicalEntry] = try {
    val iter = model2.query(classOf[LexicalEntry], 
                            "select ?entry { <"+lexicon2.getURI+"> <"+LemonModel.LEMON_URI +"entry> ?entry . " +
                            " filter( str(?entry) < \"" + entry.getURI + "\") } order by desc(?entry) limit 1"
    )
    if(iter.hasNext) {
      Some(iter.next)
    } else {
      None
    }
  } catch {
    case x : Exception => {
        x.printStackTrace()
        None
    }
  }
  lazy val nextEntry : Option[LexicalEntry] = try {
    log.info("select ?entry { <"+lexicon2.getURI+"> <"+LemonModel.LEMON_URI +"entry> ?entry . " +
                           " filter( str(?entry) > \"" + entry.getURI + "\") } order by ?entry limit 1")
    val iter =model2.query(classOf[LexicalEntry], 
                           "select ?entry { <"+lexicon2.getURI+"> <"+LemonModel.LEMON_URI +"entry> ?entry . " +
                           " filter( str(?entry) > \"" + entry.getURI + "\") } order by ?entry limit 1"
    )
    if(iter.hasNext) {
      Some(iter.next)
    } else {
      None
    }
  } catch {
    case x : Exception => {
        x.printStackTrace()
        None
    }
  }
  //lazy val entriesOrdered : Buffer[LexicalEntry] = LemonModels.getEntriesAlphabetic(model2,lexicon2,0,0)
  
  def prev(entry : LexicalEntry) = prevEntry
  
  def next(entry : LexicalEntry) = nextEntry
  
  def hasPrev(entry : LexicalEntry) = prevEntry != None
  def hasNext(entry : LexicalEntry) = nextEntry != None
  
  private def doNav(entry : Option[LexicalEntry]) = entry match {
    case Some(e) => app.navigator.entry(e, lexicon2)
    case None => NoResult
  }
  
  private def doRdf = {
    app.navigator.entryRDF(entry, lexicon2)
  }
  
  private def doDel = {
    prompt() delete (app,{ 
        () => {
          lexicon2 removeEntry entry
          prev(entry) match {
            case Some(entry2) => app.navigator entry (entry2,lexicon2)
            case None => next(entry) match {
                case Some(entry2) => app.navigator entry (entry2,lexicon2)
                case None => app.navigator.welcome ; NoResult
              }
          }
        }
      })
  }
  
  val changes = new ChangelogPage(state,app,entry,uielems)
  val talk = new TalkPage(state,app,entry,uielems)
  
  def mainContainer(editable : Boolean) = 
    vertical(widthPercent=100) (
      border() (
        BorderLocation.LEFT -> spacer(widthPx=10),
        BorderLocation.MIDDLE -> horizontal() (
          label(nolocalize(URLDecoder.decode(app.namer.displayName(entry),"UTF-8")),style=Some("entry_title")),
          (if(state.privacy && editable) { new StatusView(state,entry,uielems).button } else { spacer() })
        ),
        BorderLocation.RIGHT -> (if(public) {
            horizontal() (
              button("<<",enabled=hasPrev(entry)) { 
                doNav(prev(entry))
              },
              imageButtonWithLink("rdf_flyer.png",entry.getURI.getPath + ".rdf") {
                doRdf
              },
              imageButtonWithLink("icon_turtle.gif",entry.getURI.getPath + ".ttl") {
                doRdf
              },
              button(">>",enabled=hasNext(entry)) {
                doNav(next(entry))
              }
            )
          } else {
            horizontal() (
              button("<<",enabled=hasPrev(entry)) { 
                doNav(prev(entry))
              },
              imageButtonWithLink("rdf_flyer.png",entry.getURI.getPath + ".rdf") {
                doRdf
              },
              imageButtonWithLink("icon_turtle.gif",entry.getURI.getPath + ".ttl") {
                doRdf
              },
              button(">>",enabled=hasNext(entry)) {
                doNav(next(entry))
              },
              button("x",style=Some("delbutton")) {
                doDel
              }
            )
          })
      ),
      vertical()(
        if(editable) {
          new LexiconMemberController[C,O](state,entry,uielems).panel
        } else {
          new LexiconMemberController[C,O](state,entry,uielems).display
        },
        if(editable) {
          new PropertyController[LexicalEntry,C,O](state,entry,uielems,URI.create(LemonModel.LEMON_URI+"LexicalEntry")).panel
        } else {
          new PropertyController[LexicalEntry,C,O](state,entry,uielems,URI.create(LemonModel.LEMON_URI+"LexicalEntry")).display
        },
        if(editable) {
          new FormController[C,O](state,entry,uielems).panel
        } else {
          new FormController[C,O](state,entry,uielems).display
        },
        if(editable) {
          new SenseController[C,O](state,entry,uielems).panel
        } else {
          new SenseController[C,O](state,entry,uielems).display
        },
        if(editable) {
          new LexicalVariantController[C,O](state,entry,uielems).panel
        } else {
          new LexicalVariantController[C,O](state,entry,uielems).display
        },
        if(editable) {
          new ComponentController[C,O](state,entry,uielems).panel
        } else {
          new ComponentController[C,O](state,entry,uielems).display
        },
        if(editable) {
          new FrameController[C,O](state,entry,uielems).panel
        } else {
          new FrameController[C,O](state,entry,uielems).display
        },
        if(editable) {
          new TreeController(state,entry,uielems).panel
        } else {
          new TreeController(state,entry,uielems).display
        }
      )
    )
    
  
  def container(section : String = "Entry") = tabbedFrame(section)(
    "Entry" -> localize("Entry") -> mainContainer(false),
    "Edit" -> localize("Edit") -> mainContainer(true),
    "Changes" -> localize("Changes") -> changes.page,
    "Talk" -> localize("Talk") -> talk.page
  )
}

