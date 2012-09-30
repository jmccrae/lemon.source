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

package eu.monnetproject.lemon.source.web

import eu.monnetproject.lemon.source.app._
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.lemon.source.basic._
import eu.monnetproject.lemon.source.generation._
import eu.monnetproject.lemon.source.web.html._

/**
 * 
 * @author John McCrae
 */
object SpecialHandlers {
  private def generate(app : WebLemonEditor) = {
    new GenerateLexiconWindow(app,app.uielems).show
  }
  
  private def login(app : WebLemonEditor) = {
    val dialog = new LogInDialog(app,app.userDB)
    LoadDialogResult(app.l10n("Log in"), "logindialog", dialog.logInDialog, 400)
  }
  
  private def notimplemented(app : WebLemonEditor) = {
    NoResult
  }
  
  private def importLemon(app : WebLemonEditor) = {
    val dialog = new ImportLexiconWindow(app,app.uielems)
    dialog.show
  }
  
  def auth(app : WebLemonEditor, params : String) : Result = {
    val req = params.split(":")
    if(req.length == 3) {
      if(LogInDialog.validId(req(2))) {
        app.onLogIn(req(0),req(1),req(2)) match {
          case result : ErrorResult => result
          case result => result >> RefreshResult()
        }
      } else {
        ErrorResult("Auth code expired")
      }
    } else {
      ErrorResult("Invalid auth parameters")
    }
  }
  
  def newuser(app : WebLemonEditor, params : String) : Result = {
    val req = params.split(":")
    if(req.length == 2) {
      app.userDB.newUser(req(0),req(1)) match {
        case Some(u) => app.onLogIn(u) >> RefreshResult()
        case None => ErrorResult("User name already exists")
      }
    } else {
      ErrorResult("Invalid newuser")
    }
  }
  
  private val handlers = Map[String,WebLemonEditor => Result](
    "generate" -> generate,
    "import" -> importLemon,
    "help" -> notimplemented,
    "login" -> login
  )
  
  private val paramedHandlers = Map[String,(WebLemonEditor,String) => Result](
    "auth" -> auth,
    "newuser" -> newuser
  )
  
  def get(call : String,app : WebLemonEditor) = handlers.get(call) match {
    case Some(handler) => Some(handler(app))
    case None => None
  }
  
  def get(call : String, params : String, app : WebLemonEditor) = paramedHandlers.get(call) match {
    case Some(handler) => Some(handler(app,params))
    case None => None
  }
}
