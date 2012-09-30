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
package eu.monnetproject.lemon.source.generation

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.generator._
import eu.monnetproject.ontology._
import java.io._
import java.net.URL
import java.net.URI
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import scala.collection.JavaConversions._

class RESTGenerator(remoteURL : URL, putURL : URL, username : String, password : String,
                    graph : URI,
                    ontologySerializer : OntologySerializer, lemonSerializer : LemonSerializer) extends LemonGenerator {

  private var listeners = List[LemonGeneratorListener]()
  private val log = eu.monnetproject.util.Logging.getLogger(this)
  private var progress = 0.0f
  private var message = ""
  
  def addListener(listener : LemonGeneratorListener) {
    listeners ::= listener
  }
  
  def doGeneration(model : LemonModel, ontology : Ontology, config : LemonGeneratorConfig) {
    throw new UnsupportedOperationException();
  }
  
  def doGeneration(ontology : Ontology, config : LemonGeneratorConfig) : LemonModel = {
    val client = new DefaultHttpClient
    log.info("Posting generation request to " + remoteURL.toString)
    val post = new HttpPost(remoteURL.toString);
    val file = File.createTempFile("ontology", "rdf")
    file.deleteOnExit
    val fileWriter = new FileWriter(file);
    ontologySerializer.write(ontology, fileWriter);
    fileWriter.flush
    fileWriter.close
    val bin = new FileBody(file)
    val reqEntity = new MultipartEntity();
    reqEntity.addPart("ontology",bin);
    if(config.customLabel != null) {
      reqEntity addPart ("customLabel", new StringBody(config.customLabel.toString))
    }
    if(config.entryNamingPattern != null) {
      reqEntity addPart ("entryNamingPattern", new StringBody(config.entryNamingPattern))
    }
    reqEntity addPart ("inferLang", new StringBody(config.inferLang.toString))
    if(config.languages != null) {
      reqEntity addPart ("languages", new StringBody(config.languages.mkString(",")))
    }
    if(config.lexiconName != null) {
      reqEntity addPart ("lexiconName", new StringBody(config.lexiconName))
    }
    if(config.lexiconNamingPattern != null) {
      reqEntity addPart ("lexiconNamingPattern", new StringBody(config.lexiconNamingPattern))
    }
    if(config.otherNamingPattern != null) {
      reqEntity addPart ("otherNamingPattern", new StringBody(config.otherNamingPattern))
    }
    if(config.unlanged != null) {
      reqEntity addPart ("unlanged", new StringBody(config.unlanged.toString))
    }
    reqEntity addPart ("useDefaultLEP", new StringBody(config.useDefaultLEP.toString))
    reqEntity addPart ("future", new StringBody("true"))
    post setEntity reqEntity
    val response = client execute post
    val resEntity = response.getEntity
    if(response.getStatusLine.getStatusCode != 200) {
      val in = new BufferedReader(new InputStreamReader(resEntity.getContent,"UTF-8"))
      var s = ""
      while({s = in.readLine ; s} != null) {
        println(s)
      }
      throw new RuntimeException("Bad response HTTP" + response.getStatusLine.getStatusCode + ": " + response.getStatusLine.getReasonPhrase)
    }
    file.delete
    val in = new BufferedReader(new InputStreamReader(resEntity.getContent,"UTF-8"))
    val id = in.readLine
    if(id == null) {
      throw new RuntimeException("No id in response")
    }
    val slashOpt = if(remoteURL.toString.endsWith("/")) { "" } else { "/"}
    val pollURL = new URL(remoteURL + slashOpt + "poll?future="+id)
    while(progress < 1.0f) {
      System.err.println("Polling: " + pollURL.toString + " progress: "+ progress)
      val pollIn = new BufferedReader(new InputStreamReader(pollURL.openStream,"UTF-8"))
      val newMessage = pollIn.readLine
      System.err.println("message: " + newMessage)
      val newProgress = pollIn.readLine match { 
        case null => progress
        case p => p.toFloat
      }
      if(newMessage != null && newMessage != message || newProgress != progress) {
        for(listener <- listeners) {
          listener.update(newMessage,newProgress)
        }
        message = newMessage
        progress = newProgress
      }
      this.synchronized {
        this.wait(200)
      }
    }
    val reportURL = new URL(remoteURL +  slashOpt + "report?future="+id)
    System.err.println("Obtaining report from: " + reportURL)
    val reportIn = new BufferedReader(new InputStreamReader(pollURL.openStream,"UTF-8"))
    val report = new RESTReport()
    var s = ""
    while({s = reportIn.readLine; s} != null) {
      val ss = s.split("\",\"")
      if(ss.length == 5) {
        val entryURI1 = URI.create(ss(0).drop(1))
        val entryURI2 = URI.create(ss(1))
        val actorName = ss(2)
        val status = ss(3)
        val message = ss(4).dropRight(1)
        if(!report.entries.contains(entryURI1)) {
          report.entries += (entryURI1 -> new RESTEntryReport(entryURI2))
        }
        report.entries(entryURI1).asInstanceOf[RESTEntryReport].actorReports ::= new RESTActorReport(actorName,status,message)
      }
    }
    val dataURL = new URL(remoteURL +  slashOpt + "data?future="+id)
    
    val buf = new StringBuffer()
      
    val dataIn = dataURL.openStream
    val buf2 = new Array[Byte](1024)
    var read = -1
    while({read = dataIn.read(buf2,0,1024);read} != -1) {
      buf.append(new String(buf2,0,read))
    }
    dataIn.close
    if(putURL != null) {
      val dataPost = new HttpPost(putURL.toString)
      val dataEntity = new MultipartEntity
      dataEntity.addPart("data",new InputStreamBody(new ByteArrayInputStream(buf.toString.getBytes("UTF-8")),"file.rdf"))
      dataEntity.addPart("username", new StringBody(username))
      dataEntity.addPart("password", new StringBody(password))
      dataEntity.addPart("graph", new StringBody(graph.toString))
      dataPost setEntity dataEntity
      try {
        val dataResponse = client.execute(dataPost)
        if(dataResponse.getStatusLine.getStatusCode != 200) {
          println("Data upload response: " + dataResponse.getStatusLine.getReasonPhrase)
          val dataResponseIn = new BufferedReader(new InputStreamReader(dataResponse.getEntity.getContent))
          var s = ""
          while({s = dataResponseIn.readLine;s} != null) {
            println(s)
          }
        }
      } catch {
        case x : org.apache.http.client.ClientProtocolException => {
            // 4store query      
            val dataPost2 = new HttpPost(putURL.toString)
            val dataEntity2 = new UrlEncodedFormEntity(List(
                new BasicNameValuePair("data",buf.toString()),
                new BasicNameValuePair("graph",graph.toString())
              ))
            dataPost2.setEntity(dataEntity2)
            val dataResponse2 = client.execute(dataPost2)
            if(dataResponse2.getStatusLine.getStatusCode != 200) {
              println("Data upload response: " + dataResponse2.getStatusLine.getReasonPhrase)
              val dataResponseIn = new BufferedReader(new InputStreamReader(dataResponse2.getEntity.getContent))
              var s = ""
              while({s = dataResponseIn.readLine;s} != null) {
                println(s)
              }
            }
          }
      }
      log.info("Sending listeners a completed status")
    
      for(listener <- listeners) {
        listener.onComplete(report)
      }
    
      return null;
    } else {
      for(listener <- listeners) {
        listener.onComplete(report)
      }
      
      return LemonSerializer.newInstance().read(new StringReader(buf.toString))
    }
    /*System.err.println("Obtaining data from: " + dataURL)
     val dataIn = new BufferedReader(new InputStreamReader(dataURL.openStream,"UTF-8"))
     val model = lemonSerializer.create
     lemonSerializer.read(model, dataIn)
     return model*/
  }
}

  
class RESTReport extends GenerationReport {
  var entries = Map[URI,EntryGenerationReport]()
  
  override def getEntries() = entries
}

class RESTEntryReport(val entryURI : URI) extends EntryGenerationReport {
  var actorReports = List[ActorGenerationReport]()
  
  def getEntryURI() = entryURI
    
  def getActorReports() = actorReports
}

class RESTActorReport(val actorName : String, val status : String, val message : String) extends ActorGenerationReport {
  def getActorName() = actorName
  def getStatus() = status
  def getMessage() = message
}