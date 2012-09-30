/****************************************************************************
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
 ********************************************************************************/

package eu.monnetproject.lemon.source.web

import eu.monnetproject.lang.Language
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.util.Logging
import java.net.{URI,URLEncoder}
import scala.collection.JavaConversions._

class WebLemonNamer(app : WebLemonEditor) extends LemonAppNamer {
  val log = Logging.getLogger(this)
  
  private val random = new java.util.Random()
  
  def name(model : LemonModel, lemonType : Class[_], lexiconName : String, entryName : String, identifier : String) = {
    if(lemonType == classOf[Lexicon]) {
      app.deployPrefix+"/"+URLEncoder.encode(lexiconName,"UTF-8")
    } else if(lemonType == classOf[LexicalEntry]) {
      app.deployPrefix+"/"+URLEncoder.encode(lexiconName,"UTF-8")+"/"+URLEncoder.encode(entryName.replaceAll("\\s","_"),"UTF-8")
    } else {
      app.deployPrefix+"/"+URLEncoder.encode(lexiconName,"UTF-8")+"/"+URLEncoder.encode(entryName.replaceAll("\\s","_"),"UTF-8")+"/"+URLEncoder.encode(identifier,"UTF-8")
    }
  }
  
  def name(model : LemonModel, lexiconName : String, entryName : String, identifier : String) : URI = URI.create(
    if(entryName == null && identifier == null) {
      app.deployPrefix+"/"+URLEncoder.encode(lexiconName,"UTF-8")
    } else if(identifier == null) {
      app.deployPrefix+"/"+URLEncoder.encode(lexiconName,"UTF-8")+"/"+URLEncoder.encode(entryName.replaceAll("\\s","_"),"UTF-8")
    } else {
      app.deployPrefix+"/"+URLEncoder.encode(lexiconName,"UTF-8")+"/"+URLEncoder.encode(entryName.replaceAll("\\s","_"),"UTF-8")+"/"+URLEncoder.encode(identifier,"UTF-8")
    }
  )
  
  def name(entry : LexicalEntry, newFrag : String) : URI = {
    if(entry.getURI() == null) {
      return URI.create(app.deployPrefix + "/unknown/" + URLEncoder.encode(entry.getID(),"UTF-8") + "#" + newFrag + random.nextInt.toHexString)
    } else if(entry.getURI().getFragment() == null) {
      return URI.create(entry.getURI().toString() + "#" + newFrag + random.nextInt.toHexString)
    } else {
      return URI.create(entry.getURI().toString().dropRight(entry.getURI().getFragment().length() +1) + "#" + newFrag + random.nextInt.toHexString)
    }
  }
  
  def name(entry : LexicalSense, newFrag : String) : URI = {
    if(entry.getURI() == null) {
      return URI.create(app.deployPrefix + "/unknown/" + URLEncoder.encode(entry.getID(),"UTF-8") + "#" + newFrag + random.nextInt.toHexString)
    } else if(entry.getURI().getFragment() == null) {
      return URI.create(entry.getURI().toString() + "#" + newFrag + random.nextInt.toHexString)
    } else {
      return URI.create(entry.getURI().toString().dropRight(entry.getURI().getFragment().length() +1) + "#" + newFrag + random.nextInt.toHexString)
    }
  }
  
  
  private var lexiconNames = Map[Lexicon,String]()
  private var lexiconsByName = Map[String,Lexicon]() 
  
  for(lexicon <- app.publicModel.getLexica) {
    val lexiconName = displayName(lexicon)
    lexiconNames += (lexicon -> lexiconName)
    lexiconsByName += (lexiconName -> lexicon)
  }
  
  def nameLexicon(model : LemonModel, lexiconName : String, lang : Language) : Lexicon = {
    System.err.println("Creating a lexicon at URI " + name(model,classOf[Lexicon],lexiconName,null,null) + "class" + model.getClass())
    val lexicon = model.addLexicon(URI.create(name(model,classOf[Lexicon],lexiconName,null,null)), lang.toString)
    lexiconsByName += (lexiconName -> lexicon)
    lexiconNames += (lexicon -> lexiconName)
    lexicon
  }
  
  def onNewLexicon(lexicon : Lexicon) {
    val lexiconName = displayName(lexicon)
    lexiconsByName += (lexiconName -> lexicon)
    lexiconNames += (lexicon -> lexiconName)
  }
  
  def lexiconForName(name : String) = {
    lexiconsByName.get(name)
  }
  
  
  
  def nameEntry(model : LemonModel, lexiconName : String, entryName : String) : LexicalEntry = {
    model.getFactory.makeLexicalEntry(URI.create(name(model,classOf[LexicalEntry],lexiconName,entryName,null)))
  }
  
  private val entryRegex = """/?(.*)/(.*)""".r
  
  private def frag(uri : URI) = { uri.toString substring((uri.toString lastIndexOf app.deployPrefix) + app.deployPrefix.length + 1) }
  
  def entryForName(lexiconName : String, entryName : String) = lexiconsByName.get(lexiconName) match {
    case Some(lexicon) => Some(nameEntry(lexicon.getModel(),lexiconName,entryName))
    case None => {
        log.info("No entry for " + lexiconName)
        None
    }
  }
  
  def generatedLexicon(lexicon : Lexicon) {
    val lexiconName = displayName(lexicon)
    lexiconsByName += (lexiconName -> lexicon)
    lexiconNames += (lexicon -> lexiconName)
  }
  
  def displayName(element : LemonElement) = element match {
    case lexicon : Lexicon => frag(lexicon.getURI())
    case entry : LexicalEntry => frag(entry.getURI()).drop(frag(entry.getURI()).lastIndexOf("/")+1)
    case _ => throw new IllegalArgumentException(element.toString)
  }
  
  def lexiconNameFromEntry(entry : LexicalEntry) : String = frag(entry.getURI) match {
    case entryRegex(lexiconName,_) => lexiconName
    case _ => throw new IllegalArgumentException(entry.toString)
  }
}

