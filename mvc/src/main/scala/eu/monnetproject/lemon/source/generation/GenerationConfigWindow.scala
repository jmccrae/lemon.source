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

import eu.monnetproject.data._
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.ErrorResult
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.generator.LemonGeneratorConfig
import eu.monnetproject.label.LabelExtractor
import eu.monnetproject.lang._
import eu.monnetproject.ontology._
import java.io._
import java.net.{URL, URI}
import java.util.{Collection=>JCollection,HashSet=>JHashSet}
import eu.monnetproject.util.Logging

/**
 * The second window for generating configurations
 * @author John McCrae
 */
class GenerationConfigWindow[C,O<:C](val app : LemonEditorApp, ontology : Ontology, lexiconPrefix : String, uielems : UIElems[C,O]){
  import scala.collection.JavaConversions._
  import uielems._
  import UIElems._
  
  private val log = Logging.getLogger(this)
 // var container : O = null
	
  def langs = (availLangs map {
    lang => (lang.getName,lang)
  }).toSeq
  
  def show : Result = {
    dialog("Lexicon generation",
           formElems(field("Label URI (optional)"),
                     checkbox("Generate only for labelled entities"),
                     langSelect("Entities without language are in",defaultOption=Some("Infer")),
                     checkboxes("Languages", langs)
      )
    ) {
      case (customLabel,noDefLep,defLang,languages) => 
          try {
            val config = new LemonGeneratorConfig()
            config.customLabel = URI.create(customLabel)
            config.useDefaultLEP = !noDefLep
            val set = new JHashSet[Language]()
            for(l <- languages) {
              set.add(Language.get(l.toString()))
            }
            config.languages = if(availLangs.isEmpty || (availLangs.size == 1 && availLangs.contains(LabelExtractor.NO_LANGUAGE))) {
              null
            } else {
              set
            }
            config.unlanged = defLang.getOrElse(null)
            config.inferLang = defLang == None
            config.lexiconName = lexiconPrefix
            app.startGeneration(ontology,config)
          } catch {
            case x : Exception => new ErrorResult("Generation could not be started as " + x.getMessage)
          }
    }
  }
  private lazy val availLangs : Set[Language] = (ontology.getEntities flatMap { entity =>
      entity.getAnnotations.values flatMap { annotations => 
        annotations flatMap { 
          case annotation : LiteralValue => if(annotation.getLanguage() == null) {
              Some(LabelExtractor.NO_LANGUAGE)
            } else {
              Option(annotation.getLanguage())
            }
          case _ => None
        }
      }
    }).toSet
	      
}
