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

package eu.monnetproject.lemon.source.relations

import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.basic.EntrySelectWindow
import eu.monnetproject.lemon.model._
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class LexicalVariantController[C,O<:C](state : State, entry : LexicalEntry, uielems : UIElems[C,O])
extends StdController[(LexicalVariant,LexicalEntry),C,O](new LexicalVariantModel(entry), new LexicalVariantView(state,uielems)) 

class LexicalVariantModel(entry : LexicalEntry) extends StdModel[(LexicalVariant,LexicalEntry)] {
  def add(x : (LexicalVariant,LexicalEntry)) = {
    entry.addLexicalVariant(x._1, x._2)
  }
  
  def remove(x : (LexicalVariant,LexicalEntry)) = {
    entry.removeLexicalVariant(x._1, x._2)
  }
  
  def elements = (for{
      (variant,entrys) <- entry.getLexicalVariants().toSeq
      entry <- entrys
    } yield (variant,entry)).toSeq.sortBy(_._1.getURI.toString)
}

class LexicalVariantView[C,O<:C](state : State, val uielems : UIElems[C,O]) 
extends HasState(state) with StdPanel[(LexicalVariant,LexicalEntry),C,O] {
  import UIElems._
  import uielems._
  
  val title = "Lexical variants"
  val addText = "Add lexical variant"
  val removeText = "Remove lexical variant"
  val description = "Indicates a relation to another entry. Note that lexical variants do not take into account the meaning of the entry, for relations that are sensitive to meaning, such as translation, use a sense relation."
  
  protected def makeElem(x : (LexicalVariant, LexicalEntry),
                         remove : () => Result,
                         update : ((LexicalVariant,LexicalEntry)) => Result,
                         change : ((LexicalVariant,LexicalEntry)) => Unit) = {
    new StdColElement(List(x._1.getURI.getFragment, app.namer.displayName(x._2)),views(x),remove,() => update(x),uielems)
  }
  
  protected def makeDisplay(x : (LexicalVariant,LexicalEntry)) = html("<li>" + x._1.getURI.getFragment + " -&gt; <a href=\"" + x._2.getURI + "\">"+app.namer.displayName(x._2)+"</a></li>")
  
  lazy val entrySelect = new EntrySelectWindow(uielems,state,model)
  
  def dialog(x : Option[(LexicalVariant,LexicalEntry)], after : ((LexicalVariant,LexicalEntry)) => Result) = {
    val default = x match {
      case Some((lv,_)) => Some(lv.getURI.getFragment)
      case None => None
    }
    prompt().show(app, "Select lexical variant type", "Lexical variant type",
                  (lingOnto.getLexicalVariant() map { _.getURI().getFragment() }).toList.sorted,
                  default, s => {
        val lv = lingOnto.getLexicalVariant(s)
        entrySelect.show(le => after((lv,le)))
      });
  }
}