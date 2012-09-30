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
package eu.monnetproject.lemon.source.app;


import scala.collection.mutable.{HashMap,HashSet}
import scala.collection.JavaConversions._

import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.ontology.OntologySerializer;

import eu.monnetproject.data._
import eu.monnetproject.journalling.Journaller
import eu.monnetproject.lang.Language;
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.generator._
import eu.monnetproject.lemon.model._;
import eu.monnetproject.l10n.LocalizationLexicon;

import java.io._
import java.net.URI
import java.util.{HashSet => JHashSet}

import eu.monnetproject.util.Logging

/**
 * The common application code (i.e., save/load)
 * @author John McCrae
 */
trait LemonEditorApp {
  private val log = Logging.getLogger(this)
  
  /** The linguistic ontology (LexInfo) */
  def lingOnto : LinguisticOntology
  /** The localization lexicon */
  def l10nLex : LocalizationLexicon 
  /** The lemon serializer */
  def lemonSerializer : LemonSerializer
  /** The ontology serializer */
  def ontoSerializer : OntologySerializer
  /** The navigator (for web applications this sends redirects, for desktop it changes views) */
  def navigator : AbstractNavigator
  /** The journaller if available */
  def journaller : Option[Journaller]
  /** The user database  */
  def userDB : UserDatabase
  
  /** The language of the UI */
  var lang = Language.ENGLISH;
  
  /** Include help messages */
  var showHelp = false
	
  /** Reload all the views */
  def reload : Unit
	
  /** Localize a string to the current app lang */
  def l10n(key : String) = try {
    l10nLex.get(key,lang)
  } catch {
    case x : Exception => Logging.stackTrace(log,x) ; "ERROR"
  }
  def l10n(key : String, args : Object*) = try {
    l10nLex.get(key,args,lang)	
  } catch {
    case x : Exception => Logging.stackTrace(log,x) ; "ERROR"
  }
	
  /** Show a notification (of an error)
   * @param message The message (it will be automatically localized)
   */
  def notify(message : String) : Unit
	
  def namer : LemonAppNamer
   
  /** Get the set of available ontology objects */
  def ontologies : List[Ontology]
   
  /** Add a new ontology to the application space */
  def addOntology(ontology : Ontology) : Unit
  
  /** Creation of ontology URIs for OILS */
  protected def makeOntologyURI(relURI : URI, extra : String, model : LemonModel) : URI
  /** Creation of ontology URIs for OILS */
  protected def makeOntologyURI(frag : String, model : LemonModel) : URI
  
  /** The id of the currently logged in user  */
  def userID : Option[User] = None
  def userName : String
  
  // IO Actions
  def write(lexicon : Lexicon, target : DataTarget)
  def write(model : LemonModel, entry : LexicalEntry, target : DataTarget)
  def openResource(fileName: String, dataSource : InputStream, mimeType : String) : Result
  
  /** Get a new state with the given lexicon set */
  def state(lexicon : Option[Lexicon], model : LemonModel) : State
  
  /** Start the generator */
   def startGeneration(ontology : Ontology, config : LemonGeneratorConfig) : Result
}
