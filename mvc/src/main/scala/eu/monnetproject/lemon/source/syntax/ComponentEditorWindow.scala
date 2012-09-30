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
package eu.monnetproject.lemon.source.syntax

import eu.monnetproject.lemon.model.{Component=>LemonComponent,_}
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.common._
import scala.collection.JavaConversions._
import eu.monnetproject.util.Logging
import java.net.URI

/**
 * The editor for adding components
 * @author John McCrae
 */
class ComponentEditorWindow[C,O<:C](val state : State, lexicon : Lexicon,uielems : UIElems[C,O]) 
extends HasState(state) {
  import uielems._
  import UIElems._
  
  private val log = Logging.getLogger(this)
  
  def entries : Seq[(LexicalEntry,String)] = (lexicon.getEntrys map {
      entry => (entry,app.namer.displayName(entry))
    }).toSeq.sortBy(_._2)
  
  
  private def wrap(vals : Seq[String]) : Option[List[LemonComponent]] = {
    vals match {
      case Nil => None
      case _ => Some((vals map { value =>
            val entry = lemonFactory.makeLexicalEntry(URI.create(value))
            val component = lemonFactory.makeComponent(app.namer.name(entry,"component"))
            component.setElement(entry)
            component
          }).toList)
    }
  }
  
  def show(default : Option[List[LemonComponent]],
           after : List[LemonComponent] => Result)  = {
    dialog("Add components", twinCol("Components")(entries:_*), widthPx=450) {
      case comps => wrap(comps.map(_.toString).toList) match {
          case Some(compList) => after(compList)
          case None => NoResult
        }
    }
  }
}
