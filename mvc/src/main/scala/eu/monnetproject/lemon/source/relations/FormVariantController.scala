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

import eu.monnetproject.lemon.LemonModels
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
class FormVariantController[C,O<:C](state : State, sense : LexicalForm, uielems : UIElems[C,O])
extends StdController[(FormVariant,LexicalEntry,LexicalForm),C,O](new FormVariantModel(state,sense), new FormVariantView(state,uielems)) 

class FormVariantModel(state : State, form : LexicalForm) extends StdModel[(FormVariant,LexicalEntry,LexicalForm)] {
  def add(x : (FormVariant,LexicalEntry,LexicalForm)) = {
    form.addFormVariant(x._1, x._3)
  }
  
  def remove(x : (FormVariant,LexicalEntry,LexicalForm)) = {
    form.removeFormVariant(x._1, x._3)
  }
  
  def entryForForm(form : LexicalForm) : LexicalEntry = {
    LemonModels.getEntriesByForm(state.model,form).headOption.getOrElse(throw new RuntimeException("No entry for form"))
  }
  
  def elements = (for{
      (variant,forms) <- form.getFormVariants().toSeq
      form2 <- forms
    } yield (variant,entryForForm(form2),form2)).toSeq.sortBy(_._1.getURI.toString)
}

class FormVariantView[C,O<:C](state : State, val uielems : UIElems[C,O]) 
extends HasState(state) with StdPanel[(FormVariant,LexicalEntry,LexicalForm),C,O] {
  import UIElems._
  import uielems._
  
  val title = "Form variants"
  val addText = "Add form variant"
  val removeText = "Remove form variant"
  val description = "Form variant indicate relationships between forms in the lexicon."
  
  protected def makeElem(x : (FormVariant, LexicalEntry,LexicalForm),
                         remove : () => Result,
                         update : ((FormVariant,LexicalEntry,LexicalForm)) => Result,
                         change : ((FormVariant,LexicalEntry,LexicalForm)) => Unit) = {
    new StdColElement(List(app.namer.displayName(x._2), x._1.getURI.getFragment, x._3.getWrittenRep.value),views(x),remove,() => update(x),uielems)
  }
  
  protected def makeDisplay(x : (FormVariant,LexicalEntry,LexicalForm)) = html("<li>" + x._1.getURI.getFragment + " -&gt; <a href=\"" + x._2.getURI + "\">"+app.namer.displayName(x._2)+"</a> ("+x._3.getWrittenRep.value+")</li>")
  
  lazy val entrySelect = new EntrySelectWindow(uielems,state,model)
  
  def dialog(x : Option[(FormVariant,LexicalEntry,LexicalForm)], after : ((FormVariant,LexicalEntry,LexicalForm)) => Result) = {
    val default = x match {
      case Some((sr,_,_)) => Some(sr.getURI.getFragment)
      case None => None
    }
    val defaultForm = x match {
      case Some((_,_,ls)) => Some(ls.getWrittenRep.value)
      case None => None
    }
    prompt().show(app, "Select form variant type", "Sense variant type",
                  (lingOnto.getFormVariant() map { _.getURI().getFragment() }).toList.sorted,
                  default, s => {
        val fv = lingOnto.getFormVariant(s)
        entrySelect.show(le => {
            prompt().show(app, "Select form", "Form",(le.getForms map { _.getWrittenRep.value }).toList.sorted,
                        defaultForm, lfRep => {
                val form = le.getForms.find(_.getWrittenRep.value == lfRep).getOrElse(throw new RuntimeException())
                after((fv,le,form))
              }
            )
          })
      });
  }
}
