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
package eu.monnetproject.lemon.source.web.html

import eu.monnetproject.lemon.source.app.UserDatabase
import eu.monnetproject.lemon.source.web._
import java.util.Random

/**
 * 
 * @author John McCrae
 */
class LogInDialog(app : WebLemonEditor, userDB : UserDatabase) {
  val logInDialog = {
    val cnonce = LogInDialog.newId
     <div id="logindialog" style="text-align:center;line-height:300%;" title={app.l10n("Log in")}>
      <div>
        <script type="text/javascript">
          function passwordSubmit(e) {{
            var key = e.key || e.which;
            if(key == 13) {{
              md5_login($('#username').val(),$('#password').val(),"{userDB.nonce}","{cnonce}");
            }}
          }}
        </script>
        <label for="username">{app.l10n("User name")}</label><input type="text" name="username" id="username"/><br/>
        <label for="password">{app.l10n("Password")}</label><input type="password" name="password" id="password" onkeypress="passwordSubmit(event);"/><br/>
        <button onclick={"md5_login($('#username').val(),$('#password').val(),\""+userDB.nonce+"\",\""+cnonce+"\")"} class="dialog_ok lint_ignore">{app.l10n("Log in")}</button>
        <button onclick={"md5_newuser($('#username').val(),$('#password').val(),\""+userDB.nonce+"\")"} class="dialog_other lint_ignore">{app.l10n("New user")}</button>
    </div>
   </div>
  }
  
}

object LogInDialog {
  private var blockedIds = Set[String]()
  private var liveIds = Set[String]()
  
  val random = new Random
  
  def newId : String = {
    var id = ""
    do {
      id = random.nextInt.toHexString
    } while(blockedIds.contains(id) || liveIds.contains(id))
    liveIds += id
    id
  }
  
  def validId(id : String) = {
    if(!blockedIds.contains(id) && liveIds.contains(id)) {
      liveIds -= id
      blockedIds += id
      true
    } else {
      false
    }
  }
}
//  val logInDialog = 
//    <div id="logindialog" style="text-align:center;line-height:300%;" title={app.l10n("Log in")}>
//      <div>
//        <img src={SourceWebServlet.path+"/images/google.ico"}/>
//        <a href={SourceWebServlet.path + "/login?provider=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid"}>{app.l10n("Log in with Google")}</a>
//      </div>
//      <div>
//        <img src={SourceWebServlet.path+"/images/yahoo.ico"}/>
//        <a href={SourceWebServlet.path + "/login?provider=yahoo.com"}>{app.l10n("Log in with Yahoo")}</a>
//      </div>
//      <div>
//        <img src={SourceWebServlet.path+"/images/myopenid.ico"}/>
//        <a href={SourceWebServlet.path + "/login?provider=myopenid.com"}>{app.l10n("Log in with MyOpenID")}</a>
//      </div>
//      <div>
//        <form action={SourceWebServlet.path+"/login"} method="get">
//          <img src={SourceWebServlet.path+"/images/openid.ico"}/>
//          {app.l10n("Provider:")}<input type="text" name="provider"/>
//          <input type="submit" value={app.l10n("OK")}/>
//        </form>
//      </div>
//    </div>
//}
