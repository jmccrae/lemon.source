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
package eu.monnetproject.lemon.source.app



import eu.monnetproject.data._
import eu.monnetproject.journalling.Journaller
import eu.monnetproject.l10n._
import eu.monnetproject.lang._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.generator._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.oils._
import eu.monnetproject.ontology.Ontology
import eu.monnetproject.ontology.OntologySerializer
import eu.monnetproject.util._
import java.io._
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import eu.monnetproject.lemon.LemonRepository
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
abstract class WebLemonEditorApp(val repository : LemonRepository,
                                 journaller2 : Journaller,
                                 //val generator : LemonGenerator,
                                 val lingOnto : LinguisticOntology,
                                 val localizer : Localizer,
                                 val lemonOils : LemonOils,
                                 val ontoSerializer : OntologySerializer
                                 
) extends LemonEditorApp {

  val _l10nLex = localizer.getLexicon("lemonsource")
  def l10nLex = _l10nLex
  
  private val log = Logging.getLogger(this)
  
  protected def deployPrefix : String
  
  val journaller = Option(journaller2)
  
  def generator(graph : URI) : LemonGenerator
  
  def importURL : URL
  def importUser : String
  def importPassword : String
  def importGraph : Option[URI]
  
  ////////////////////////////////////////////////////////////////////////////
  // Dependencies
  
//  val repository = Services.get(classOf[Repository])
//  val journaller = try {
//    Option(Services.get(classOf[Journaller]))
//  } catch {
//    case x : Exception => log.stackTrace(x) ; None
//  }
  def lemonSerializer : LemonSerializer = repository.connect(URI.create(deployPrefix+"public"))
//    journaller match {
//    case Some(j) => userID match {
//        case Some(user) => new RepoLemonSerializer(new JournallingRepository(user.id,j,repository))
//        case None => new RepoLemonSerializer(new JournallingRepository("anonymous",j,repository))
//      }
//    case None => new RepoLemonSerializer(repository)
//  }
//  val generator : LemonGenerator = Services.get(classOf[LemonGenerator])
//  val lingOnto : LinguisticOntology = Services.get(classOf[LinguisticOntology])
//  val l10nLex : LocalizationLexicon = Services.get(classOf[Localizer]).getLexicon("lemonsource")
//  val lemonOils : LemonOils = Services.get(classOf[LemonOils])
//  val ontoSerializer : OntologySerializer = Services.get(classOf[OntologySerializer])
  def userDB : UserDatabase
  
  private var auxOnto = Map[LemonModel,Ontology]()
  
  /** Handle ontologies attached to lemon models */
  def auxiliaryOntology(model : LemonModel) = if(auxOnto contains model) {
    auxOnto(model)
  } else {
    val ao = ontoSerializer.create(URI.create(
        deployPrefix+
        (if(!(model == publicModel)) {
            if(userID == None) {
              throw new IllegalArgumentException("Attempts to obtain private URI but not logged-in")
            }
            "user/" + URLEncoder.encode(userID.get.id,"UTF-8")+"/"
          } else {
            "public/"
          }) + "auxiliary/Ontology"))
    auxOnto += (model -> ao)
    ao 
  }
	
  ///////////////////////////////////////////////////////////////////////////////
  // Configuration

	
  private var userID2 : Option[User] = None
  override def userID = userID2
  def userName = userID match {
    case Some(user) => user.displayName
    case None => l10n("Anonymous")
  }
  protected def setUserID(str : Option[User]) { userID2 = str }

  def onLogIn(userID2 : String, auth : String, nonce : String) : Result = {
    log.info("logging in")
    userDB.get(userID2,auth,nonce) match {
      case Some(u) => onLogIn(u)
      case None => ErrorResult("Username and password do not match")
    }
  }
  
  def onLogIn(user : User) : Result = {
    setUserID(Some(user))
    try {
      lemonModelPrivate = repository.connect(URI.create(deployPrefix+"user/"+URLEncoder.encode(userID.get.id,"UTF-8"))).create()
      for(lexicon <- lemonModelPrivate.getLexica) {
        namer.onNewLexicon(lexicon)
      }
      return NoResult
    } catch {
      case x : Exception => {
          log.stackTrace(x)
          return ErrorResult("Persitant storage failed:" + x.getMessage());
        }
    }
  }
  
  def onLogOut : Unit
  
  /** Should be called by onLogIn */
  protected def setPrivateModel = {
    lemonModelPrivate = repository.connect(URI.create(deployPrefix+"user/"+URLEncoder.encode(userID.get.id,"UTF-8"))).create()
  }
	
  ///////////////////////////////////////////////////////////////////////////////////////
  // Global variables
  def serializer = ontoSerializer 
	
  private var lemonModelPublic : LemonModel = lemonSerializer.create()
	
  private var lemonModelPrivate : LemonModel = null
	      
  private var privateLexica = Set[Lexicon]()
	
	
  ////////////////////////////////////////////////////////////////////////////////////////
  // Actions
	
	
  def generatorListener(lexiconName : String) : LemonGeneratorListener
				
  protected def lexiconAdded(lexicon : Lexicon) {
    privateLexica += lexicon
  }
  
  def startGeneration(ontology : Ontology, config : LemonGeneratorConfig) = {		
    privateModel match {
      case Some(model) => {
          try {		
            config.lexiconNamingPattern = deployPrefix + "/${lexiconName}"
            config.entryNamingPattern = deployPrefix + "/${lexiconName}/${entryName}"
            config.otherNamingPattern = deployPrefix + "/${lexiconName}/${entryName}#${identifier}"
            val genrtor = generator(URI.create(deployPrefix+"user/"+URLEncoder.encode(userID.get.id,"UTF-8")))
            genrtor.addListener(generatorListener(config.lexiconName));
            val t = new Thread(new Runnable() {
                def run() {
                  val genModel = genrtor.doGeneration(ontology, config)
                  log.info("Adding new lexica")
                  lemonModelPrivate = repository.connect(URI.create(deployPrefix+"user/"+URLEncoder.encode(userID.get.id,"UTF-8"))).create()
                  for(lexicon <- lemonModelPrivate.getLexica) {
                    namer.onNewLexicon(lexicon)
                  }
                  if(genModel != null) {
                    for(lexicon <- genModel.getLexica) {
                      log.info("Importing lexicon " + lexicon.getURI())
                      println(lexicon.getURI.toString)
                      model.importLexicon(lexicon, lingOnto);
                      namer.generatedLexicon(lexicon)
                    }
                    log.info("Finished importing lexica")
                  }
                }
              });
            t.start();
            tracker
          } catch {
            case x : Exception => {
                log.severe("Lexicon generation failed:" + x.getMessage())
                x.printStackTrace()
                ErrorResult("Lexicon generation failed:" + x.getMessage())
              }
          }
        }
      case None => log.warning("Not generating as there is no private model") ; ErrorResult("Not generating as there is no private model")
    }
  }
  
  def tracker : Result
	
  def write(model : LemonModel, target : DataTarget) {
    lemonSerializer.write(model,target.asWriter)
  }
	
  def write(model : LemonModel, entry : LexicalEntry, target : DataTarget) {
    lemonSerializer.writeEntry(model,entry,lingOnto,target.asWriter)
  }
  
  	
  def write(lexicon : Lexicon, target : DataTarget) {
    lemonSerializer.writeLexicon(lexicon.getModel(),lexicon,lingOnto,target.asWriter)
  }
	
	
////////////////////////////////////////////////////////////////////////////////////////////////
// Handle URIs
  
  
  def makeOntologyURI(relURI : URI, extra : String,model : LemonModel) : URI = {
    makeOntologyURI(relURI.toString().substring(scala.math.max(relURI.toString().lastIndexOf("#"),relURI.toString().lastIndexOf("/"))+1) +
                    "_" + URLEncoder.encode(extra,"UTF-8"),model)
  }
  
  def makeOntologyURI(frag : String,model : LemonModel) : URI = {
    URI.create(auxiliaryOntology(model).getBaseURI().toString() + "#" +
               frag)
  }
  
////////////////////////////////////////////////////////////////////////////////////////////////////
// RDF Handler
  
  private val entryRegex = """(.*)/(.*)""".r
  
  def handleRDF(serverPath : String, path : String, output : java.io.OutputStream, mimeType : String) {
    val target = new DataTarget() {
      def asFile = throw new UnsupportedOperationException
      def asURL = throw new UnsupportedOperationException
      def asOutputStream = output
      def asWriter = new OutputStreamWriter(output)
    }
    path.drop(path.lastIndexOf("#")+1) match {
      case entryRegex(lexiconName,entryName) => {
          namer.entryForName(lexiconName, entryName) match {
            case Some(entry) => write(namer.lexiconForName(lexiconName).get.getModel(),entry, target)
            case None => throw new Exception("Entry not found")
          }
        }
      case otherwise => namer.lexiconForName(path) match {
          case Some(lexicon) => write(lexicon,target)
          case None => throw new Exception("Lexicon not found")
        }
    }
  }
  
////////////////////////////////////////////////////////////////////////////////////////////////////
// States and models
    
  private abstract class MergedModel(val model : LemonModel) extends LemonModel {
    def getContext() = model.getContext()
    
    def addLexicon(uri : URI, lang : String) = model.addLexicon(uri,lang.toString)
    
    def removeLexicon(lexicon : Lexicon) = model.removeLexicon(lexicon)
    
    def getFactory() = model.getFactory()
    
    @Deprecated
    def toRepository = null
    
    def query[Elem <: LemonElementOrPredicate](clazz : Class[Elem], sparql : String) = {
      lemonModelPrivate match {
        case null => lemonModelPublic.query(clazz,sparql)
        case _ => lemonModelPublic.query(clazz,sparql) ++ lemonModelPrivate.query(clazz,sparql)
      }
    }
    
    def merge[Elem <: LemonElement](from : Elem, to : Elem) { 
      model.merge(from,to)
    }
    
    def purgeLexicon(lexicon : Lexicon, lingOnto : LinguisticOntology) {
      model.purgeLexicon(lexicon, lingOnto)
    }
    
    def importLexicon(lexicon : Lexicon, lingOnto : LinguisticOntology) {
      model.importLexicon(lexicon, lingOnto)
    }
    
    def getPatterns = model.getPatterns
    
    def addPattern(pattern : MorphPattern) = model addPattern pattern
  }
  
  
  def privateModel : Option[LemonModel] = lemonModelPrivate match {
    case null => log.severe("lemonModelPrivate is null") ; None
    case _ => Some(lemonModelPrivate) /*Some(new MergedModel(lemonModelPrivate) {
                                       def getLexica = lemonModelPublic.getLexica() ++ lemonModelPrivate.getLexica()
                                       })*/
  }
  
  lazy val publicModel : LemonModel = new MergedModel(lemonModelPublic) {
    def getLexica = lemonModelPublic.getLexica()
  }
  
  def trueModel(model : LemonModel) = model match {
    case mm : MergedModel => mm.model
    case x => x
  }
    
  private def lingOnto2 = lingOnto
  
  def state(lexicon2 : Option[Lexicon], model2 : LemonModel) = new State {
    def app = WebLemonEditorApp.this
    def lemonFactory = model.getFactory
    def lexicon = lexicon2.getOrElse(null)
    def model = model2
    val lexiconName = lexicon2 match {
      case Some(lexicon2) => namer.displayName(lexicon2)
      case None => "ERROR"
    }
    def lingOnto = lingOnto2
    val privacy = lexicon2 match {
      case None => lemonModelPrivate != null
      case Some(lexicon2) => {
          lemonModelPrivate != null && lemonModelPrivate.getLexica().contains(lexicon2)
        }
    }
  }
  
//////////////////////////////////////////////////////////////////////////////
// Ontologies
// 
  
  var ontologies = List[Ontology]()
  
  def addOntology(ontology : Ontology) { ontologies ::= ontology }
}
