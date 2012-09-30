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

import eu.monnetproject.lang.Language
import eu.monnetproject.lemon.LemonModel
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.relations._
import eu.monnetproject.util.Logging
import java.net.URI
import scala.collection.JavaConversions._

trait FormType

object FormTypes {
  private class FormTypeImpl(name : String) extends FormType {
    override def toString = name
  }
  val Canonical : FormType = new FormTypeImpl("Canonical")
  val Other : FormType = new FormTypeImpl("Other")
  val Abstract : FormType = new FormTypeImpl("Abstract")
  val Lexical : FormType = new FormTypeImpl("Lexical")
}

case class FormWithType(val form : LexicalForm, val formType : FormType)

/** 
 * Controls the display of forms
 * @author John McCrae
 */
class FormController[C,O<:C](val state : State, val entry : LexicalEntry, uielems : UIElems[C,O]) extends 
StdController[FormWithType,C,O](new FormModel(entry), new FormView[C,O](state,uielems,entry))

class FormModel(entry : LexicalEntry) extends StdModel[FormWithType] {
  import FormTypes._
  private val log = Logging.getLogger(this)
  
  def add(form : FormWithType) = {
    log.info("add:"+form)
    if(entry.getCanonicalForm == null) {
      entry.setCanonicalForm(form.form)
    } else {
      form match {
        case FormWithType(f,Canonical) => entry.setCanonicalForm(f)
        case FormWithType(f,Other) => entry.addOtherForm(f)
        case FormWithType(f,Abstract) => entry.addAbstractForm(f)
        case FormWithType(f,Lexical) => entry.addForm(f)
      }
    }
  }
  
  def remove(form : FormWithType) = {
    log.info("remove:"+form)
    if(form.form == entry.getCanonicalForm) {
      entry.setCanonicalForm(null)
    } else {
      form match {
        case FormWithType(f,Canonical) => 
        case FormWithType(f,Other) => entry.removeOtherForm(f)
        case FormWithType(f,Abstract) => entry.removeAbstractForm(f)
        case FormWithType(f,Lexical) => entry.removeForm(f)
      }
    }
  }
  
  def elements = {
    (if(entry.getCanonicalForm != null) {
        Seq(FormWithType(entry.getCanonicalForm,Canonical)
        )
      } else {
        Nil
      }) ++
    (entry.getAbstractForms map { f => FormWithType(f,Abstract)}) ++
    (entry.getOtherForms map { f => FormWithType(f,Other)}) ++
    (for{
        form <- entry.getForms() 
        if form != entry.getCanonicalForm && 
        !entry.getAbstractForms.contains(form) &&
        !entry.getOtherForms.contains(form)
      } yield FormWithType(form,Lexical))
  }
}

class FormView[C,O<:C](state : State, val uielems : UIElems[C,O], entry : LexicalEntry) extends HasState(state) with StdPanel[FormWithType,C,O] {
  val title = "Forms"
    
  val addText = "Add Form"
    
  val removeText = "Remove Form"
  val description = "A form is the text of the entry. Multiple forms are normally used to indicate inflectional variants of a term. Orthographics variants, such as spelling differences are not considered different forms. All the forms of an entry should have the same words in the same order."
  
  protected override def views(form : FormWithType) : Seq[StdController[_,C,O]] = 
    List(new PropertyController(state,form.form,uielems,URI.create(LemonModel.LEMON_URI+"LexicalForm")),
         new FormVariantController(state,form.form,uielems)
    )
  
  protected def makeElem(form : FormWithType, removeAction : () => Result, updateAction : FormWithType => Result,changeAction : FormWithType => Unit) = { 
    val elem = new FormElement(form,
                               views(form),
                               removeAction,
                               s => { 
        form.form.setWrittenRep(new Text(s,form.form.getWrittenRep.language))
      }, s => {
        changeAction(FormWithType(form.form,s))
      },uielems)      
//    val propertyView = new PropertyView[LexicalForm,C,O](this,form.form,URI.create(LemonModel.LEMON_URI+"Form"),true,uielems)
//    elem addComponent propertyView.panel
//    propertyView.initialize
//    val formVariantsView = new FormVariantsView(this,form.form,true,uielems)
//    elem addComponent formVariantsView.panel
//    formVariantsView.initialize
    elem
  }
  
  protected def makeDisplay(form : FormWithType) = uielems.html("<li>(<i>" + form.formType + "</i>) " + form.form.getWrittenRep.value + "</li>") 
  
  def dialog(elem : Option[FormWithType], after : (FormWithType) => Result) = {
    uielems.prompt().show(app,app.l10n("Add form"),app.l10n("New form")
                          ,elem.map(_.form.getWrittenRep.value).getOrElse(""), s => {
        val f = lemonFactory.makeForm(app.namer.name(entry,"form"))
        f.setWrittenRep(new Text(s,state.lexicon.getLanguage))
        after(FormWithType(f,FormTypes.Other))
      });
  }
}
