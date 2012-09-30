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
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.util.Logging
import scala.collection.JavaConversions._
import scala.math._

/**
 * 
 * @author John McCrae
 */
class TreeElement[C,O<:C](app : LemonEditorApp, node : Node, entry : LexicalEntry, remove : () => Result, uielems : UIElems[C,O])
extends StdElement(Nil,remove,uielems) {
  val log = Logging.getLogger(this)
  
  def leaves(_node : Node) : List[PhraseTerminal] = leaves(_node,100)
    
  private def leaves(_node : Node, n : Int) : List[PhraseTerminal] = if(n > 0) {
    _node.getLeaf match {
      case null => {
          val rv = (for(subnode <- _node.getEdge(Edge.edge)) yield {
              leaves(subnode,n-1)
            }).toList.flatten
          if(rv.isEmpty) {
            log.warning("failed to find leaves")
          }
          rv
        }
      case leaf => List(leaf)
    }
  } else {
    log.severe("Tree loops at node " + _node.getID + " " + node.getURI)
    Nil
  }
  
  def nodeIdx(_node : Node) : Int = entry.getDecompositions.headOption match {
    case Some(decomp) => (leaves(_node).map(decomp.indexOf(_)) :+ 1000000).min
    case None => log.warning("No decomposition") ; 0
  }
  
  def tree(_node : Node) : List[Node] = {
    _node.getEdge(Edge.edge).toList.sortBy(nodeIdx(_))
  }
  def name(_node : Node) : String = _node.getConstituent match {
    case null => _node.getLeaf match {
        case null => "???"
        case leaf => leaf match {
            case comp : LemonComponent => app.namer.displayName(comp.getElement)
            case _ => "NOTACOMP"
          }
      }
    case cons => {
        val uriString = cons.getURI.toString
        uriString.substring(max(uriString.lastIndexOf("#"),uriString.lastIndexOf("/"))+1)
      }
  }
  
  override def main = List(uielems.tree(node,tree,name))
}

