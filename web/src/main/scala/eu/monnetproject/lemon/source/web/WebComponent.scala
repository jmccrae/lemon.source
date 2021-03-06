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

import eu.monnetproject.lemon.source.common.UIElems._
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import scala.xml._

/**
 * 
 * @author John McCrae
 */

trait WebComponent {
  def html : Node
  def id : String
}

class SimpleComponent(val id : String, val html : Node) extends WebComponent

class WebContainer(val id : String,
                   components : List[WebComponent],
                   val main : scala.xml.Elem,
                   val elem : Int => Option[scala.xml.Elem] = { i => None }) 
extends WebComponent {
  private var comps : Buffer[WebComponent] = components.toBuffer
  def add(comp : WebComponent) {
    comps :+= comp
  }
  def remove(comp : WebComponent) {
    comps -= comp
  }
  def update(auld : WebComponent, nieuw : WebComponent) {
    comps = for(comp <- comps) yield {
      if(comp == auld) {
        nieuw
      } else {
        comp
      }
    }
  }
  
  def clear {
    comps = new ListBuffer[WebComponent]()
  }
  def html : Node = {
    var i = 0;
    main.copy(child=main.child ++ (for(comp <- comps) yield {
        i += 1
        elem(i) match {
          case Some(subtag) => subtag.copy(child=comp.html)
          case None => comp.html
        }
      }))
  }
}

