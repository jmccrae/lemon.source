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

import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.app.WebLemonEditorApp
import java.io._
import java.net._
import scala.collection.JavaConversions._

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

/**
 * 
 * @author John McCrae
 */
class ImportLexiconWindow[C,O<:C](app : WebLemonEditorApp, uielems : UIElems[C,O]) {
  import UIElems._
  import uielems._
  
  def theModel = app.privateModel match {
    case Some(pm) => app.trueModel(pm)
    case None => app.trueModel(app.publicModel)
  }
  
  private def fileToString(file : File) : String = {
    new java.util.Scanner(file).useDelimiter("\\Z").next
  }
  
  def show = dialog("Import a lexicon (this basically does not work at the moment, sorry - John)", upload("Lemon file", file => {
        val client = new DefaultHttpClient()
        val dataPost = new HttpPost(app.importURL.toString)
        val dataEntity = new MultipartEntity
        dataEntity.addPart("data",new FileBody(file))
        dataEntity.addPart("username", new StringBody(app.importUser))
        dataEntity.addPart("password", new StringBody(app.importPassword))
        app.importGraph match {
          case Some(graph) => {
              try {
                dataEntity.addPart("graph", new StringBody(graph.toString))
                dataPost setEntity dataEntity
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
                case x : Exception => {
                    // 4store query      
                    println("Connection failed... trying as URLEncoded")
                    val dataPost2 = new HttpPost(app.importURL.toString)
                    val dataEntity2 = new UrlEncodedFormEntity(List(
                        new BasicNameValuePair("data",fileToString(file)),
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
            }
          case None =>
        } 
      })) {
    e => NoResult
  }
}
