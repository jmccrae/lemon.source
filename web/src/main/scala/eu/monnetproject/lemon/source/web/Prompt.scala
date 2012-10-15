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

import eu.monnetproject.lemon.source.app._
import eu.monnetproject.lemon.source.common._

/**
 * 
 * @author John McCrae
 */
class Prompt(uielems : WebUIElem) extends AbstractPrompt {
  import uielems._
  import UIElems._
  
  def show(app : LemonEditorApp, caption : String, prompt : String, 
           notify : Boolean => Result) = {
    val id = randId()
    val tag = <div id={id} class="dialog" title={app.l10n(caption)}>
      <div>{app.l10n(prompt)}</div>
      <div>
        <button class="dialog_cancel" onclick={"callAction('"+id+"','false');doClose('"+id+"');"}>{app.l10n("No")}</button>
        <button class="dialog_ok lint_ignore" onclick={"callAction('"+id+"','true');doClose('"+id+"');"}>{app.l10n("Yes")}</button>
      </div>
              </div>
    app.asInstanceOf[WebLemonEditor].navigator.addHandler(id, Some("true"),{ notify(true) })
    app.asInstanceOf[WebLemonEditor].navigator.addHandler(id, Some("false"),{ notify(false) })
    new LoadDialogResult(app.l10n(caption),id,tag,400)
  }
  
  def show(app : LemonEditorApp, caption : String, prompt : String, 
           default : String, notify : String => Result) = {
    dialog(caption,field(prompt,Some(default)))(notify)
  }
  
  def show(app : LemonEditorApp, caption : String, prompt : String, options : List[String], 
           default : Option[String], notify : String => Result) = {
    dialog(caption,select(prompt,default)((options zip options):_*)(s => NoResult))(notify)
  }
        
  def delete(app : LemonEditorApp, after : () => Result) = {
    val id = randId()
    val tag = <div id={id} class="dialog" title={app.l10n("Confirm Deletion")}>
      <div>{app.l10n("Are you sure you want to delete this element? Changes cannot be undone")}</div>
      <div>
        <button class="dialog_cancel" onclick={"doClose('"+id+"');"}>{app.l10n("No")}</button>
        <button class="dialog_ok lint_ignore" onclick={"call('"+id+"');doClose('"+id+"');"}>{app.l10n("Yes")}</button>
      </div>
              </div>
    app.asInstanceOf[WebLemonEditor].navigator.addHandler(id, None,{ after() })
    new LoadDialogResult(app.l10n("Confirm Deletion"),id,tag,400)
  }
}
