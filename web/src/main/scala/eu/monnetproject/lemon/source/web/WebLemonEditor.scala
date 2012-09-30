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

import eu.monnetproject.data._
import eu.monnetproject.l10n._
import eu.monnetproject.lang._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.generator._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.generation.RESTGenerator
import eu.monnetproject.lemon.source.web.html._
import eu.monnetproject.lemon.source.users.UserDatabaseImpl
import eu.monnetproject.util._
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import javax.servlet.ServletConfig
import scala.collection.JavaConversions._
import scala.math._

/**
 * 
 * @author John McCrae
 */
class WebLemonEditor(repository : LemonRepository,
                     journaller2 : eu.monnetproject.journalling.Journaller,
                  //   generator : LemonGenerator,
                  config : ServletConfig,
                     lingOnto : LinguisticOntology,
                     localizer : Localizer,
                     lemonOils : eu.monnetproject.lemon.oils.LemonOils,
                     ontoSerializer : eu.monnetproject.ontology.OntologySerializer) 
extends WebLemonEditorApp(repository,journaller2,lingOnto,localizer,lemonOils,ontoSerializer) {
  val uielems = new WebUIElem(this)
  val navigator : Navigator = new Navigator(this)
  var viewMap = Map[UIView[_],String]()
  var elemMap = Map[ViewElement[WebComponent,WebContainer],String]()
  
  override val userDB = new UserDatabaseImpl()
  
  def importURL = config.getInitParameter("virt_import") match {
      case null => null
      case url => new URL(url)
    }
  def importUser = config.getInitParameter("virt_user") match {
      case null => println("defaulting virt_user to dba");"dba"
      case username => username
    }
  def importPassword = config.getInitParameter("virt_password") match {
      case null => println("defaulting virt_password to dba"); "dba"
      case password => password
    }
    def importGraph = userID match {
      case Some(userID) => Some(URI.create(deployPrefix+"user/"+URLEncoder.encode(userID.id,"UTF-8")))
      case None => None
    }
  
  override def generator(graph : URI) : LemonGenerator = new RESTGenerator(
    config.getInitParameter("generator") match {
      case null => println("defaulting generator to localhost");new URL("http://localhost:8080/generate")
      case url => new URL(url)
    },
    importURL,
    importUser,
    importPassword,
    graph,
    ontoSerializer,
    LemonSerializer.newInstance
  )
  
  /** Reload all the views */
  def reload { 
    throw new RuntimeException("deprecate this!")
  }	
  
  /** Show a notification (of an error)
   * @param message The message (it will be automatically localized)
   */
  def notify(message : String) {
    throw new RuntimeException("Use a result")
  }
	
  val namer = new WebLemonNamer(this)
  
  def openResource(fileName: String, dataSource : InputStream, mimeType : String) = {
    new OpenResourceResult(fileName,dataSource,mimeType)
  }
  
  private var lastGeneratorProgress = -1
  private var lastGeneratorMessage = ""
  private var lastGeneratorReport : Option[GenerationReport] = None
  var lastModel : Option[String] = None
  
  def getProgress = (lastGeneratorProgress,lastGeneratorMessage)
  
  def generatorListener(lexiconName : String) = {
    lastGeneratorProgress = 0
    lastGeneratorMessage = "Starting"
    lastModel = None
    new LemonGeneratorListener() {
      override def update(message : String, progress : Float) {
        lastGeneratorProgress = min((progress * 100.0).toInt,99)
        lastGeneratorMessage = message
      }
      
      override def onComplete(report : GenerationReport) {
        lastGeneratorProgress = 101
        lastGeneratorMessage = "Complete"
        lastGeneratorReport = Some(report)
        navigator.addHandler("generation_report", None, {
            LoadDialogResult(l10n("Generation report"),"generation_report_dialog",new ReportDisplayer(WebLemonEditor.this).display(report),800)
          })
        //val sw = new java.io.StringWriter()
        //val result = LemonSerializer.newInstance.write(tempModel,sw)
        //lastModel = Some(sw.toString)
        //lemonSerializer.read(trueModel(privateModel.get),new java.io.StringReader(sw.toString))
        
      }
    }
  }
  
  def tracker = new StartTrackerResult(new SimpleComponent("tracker",new Tracker(this).tag))
  
  def onLogOut { setUserID(None) }
  
  def deployPrefix = "http://monnetproject.deri.ie/lemonsource"
  def deployServer = "http://monnetproject.deri.ie"
}
