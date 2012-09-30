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

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import java.net.URI
import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class TreeController[C,O<:C](state : State, entry : LexicalEntry, uielems : UIElems[C,O])
extends StdController[Node,C,O](new TreeModel(entry),new TreeView(state,entry,uielems))

class TreeModel(entry : LexicalEntry) extends StdModel[Node] {
  def add(node : Node) = {
    entry.addPhraseRoot(node)
  }
  
  def remove(node : Node) = {
    entry.removePhraseRoot(node)
  }
  
  def elements = entry.getPhraseRoots.toSeq
}

class TreeView[C,O<:C](state : State, entry : LexicalEntry, val uielems : UIElems[C,O]) 
extends HasState(state) with StdPanel[Node,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Phrase trees"
  val addText = "Add tree"
  val removeText = "Remove tree"
  val description = "A tree gives a detailed view on the structure of a multi-word entry"
  
  protected def makeElem(node : Node,
                         remove : () => Result,
                         update : Node => Result,
                         change : Node => Unit) = {
    new TreeElement(app,node,entry,remove,uielems)
  }
  
  protected def makeDisplay(node : Node) = new TreeElement(app,node,entry,() => NoResult,uielems).main.head
  
  def dialog(node : Option[Node], after : Node => Result) = {
    NoResult
  }
}
//class TreeController(val view : UIView[Node] with State, entry : LexicalEntry)
//extends HasState(view) with GenericController[Node] {
//
//  val title = "Phrase trees"
//  val addText = "Add tree"
//  val removeText = "Remove tree"
//  
//  private val consRegex = "c(.*)".r
//  private val leafRegex = "l(.*)".r
//  
//  def wrap(vals : List[String]) = vals.headOption match {
//    case Some(head) => {
//        val node = lemonFactory.makeNode
//        wrap(vals,node)
//        Some(node)
//    }
//    case None => None
//  }
//  
//  @tailrec private def wrap(vals : List[String], node : Node) : Option[Node]= vals.headOption match {
//    case Some("(") => wrap(vals.tail,node)
//    case Some(")") => Some(node)
//    case Some(consRegex(cons)) => {
//        node setConstituent(new ConstituentImpl(URI.create(cons)))
//        wrap(vals.tail,node)
//    }
//    case Some(leafRegex(leaf)) => {
//        val comp = lemonFactory.makeComponent
//        comp setElement lemonFactory.makeLexicalEntry(URI.create(leaf))
//        node setLeaf comp
//        wrap(vals.tail,node)
//    }
//    case None => None
//  }
//  
//  def unwrap(node : Node) : List[String] = {
//    var rv : List[String] = Nil
//    if(node.getConstituent != null) {
//      rv ::= "c" + node.getConstituent.getURI.toString
//    }
//    if(node.getLeaf != null) {
//      rv ::= "l" + node.getLeaf.asInstanceOf[Component].getElement.getURI.toString
//    }
//    if(!node.getEdge(Edge.edge).isEmpty) {
//      val unwrappedsub : List[String] = (node.getEdge(Edge.edge).flatMap(subnode => unwrap(subnode))).toList
//      rv :::= "(" :: unwrappedsub ::: ")" :: Nil
//    }
//    rv
//  }
//  
//  def load {
//    for(node <- entry.getPhraseRoots) {
//      view.add(node)
//    }
//  }
//  
//  def add(node : Node) = {
//    entry.addPhraseRoot(node)
//    view add node
//  }
//  
//  def remove(node : Node) = {
//    entry.removePhraseRoot(node)
//    view remove node
//  }
//  
//  private class ConstituentImpl(uri : URI) extends URIElement(uri) with Constituent
//}
