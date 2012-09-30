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

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.common._
import scala.collection.JavaConversions._

/**
 * A window for selecting properties
 * @author John  McCrae
 */
class PropertyEditorWindow[C,T<:C](val app : LemonEditorApp, lingOnto : LinguisticOntology, uielems : UIElems[C,T])  {
  import uielems._
  import UIElems._
  
  def show(default : Option[PropertyPair], after : PropertyPair => Result)  : Result = {
    val newDefault = default match {
      case Some(PropertyPair(p,v)) => Some(p,PropertyPair(p,v))
      case None => None
    }
    dialog("Change lexical property", tiedSelects(
        "Property",
        "Property Value",
        props,
        propVals,
        newDefault,
        Some((e : Property,f : PropertyPair) => (app.lingOnto.getDefinitions(e).headOption.getOrElse(""),
                       app.lingOnto.getDefinitions(f.propVal).headOption.getOrElse("")))
      ))(after)
  }
  
  def props : Seq[(Property,String)] = lingOnto.getProperties.toSeq.map {
    prop => (prop,prop.getURI.getFragment)
  }.sortBy(_._2)
  
  def propVals(prop : Property) : Seq[(PropertyPair,String)] = lingOnto.getValues(prop).toSeq.map {
    propVal => (PropertyPair(prop,propVal),propVal.getURI.getFragment)
  }.sortBy(_._2)
}
