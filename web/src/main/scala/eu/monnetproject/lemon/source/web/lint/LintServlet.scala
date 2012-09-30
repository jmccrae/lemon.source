package eu.monnetproject.lemon.source.web.lint

import eu.monnetproject.lemon.source.web._
import java.io._
import java.net._
import java.util.regex._
import javax.servlet.http._
import org.apache.http.client._
import org.apache.http.impl.client._
import org.apache.http.client.methods._
import org.apache.commons.lang._
import scala.io._
import scala.xml._
import scala.collection.mutable.ListBuffer

class LintServlet extends HttpServlet {

  def getPath = "/lemonsource-lint"
  
  private val httpClient = new DefaultHttpClient()
  
  private var pathsToProcess = ListBuffer[URL]()
  private var pathsProcessed = ListBuffer[URL]()
    
  override def service(req : HttpServletRequest, resp : HttpServletResponse) {  
    if ((req.getRemoteAddr().equals("127.0.0.1") || req.getRemoteAddr().equals("0:0:0:0:0:0:0:1"))) {
      val out = resp.getWriter
      if(req.getPathInfo == "/next") {
        printHeader(out)
    
        if(!pathsToProcess.isEmpty) {
          val current = pathsToProcess.head
          try {
            val page = openPage(current)
            val (links,calls) = processPage(page,current,req)
            out.println("<div class='lint_ok'>Page OK: " + current+"</div><ul>")
            for((call,callXML) <- calls) {
              out.println(makeCall(req,current,call,callXML))
            }
            out.println("</ul>")
            for(link <- links) {
              enqueue(link)
            }
          } catch {
            case x : Exception => {
                x.printStackTrace()
                out.println("<div class='lint_err'>Error: " + current + "(" + x.getClass.getName + " " + x.getMessage+")</div>")
              }
          }
          pathsProcessed :+= current
          pathsToProcess -= current
          printFooter(out,req.getParameter("id").toString.toInt)
        } else {
          out println("<div>Finished</div>")
          pathsToProcess = ListBuffer[URL]()
          pathsProcessed = ListBuffer[URL]()
          printFooter(out,-1)
        }
        resp.setStatus(HttpServletResponse.SC_OK)
      } else {
        pathsToProcess :+= new URL(getFullPath(req) + SourceWebServlet.path)
        printHeader(out)
        printFooter(out,0)
      }
    } else {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN)
    }
  }
  
  def enqueue(link : URL) {
    if(!(pathsProcessed contains link) && !(pathsToProcess contains link) && link.toString.startsWith("http://localhost")) {
      pathsToProcess :+= link
    }
  }
  
  private val linkRegex = "ref=\"([^\"]*)\""
  private val callRegex = ".*call\\('([^']*)'\\).*".r
  
  def processPage(page : BufferedReader, pageURL : URL, req : HttpServletRequest) = {
    if(pageURL.toString.endsWith(".css")) {
      (Nil,Nil)
    } else {
      var links = List[URL]()
      var calls = List[(String,Node)]()
      var line = ""
      var builder = new StringBuilder()
      while({line = page.readLine; line} != null) {
        if(!(line startsWith ("<?xml")) && !(line startsWith "<!DOC")) {
          builder.append(line.replaceAll("&nbsp;","&#180;") + "\n")
        }
      }
      val xml = XML.loadString(builder.toString)
      for(href <- xml \\ "@href") {
        links ::= resolveURL(req,pageURL,href.text)
      }
      for(clickable <- xml.descendant if !(clickable \ "@onclick" isEmpty) && 
          (clickable \ "@disabled" isEmpty) &&
          !((clickable \ "@class").text contains "lint_ignore")
      ) {
        try {
          val callRegex(c) = (clickable \ "@onclick" text)
          calls = (c,clickable) :: calls
        } catch {
          case me : MatchError => 
        }
      }
      (links,calls)
    }
  }
  
  def openPage(url : URL) = {
    val get = new HttpGet(url.toString)
    val resp = httpClient.execute(get)
    if(resp.getStatusLine.getStatusCode() == 200) {
      new BufferedReader(new InputStreamReader(resp.getEntity.getContent))
    } else {
      throw new RuntimeException(""+resp.getStatusLine.getStatusCode())
    }
  }
  
  def printHeader(out : PrintWriter) = {
    out println("<html><head><title>Lemon Source lint</title>")
    out println("<style>")
    out println(".lint_ok { color:green; }")
    out println(".lint_err { color:red; }")
    out println(".lint_warn { color:orange; }")
    out println("</style>")
    out println("<script type='text/javascript' src='"+SourceWebServlet.path+"/js/jquery-1.6.2.min.js'></script>")
    out println("</head><body>")
  }
  
  def printFooter(out : PrintWriter, id : Int) = {
    if(id >= 0) {
      out.println("<span id='next_"+id+"'></span>")
      out.println("<script type='text/javascript'>")
      out.println("$('#next_"+id+"').load('next?id="+(id+1)+"');")
      out.println("</script>")
    }
    out println("</body></html>")
  }
  
  private val redirectJS = "window.location='(.*)';".r
  private val onetimeJS = (".*load\\('("+SourceWebServlet.path + "/ajax_onetime\\?id=[^']*)'.*").r
  private val sessionExpired = "sessionExpired\\(\\);".r
  private val alert = "alert\\('(.*)'\\);?".r
  
  def callResult(callElem : Node, status : String, message : String) = {
    "<li class='lint_"+status+"'>Call: "+ StringEscapeUtils.escapeXml(callElem.toString) + " " + message + "</li>"
  }
  
  def makeCall(req : HttpServletRequest, parent : URL, call : String, callElem : Node) : String = {
    try {
      val callURL = new URL(getFullPath(req)+SourceWebServlet.path+"/ajax_call?id=" + call)
      val out = openPage(callURL)
      val result = out.readLine
      if(!callURL.toString.startsWith("http://localhost")) {
        callResult(callElem,"ok","Remote")
      } else if(result == null || result == "") {
        callResult(callElem,"warn", "No Result")
      } else {
        result match {
          case "noop();" => {
              callResult(callElem,"ok","NOOP")
            }
          case redirectJS(url) => {
              enqueue(resolveURL(req,parent,url)) 
              callResult(callElem,"ok","redirect to" + url)
            }
          case onetimeJS(callback) => {
              enqueue(new URL(getFullPath(req)+callback))
              callResult(callElem,"ok","onetime " + callback)
            }
          case sessionExpired() => {
              callResult(callElem,"err"," session expired")
            }
          case alert(msg) => {
              callResult(callElem,"err","alert: " + msg)
            }
          case _ => callResult(callElem,"warn","unknown:" + result)
        }
      }
    } catch {
      case x : Exception => {
          x.printStackTrace
          callResult(callElem,"err"," Exception ("+x.getClass.getName + " : " + x.getMessage +")")
        }
    }
  }
  
  private def resolveURL(req : HttpServletRequest, parent : URL, url : String) : URL = {
    if(url.startsWith("http:")) {
      new URL(url)
    } else if(url.startsWith("/")) {
      new URL(getFullPath(req)+url)
    } else if(url.startsWith("#")) {
      parent
    } else {
      new URL(cleanURL(parent) + url)
    }
  }
  
  private def cleanURL(url : URL) = url.getProtocol() + "://" + url.getHost + (if(url.getPort() != 80) { ":" + url.getPort() } else { ""}) + url.getPath
  
  private def getFullPath(req : HttpServletRequest) = req.getScheme() + "://" + req.getServerName() + (if(req.getServerPort() != 80) {
      ":" + req.getServerPort()
    } else {
      ""
    })

  private def getPath(req : HttpServletRequest) = getFullPath(req) + SourceWebServlet.path + req.getPathInfo()
}
