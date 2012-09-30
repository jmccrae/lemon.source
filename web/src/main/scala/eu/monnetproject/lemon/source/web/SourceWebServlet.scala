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

import java.io._
import java.net.URI
import java.net.URL
import javax.servlet._
import javax.servlet.http._
import java.util.Random
import eu.monnetproject.lang._
import eu.monnetproject.lemon.LemonRepository
import eu.monnetproject.lemon.SPARQLLemonRepository
import eu.monnetproject.lemon.SPARQL
import eu.monnetproject.lemon.source.app.ErrorResult
import eu.monnetproject.lemon.source.app.User
import eu.monnetproject.lemon.source.users.UserImpl
import eu.monnetproject.lemon.source.web.html._
import eu.monnetproject.framework.services.Services
import eu.monnetproject.util._
import org.apache.commons.fileupload._
import org.apache.commons.fileupload.disk._
import org.apache.commons.fileupload.servlet._
import scala.collection.JavaConversions._
import scala.xml._
import scala.xml.dtd._

/**
 * 
 * @author John McCrae
 */
object SourceWebServlet {
  val log = Logging.getLogger(this)
  def path = "/lemonsource"
  private lazy val transOut = try {
    new PrintWriter(System.getProperty("user.home") + File.separator + "trans.log")
  } catch {
    case x : IOException => x.printStackTrace(); null 
  }
  def transLog(str : String) {
    if(transOut != null) {
      transOut.println(str)
      transOut.flush
    }
    log.info(str)
  }
}

class SourceWebServlet extends HttpServlet {
  System.setProperty("eu.monnetproject.framework.services.osgi","disabled")
  private val maintenanceMode = false
  
  val log = Logging.getLogger(this)
  def getPath = SourceWebServlet.path
  
  
  private val XHTML = DocType("html",
                              PublicID("-//W3C//DTD XHTML 1.0 Strict//EN",
                                       "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"), 
                              Nil)
  
  private val CSS = "(/css/.*)".r
  private val JS = "(/js/.*)".r
  private val IMAGES = "(/images/.*)".r
  private val GENERATED_FRAG = "/Special:LastGeneratedLexicon"
  private val SPECIAL_DOWNLOAD = "/Special:Download/(.*)".r
  private val SPECIAL_DUMP = "/Special:Dump/(.*)".r
  
  private def params(req : HttpServletRequest) : Map[String,String] = req.getParameterMap.map {
    case (x,Array(y : String)) => (x.toString,y)
    case (x,y) => (x.toString,y.toString)
  }.toMap
  
  private def makeApp(session : HttpSession, lemonRepo : LemonRepository) : WebLemonEditor = {
    log.info("Creating web lemon editor app")
          val os = Services get classOf[eu.monnetproject.ontology.OntologySerializer]
          val generatorURL = getServletConfig.getInitParameter("generator") match {
            case null => new URL("http://localhost:8080/generate")
            case url => new URL(url)
          }
          val newApp = new WebLemonEditor(
            lemonRepo,
            new eu.monnetproject.lemon.source.journal.DummyJournaller(),
            //Services get classOf[eu.monnetproject.lemon.generator.LemonGenerator],
            //   new RESTGenerator(generatorURL,os,LemonSerializer.newInstance),
            getServletConfig,
            Services get classOf[eu.monnetproject.lemon.LinguisticOntology],
            Services get classOf[eu.monnetproject.l10n.Localizer],
            Services get classOf[eu.monnetproject.lemon.oils.LemonOils],
            os
          )
          //session.setAttribute("app", newApp)
          log.info("Web lemon editor ready")
          newApp
  }
  
  private var anonApp : Option[WebLemonEditor] = None
  
  override def service(req : HttpServletRequest, resp : HttpServletResponse) {
    val staticLocations = getServletConfig().getInitParameter("static")
    val staticFile = new File(staticLocations + req.getPathInfo)
    if(req.getPathInfo.startsWith("/") && req.getPathInfo() != "/" && staticFile.exists) {
      serveFile(staticFile,resp)
      return;
    }
    if(maintenanceMode) {
      handleMaintenance(req,resp)
      return;
    }
    val lemonRepo = getServletConfig.getInitParameter("queryEndpoint") match {
      case null => new MemoryLemonRepository()
      case queryEndpoint => new SPARQLLemonRepository(queryEndpoint, 
                                                      getServletConfig.getInitParameter("updateEndpoint"),
                                                      getServletConfig.getInitParameter("updateQueryParam"),
                                                      getServletConfig.getInitParameter("username"),
                                                      getServletConfig.getInitParameter("password"),
                                                      SPARQL.valueOf(Option(getServletConfig.getInitParameter("sparqlDialect")).getOrElse("SPARUL"))
        )
    }
    val repoURI : URI = getServletConfig.getInitParameter("repoURI") match {
      case null => URI.create("memory:/")
      case x => URI.create(x)
    }
    val session = req.getSession
    val app : WebLemonEditor = session.getAttribute("app") match {
      case null => {
          anonApp.getOrElse { anonApp = Some(makeApp(session,lemonRepo)) ; anonApp.get }
        }
      case a => a.asInstanceOf[WebLemonEditor]
    }
    req.getParameter("lang") match {
      case null =>
      case l => app.lang = Language.get(l)
    }
    req.getParameter("help") match {
      case null => 
      case "off" => app.showHelp = false
      case "on" => app.showHelp = true
    }
    SourceWebServlet.transLog("req: " + req.getPathInfo + "?" + req.getQueryString)
    req.getPathInfo() match {
      case null => serveMain(app,Welcome.make(Language.ENGLISH),resp)
      case "/" => serveMain(app,Welcome.make(Language.ENGLISH),resp)
      case "/ajax_call" => app.navigator.handleCall(params(req), resp)
      case "/ajax_submit" => app.navigator.handleSubmit(params(req), resp)
      case "/ajax_field" => app.navigator.handleFieldCall(params(req), resp)
      case "/ajax_onetime" => app.navigator.handleOneTime(params(req).get("id").getOrElse(""), resp)
      case "/ajax_elem" => app.navigator.handleElemCall(params(req).get("id"),params(req).get("value"),resp)
      case GENERATED_FRAG => serveLastGenerated(app,resp)
      case SPECIAL_DOWNLOAD(lexiconName) => {
          val acceptType = getFirstAcceptType(req)
          app.navigator.handleDownloadRDF(lexiconName,resp,if(acceptType == XHTML_MIME || acceptType == HTML) { RDFXML } else { acceptType })
        }
      case SPECIAL_DUMP(fileName) => {
          if(fileName.contains("/")) {
            resp.sendError(HttpServletResponse.SC_GONE)
          } else {
            val dumpFolder = new File(getServletConfig().getInitParameter("dumps"))
            if(!dumpFolder.exists) {
              resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            } else {
              val dumpFile = new File(dumpFolder,fileName)
              if(dumpFile.exists) {
                serveFile(dumpFile,resp)
              } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND)
              }
            }
          }
          
        }
      case "/upload" => {
          val factory = new DiskFileItemFactory()
          val upload = new ServletFileUpload(factory)
          val items = upload.parseRequest(req).asInstanceOf[java.util.List[FileItem]]
          val files = (for(item <- items) yield {
              val fileName = "source_" + new Random().nextLong()
              val file = File.createTempFile(fileName, "tmp")
              file.deleteOnExit
              item.write(file)
              (item.getFieldName,file)
            }).toMap
          app.navigator.handleUpload(files, resp)
        }
        case "/ajax_auth" => {
          val userApp = makeApp(session,lemonRepo)
          SpecialHandlers.auth(userApp, req.getParameter("action")) match {
            case e : ErrorResult => {
              app.navigator.handleResult(e,resp)
            }
            case r => {
              session.setAttribute("app",userApp)
              app.navigator.handleResult(r,resp)
            }
          }
        }
        case "/ajax_newuser" => {
          val userApp = makeApp(session,lemonRepo)
          SpecialHandlers.newuser(userApp, req.getParameter("action")) match {
            case e : ErrorResult => {
              app.navigator.handleResult(e,resp)
            }
            case r => {
              session.setAttribute("app",userApp)
              app.navigator.handleResult(r,resp)
            }
          }
        }
//      case "/login" => {
//          val provider = req.getParameter("provider")
//          val redirection = logInHandler.authenticate(provider,getFullPath(req) + SourceWebServlet.path + "/login_verify")
//          resp.sendRedirect(redirection)
//        }
//      case "/login_verify" => {
//          val userID = logInHandler.verify(req.getParameterMap,req.getRequestURL.toString)
//          app.onLogIn(userID)
//          resp.sendRedirect(SourceWebServlet.path)
//        }
      case "/logout" => {
          app.onLogOut
          resp.sendRedirect(SourceWebServlet.path)
        }
      case "/poll" => {
          val (value,message) = app.getProgress
          val json = "{ \"value\": " + value + ", \"message\": \"" + message +"\"}"
          resp.setContentType("application/json")
          resp.setStatus(HttpServletResponse.SC_OK)
          resp.getWriter.print(json)
        }
      case CSS(path) => serveStatic(path,resp)
      case JS(path) => serveStatic(path,resp)
      case IMAGES(path) => serveStatic(path,resp)
      case path => {
          val acceptType = getFirstAcceptType(req)
          if((!path.endsWith(".rdf") && !path.endsWith(".ttl") && acceptType != RDFXML && acceptType != TURTLE) || path.endsWith(".html")) {
            app.navigator.handlePath(path.replaceAll("\\.html$",""),params(req)) match {
              case Some(contents) => serveMain(app,contents,resp)
              case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
            }
          } else {
            var mustLogOut = false
            if(req.getParameter("userGraph") != null) {
              app.userID match {
                case Some(user : User) => if(user.id != req.getParameter("userGraph")) {
                    app.onLogOut
                    app.onLogIn(UserImpl(req.getParameter("userGraph"),"",None,None))
                    mustLogOut = true
                  }
                case None => {
                    app.onLogIn(UserImpl(req.getParameter("userGraph"),"",None,None))
                    mustLogOut = true
                  }
              }
              app
            } else {
              app
            }
            val rdfType = if(acceptType == TURTLE || path.endsWith(".ttl")) { TURTLE } else { RDFXML }
            if(rdfType == TURTLE) {
              val staticFileTTL = new File(staticLocations + req.getPathInfo + ".ttl")
              if(staticFileTTL.exists) {
                serveFile(staticFileTTL,resp)
                return;
              }
            } else {
              val staticFileRDF = new File(staticLocations + req.getPathInfo + ".rdf")
              if(staticFileRDF.exists) {
                serveFile(staticFileRDF,resp)
                return;
              }
            }
            app.navigator.handleRDF(path.replaceAll("\\.(rdf|ttl)$",""),resp, rdfType)
            if(mustLogOut) {
              app.onLogOut
            }
          } 
        }
    }
  }
  
  private val HTML = "text/html"
  private val XHTML_MIME = "application/xhtml+xml"
  private val RDFXML = "application/rdf+xml"
  private val TURTLE = "application/x-turtle"
  private val TURTLE2 = "text/turtle"
  private val TURTLE3 = "application/rdf+turtle"
  
  private def getFirstAcceptType(req : HttpServletRequest) : String = {
    if(req.getHeader("Accept") == null) {
      return HTML;
    } else {
      for(typ <- req.getHeader("Accept").split(",")) {
        var typAct = typ
        if(typAct.indexOf(";") > 0) {
          typAct = typAct.substring(0, typAct.indexOf(";"))
        }
        typAct match {
          case RDFXML => return RDFXML
          case TURTLE => return TURTLE
          case TURTLE2 => return TURTLE
          case TURTLE3 => return TURTLE
          case XHTML_MIME => return XHTML_MIME
          case HTML => return HTML
          case _ => return HTML
        }
      }
      return HTML
    }
  }
  
  private def getFullPath(req : HttpServletRequest) = req.getScheme() + "://" + req.getServerName() + (if(req.getServerPort() != 80) {
      ":" + req.getServerPort()
    } else {
      ""
    })
  
  private def serveMain(app : WebLemonEditor, contents : NodeSeq, resp : HttpServletResponse) {
    resp.setStatus(HttpServletResponse.SC_OK)
    resp.setContentType("application/xhtml+xml")
    XML.write(resp.getWriter, Frame.make(SourceWebServlet.path,contents,app), "UTF-8", true, XHTML)
  }
  
  
  private def staticContents(page : String) = {
    val url = this.getClass.getResource(page)
    if(url == null) {
      log.warning("Resource not found: " + page)
      "RESOURCE_NOT_FOUND="+page
    } else {
      val sb = new StringBuilder()
      val in = new BufferedReader(new InputStreamReader(url.openStream,"UTF-8"))
      var s = ""
      while({s = in.readLine() ; s != null}) {
        sb.append(s+"\n");
      }
      sb.toString
    }
  }
  
  private def serveStatic(page : String, resp : HttpServletResponse) {
    val url = this.getClass.getResource(page)
    if(url == null) {
      log.warning("Resource not found: " + page)
      resp.sendError(HttpServletResponse.SC_NOT_FOUND)
    } else {
      val in = url.openStream
      resp.setStatus(HttpServletResponse.SC_OK)
      resp.setContentType(getContentType(page))
      val out = resp.getOutputStream
      val buf = new Array[Byte](1024)
      var read = 0
      while({read = in.read(buf) ; read != -1}) {
        out.write(buf,0,read)
      }
      out.flush
    }
  }
  
  private def serveFile(file : File, resp : HttpServletResponse) {
    val in = new FileInputStream(file)
    resp.setStatus(HttpServletResponse.SC_OK)
    resp.setContentType(getContentType(file.getPath))
    val out = resp.getOutputStream
    val buf = new Array[Byte](1024)
    var read = 0
    while({read = in.read(buf) ; read != -1}) {
      out.write(buf,0,read)
    }
    out.flush
  }
  
  private def serveLastGenerated(app : WebLemonEditor, resp : HttpServletResponse) {
    app.lastModel match {
      case Some(lastModel) => {
          resp.setContentType("application/rdf+xml")
          resp.getWriter.write(lastModel)
        }
      case None => {
          log.info("No generated model")
          resp.sendRedirect("/lemonsource/Special:PrivateLexica")
        }
    }
  }
  
  private def handleMaintenance(req : HttpServletRequest, resp : HttpServletResponse) {
    req.getPathInfo match {
      case null => {
          resp.setStatus(HttpServletResponse.SC_OK)
          resp.setContentType("application/xhtml+xml")
          XML.write(resp.getWriter, MaintenancePage.page("/lemonsource"), "UTF-8", true, XHTML)
        }
      case CSS(path) => serveStatic(path,resp)
      case JS(path) => serveStatic(path,resp)
      case IMAGES(path) => serveStatic(path,resp)
      case _ => {
          resp.setStatus(HttpServletResponse.SC_OK)
          resp.setContentType("application/xhtml+xml")
          XML.write(resp.getWriter, MaintenancePage.page("/lemonsource"), "UTF-8", true, XHTML)
        }
    }
  }
    
  private val HTML_TYPE = ".*\\.html".r
  private val CSS_TYPE = ".*\\.css".r
  private val JS_TYPE = ".*\\.js".r
  private val PNG_TYPE = ".*\\.png".r
  private val ZIP_TYPE = ".*\\.zip".r
  private val BZ2_TYPE = ".*\\.bz2".r
  private val TTL_TYPE = ".*\\.ttl".r
  private val RDF_TYPE = ".*\\.rdf".r
  
  private def getContentType(fileName : String) = fileName match {
    case HTML_TYPE() => "text/html"
    case CSS_TYPE() => "text/css"
    case JS_TYPE() => "application/javascript"
    case PNG_TYPE() => "image/png"
    case ZIP_TYPE() => "application/x-zip"
    case BZ2_TYPE() => "application/bzip2"
    case TTL_TYPE() => "text/turtle"
    case RDF_TYPE() => "application/rdf+xml"
    case _ => "text/plain"
  }
}
