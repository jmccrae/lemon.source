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
package eu.monnetproject.lemon.source.web

import eu.monnetproject.lang.Language
import eu.monnetproject.lemon.source.app._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.basic._
import eu.monnetproject.lemon.source.lexicon._
import eu.monnetproject.lemon.source.search._
import eu.monnetproject.lemon.source.users.UserPage
import eu.monnetproject.lemon.source.web.html.StaticEntryPage
import eu.monnetproject.lemon.source.web.html.Disambiguation
import eu.monnetproject.lemon.LemonModels
import eu.monnetproject.lemon.model.{Node=>_,_}
import eu.monnetproject.util._
import java.io.File
import java.net._
import javax.servlet.http.HttpServletResponse
import scala.math._
import scala.xml._
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class Navigator(app : WebLemonEditor) extends AbstractNavigator {
  private val log = Logging.getLogger(this)
  private var callHandlers = Map[(String,Option[String]),() => Result]()
  private var fieldHandlers = Map[String,(String) => Result]()
  private var submitHandlers = Map[String,SubmitHandlerEntry[_]]()
  private var uploadHandlers = Map[String,File => Unit]()
  private var oneTimeTags = Map[String,NodeSeq]()
  
  private val PUBLIC_LEXICA_FRAG = "/Special:PublicLexica"
  private val PRIVATE_LEXICA_FRAG = "/Special:PrivateLexica"
  private val GROUPS_FRAG = "/Special:Groups"
  private val TEST_LOGIN = "/Special:TestLogin"
  private val ACCOUNT_FRAG = "/Special:Account"
  private val ONTOLOGIES_PUBLIC = "/Ontology:AllPublic"
  private val LOGIN_VERIFY = "/Login:Verify"
  private val loginVerify = (LOGIN_VERIFY+"\\?(.*)").r
  private val SEARCH_FRAG = "/Special:Search"
  private val entryFrag = """/([^/]*)/(.*)"""r
  private val lexiconFrag = """/(.*)""".r
  
  def handlePath(path : String, params : Map[String,String]) : Option[NodeSeq] = {
    path match {
      case PUBLIC_LEXICA_FRAG => Some(publicLexica(false))
      case PRIVATE_LEXICA_FRAG => myLexica(false)
      case ACCOUNT_FRAG => account(false)
      case SEARCH_FRAG => search(params.getOrElse("query",""),false)
      case entryFrag(lexiconName,entryName) => {
          val model = app.privateModel.getOrElse(app.publicModel)
          val entry = model.getFactory.makeLexicalEntry(URI.create(app.deployPrefix + "/" + lexiconName + "/" + URLEncoder.encode(entryName.replaceAll("\\+"," "),"UTF-8")))
          if(entry.getCanonicalForm == null) {
            disambiguation(entry.getURI(),"http://localhost:8000/sparql/")
          } else {
//          app.namer.entryForName(URLDecoder.decode(lexiconName,"UTF-8"),URLDecoder.decode(entryName,"UTF-8")) match {
//            case Some(entry2) => {
            app.namer.lexiconForName(lexiconName) match {
              case Some(lexicon) => Some(handleEntry(entry,lexicon,action=params.get("action")))
              case None => log.warning("Lexicon not found :" + lexiconName) ; None
            }
          }
//              }
//            case None => log.warning("Entry not found:" + entryName) ; None
//          }
        }
      case lexiconFrag(lexiconName) => {
          val model = app.privateModel.getOrElse(app.publicModel)
          app.namer.lexiconForName(URLDecoder.decode(lexiconName,"UTF-8")) match {
            case Some(lexicon) => Some(handleLexicon(lexicon))
            case None => None
          }
        }
      case _ => None
    }
  }
  
  def handleDownloadRDF(lexiconName : String, resp : HttpServletResponse, mimeType : String) = {
    val model = app.privateModel.getOrElse(app.publicModel)
    app.namer.lexiconForName(URLDecoder.decode(lexiconName,"UTF-8")) match {
      case Some(lexicon) => {
          resp.setContentType(mimeType)
          app.lemonSerializer.writeLexicon(app.trueModel(model), lexicon, app.lingOnto, resp.getWriter)
        }
      case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
    }
  }
  
  def handleRDF(path : String, resp : HttpServletResponse, mimeType : String) = {
    path match {
      case entryFrag(lexiconName,entryName) => {
          val model =app.privateModel.getOrElse(app.publicModel)
          val entry2 = model.getFactory.makeLexicalEntry(URI.create(app.deployPrefix + "/" + lexiconName + "/" + URLEncoder.encode(entryName.replaceAll("\\+"," "),"UTF-8")))
          if(entry2.getCanonicalForm() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND)
          } else {
//          app.namer.entryForName(URLDecoder.decode(lexiconName,"UTF-8"),URLDecoder.decode(entryName,"UTF-8")) match {
//            case Some(entry2) => {
            app.namer.lexiconForName(URLDecoder.decode(lexiconName,"UTF-8")) match {
              case Some(lexicon) => {
                  resp.setContentType(mimeType)
                  app.lemonSerializer.writeEntry(app.trueModel(model), entry2, app.lingOnto, resp.getWriter,mimeType=="application/rdf+xml")
                  resp.setStatus(HttpServletResponse.SC_OK)
                } 
              case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
            }
          }
//              }
//            case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
//          }
        }
      case lexiconFrag(lexiconName) => {
          val model = app.privateModel.getOrElse(app.publicModel)
          app.namer.lexiconForName(URLDecoder.decode(lexiconName,"UTF-8")) match {
            case Some(lexicon) => {
                resp.setContentType(mimeType)
                app.lemonSerializer.writeLexiconDescription(app.trueModel(model), lexicon, resp.getWriter)
              }
            case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
          }
          
        }
      case _ => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
    }
  }
  
  private val random = new java.util.Random()
  
  def handleResult(result : Result, resp : HttpServletResponse) {
    result match {
      case result => {
          val cid = "c" + abs(random.nextLong)
          def callId(c : String) : Stream[String] = Stream.cons(c,callId(c+"x"))
          val (script,callBacks,_) = ResultTranslator.translate(result, callId(cid),app.deployServer)
          for((callBack,callId) <- callBacks zip callId(cid)) {
            if(callBack != None) {
              oneTimeTags +=(callId -> callBack.get)
            }
          }
          resp.setStatus(HttpServletResponse.SC_OK)
          resp.setContentType("application/javascript")
          resp.getWriter.println(script)
          SourceWebServlet.transLog("script: " +script)
        }
    }
    
  }
  
  def noHandler(id : String, resp : HttpServletResponse) {
    SourceWebServlet.transLog("No handler for "+ id)
    resp.setStatus(HttpServletResponse.SC_OK)
    resp.setContentType("application/javascript")
    resp.getWriter.println("sessionExpired();")
  }
  
  def badRequest(resp : HttpServletResponse) {
    SourceWebServlet.transLog("Bad request")
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST)
  }
  
  def handleCall(params : Map[String,String], resp : HttpServletResponse) {
    params.get("id") match {
      case Some(id) => {
          callHandlers.get((id,params.get("action"))) match {
            case Some(handler) => handleResult(handler(),resp)
            case None => {
                params.get("action") match {
                  case Some(action) => SpecialHandlers.get(id,action,app) match {
                      case Some(result) => handleResult(result,resp)
                      case None => noHandler(id,resp)
                    }
                  case None => SpecialHandlers.get(id,app) match {
                      case Some(result) => handleResult(result,resp)
                      case None => noHandler(id,resp)
                    }
                } 
              }
          }
        }
      case None => badRequest(resp)
    }
  }
  
  def handleFieldCall(params : Map[String,String], resp : HttpServletResponse) {
    params.get("id") match {
      case Some(id) => fieldHandlers.get(id) match {
          case Some(handler) => params.get("value") match {
              case Some(v) => handleResult(handler(v),resp)
              case None => badRequest(resp)
            }
          case None => {
              noHandler(id,resp)
            }
        }
      case None => {
          badRequest(resp)
        }
    }
  }
  
  def handleSubmit(params : Map[String,String], resp : HttpServletResponse) {
    params.get("id") match {
      case Some(id) => submitHandlers.get(id) match {
          case Some(handler) => {
              handleResult(handler.result(params),resp)
            }
          case None => {
              noHandler(id,resp)
            }
        }
      case None => {
          badRequest(resp)
        }
    }
    
  }
  
  def handleOneTime(id : String, resp : HttpServletResponse) {
    oneTimeTags.get(id) match {
      case Some(result) => {
          resp.setStatus(HttpServletResponse.SC_OK)
          resp.setContentType("text/html")
          resp.getWriter.append(result.toString)
          oneTimeTags -= id
        }
      case None => {
          resp.setStatus(HttpServletResponse.SC_OK)
          resp.setContentType("text/html")
          resp.getWriter.print("")
        }
    }
  }
  
  def handleElemCall(id : Option[String], value : Option[String], resp : HttpServletResponse) {
    if(id == None || value == None) {
      badRequest(resp)
    } else  {
      fieldHandlers.get(id.get) match {
        case Some(handler) => {
            val result = handler(value.get)
            handleResult(result,resp)
          }
        case None => {
            noHandler(id.get,resp)
          }
      }
    }
  }
  
  def handleUpload(files : Map[String,File],resp : HttpServletResponse) {
    for((id,file) <- files) {
      uploadHandlers.get(id) match {
        case Some(handler) => {
            handler(file)
          }
        case None => {
            noHandler(id,resp)
            return;
          }
      }
    }
    resp.setStatus(HttpServletResponse.SC_OK)
  }
  
  def addHandler(id : String, action : Option[String], function : => Result) {
    callHandlers += ((id,action) -> { () => function})
  }
  
  def addFieldHandler(id : String, function : (String) => Result) {
    fieldHandlers += (id -> function)
  }
  
  def addSubmitHandler[E](id : String, extractor : FormElemExtractor[E], function : E => Result) {
    submitHandlers += (id -> SubmitHandlerEntry[E](extractor,function))
  }
  
  def addUploadHandler[E](id : String, function : File => Unit) {
    uploadHandlers += (id -> function)
  }
  
  def entry(entry : LexicalEntry, lexicon : Lexicon, animate : Boolean = true) : SendRedirectResult = {
    new SendRedirectResult(entry.getURI.toString)
  }
  
  def lexicon(lexicon : Lexicon, animate : Boolean = true) : SendRedirectResult = {
    new SendRedirectResult(lexicon.getURI.toString)
  }
  
  def entryRDF(entry : LexicalEntry, lexicon : Lexicon) : Result = {
    val lexiconName = URLEncoder.encode(app.namer.displayName(lexicon),"UTF-8")
    val entryName = URLDecoder.decode(app.namer.displayName(entry),"UTF-8")
    new SendRedirectResult(SourceWebServlet.path + "/" + lexiconName + "/" + entryName+"?rdf")
  }
  
  def lexiconRDF(lexicon : Lexicon) : Result = {
    val lexiconName = URLEncoder.encode(app.namer.displayName(lexicon),"UTF-8")
    new SendRedirectResult(SourceWebServlet.path + "/" + lexiconName + "?rdf")
  }
  
  def downloadRDF(lexicon : Lexicon) : Result = {
    val lexiconName = URLEncoder.encode(app.namer.displayName(lexicon),"UTF-8")
    new SendRedirectResult(SourceWebServlet.path + "/Special:Download/" + lexiconName + "?rdf")
  }
  
  def handleEntry(entry : LexicalEntry, lexicon : Lexicon, animate : Boolean = true, action : Option[String]) : NodeSeq = {
    val model = lexicon.getModel
    val lexiconName = URLEncoder.encode(app.namer.displayName(lexicon),"UTF-8")
    val entryName = URLDecoder.decode(app.namer.displayName(entry),"UTF-8")
    val basicTab = new EntryPage(app,entry,Language.get(lexicon.getLanguage()),lexicon,model,app.lingOnto, app.uielems,model eq app.trueModel(app.publicModel))
    action match {
      case Some("Entry") => StaticEntryPage.makePage(entry,app.l10n,model)
      case Some(act) => basicTab.container(act).html
      case None => StaticEntryPage.makePage(entry,app.l10n,model)
//basicTab.container().html
    }
  }
  
  def handleLexicon(lexicon : Lexicon) : NodeSeq = {
    val model = lexicon.getModel
    val lexiconName = URLEncoder.encode(app.namer.displayName(lexicon),"UTF-8")
    val lexiconPage = new SingleLexiconPage(app,model,lexicon,app.uielems,model eq app.trueModel(app.publicModel))
    lexiconPage.panel.html 
  }
  
  def welcome = new SendRedirectResult(SourceWebServlet.path)
  
  def publicLexica(animate : Boolean = true) = {
    val model = app.publicModel
    val lexiconPage = new LexiconPage(app,model,app.l10n("Public lexica"), app.uielems,true)
    lexiconPage.panel.html
  }
  
  def myLexica(animate : Boolean = true) : Option[Node] = {
    if(app.privateModel == None) {
      None
    } else {
      val model = app.privateModel.getOrElse(throw new RuntimeException())
      val lexiconPage = new LexiconPage(app,model,app.l10n("My lexica"), app.uielems,false)
      Option(lexiconPage.panel.html)
    }
  }
  
  def account(animate : Boolean = true) : Option[Node] = {
    app.userID match {
      case Some(user) => {
          val userPage = new UserPage(user,app.userDB,app.uielems)
          Option(userPage.page.html)
        }
      case None => None
    }
  }
  
  def search(query : String, animate : Boolean = true) : Option[Node] = {
    val model = app.privateModel match {
      case Some(m) => m
      case None => app.publicModel
    }
    val searchPage = new SearchResultPage(app.state(None, model),query,app.uielems)
    Some(searchPage.panel.html)
  }
  
  def disambiguation(uri : URI, queryEndpoint : String) : Option[Node] = {
    val privateContext = app.privateModel.map { 
      pm => pm.getContext
    }
    val results : List[AnyRef] = SPARQLResolver.sparqlProp(uri, URI.create("http://www.w3.org/2000/01/rdf-schema#seeAlso"), 
                                                           List(URI.create(app.deployPrefix +"public")) ++ privateContext, queryEndpoint)

    if(results.isEmpty) {
      None
    } else {
      Some(Disambiguation.page(results,app.l10n))
    }
  }
  
  private case class SubmitHandlerEntry[E](val extractor : FormElemExtractor[E], val action : E => Result) {
    def result(params : Map[String,String]) = {
      extractor.extract(params) match {
        case Some(e) => action(e)
        case None => ErrorResult("Could not understand input")
      }
    }
  }
}

class ServletResponseTarget(response : HttpServletResponse) extends eu.monnetproject.data.DataTarget {
  def asURL = throw new UnsupportedOperationException
  def asOutputStream = response.getOutputStream
  def asWriter = response.getWriter
  def asFile = throw new UnsupportedOperationException
}
