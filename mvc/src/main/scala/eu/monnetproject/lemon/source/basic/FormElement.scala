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

package eu.monnetproject.lemon.source.basic

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.common.UIElems._
import eu.monnetproject.lemon.source.basic._
import eu.monnetproject.lemon.source.basic.FormTypes._
/**
 * 
 * @author John McCrae
 */
class FormElement[Component,Container<:Component](form : FormWithType,
                                                  views : Seq[StdController[_,Component,Container]],
                                                  deleteAction : () => Result,
                                                  formChangeAction : (String) => Unit, typeChangeAction : (FormType) => Unit,
                                                  uielems : UIElems[Component,Container]) 
extends StdElement(views,deleteAction,uielems) {
  import uielems._

  protected def main = {
    uielems.select(nolocalize(""),Some(form.formType))(
      Canonical -> "Canonical",
      Other -> "Other",
      Abstract -> "Abstract",
      Lexical -> "No Pref"
    )(e => { typeChangeAction(e) ; NoResult}).components.toList :::
    List(immediateField(nolocalize(""), Some(form.form.getWrittenRep.value)) {
        select
      } {
        e => formChangeAction(e) ; NoResult
      })
  }
}
 

