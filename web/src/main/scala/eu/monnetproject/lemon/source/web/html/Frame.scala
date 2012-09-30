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

package eu.monnetproject.lemon.source.web.html

import eu.monnetproject.lemon.source.web.WebLemonEditor

import scala.xml._

/**
 * 
 * @author John McCrae
 */
object Frame {
  private def makeLangOption(l10n : String => String, iso : String, name : String, lang : String) = {
    if(iso == lang) {
      <option value={iso} selected="selected">{l10n(name)}</option>
    } else {
      <option value={iso}>{l10n("name")}</option>
    }
  }
  
  def make(root : String, main : NodeSeq, app : WebLemonEditor) = {
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>{app.l10n("lemon source")}</title>
        <link rel="stylesheet" type="text/css" href={root + "/css/Aristo.css"}/>
        <link rel="stylesheet" type="text/css" href={root + "/css/source.css"}/>
        <script type="text/javascript" src={root + "/js/jquery-1.6.2.min.js"}></script>
        <script type="text/javascript" src={root + "/js/jquery-ui-1.8.16.custom.min.js"}></script>
        <script type="text/javascript" src={root + "/js/source.js"}></script>
        <script type="text/javascript" src={root + "/js/md5.js"}></script>
      </head>
      <body>
        <div class="maincontainer">
          <div class="maincolumn">
            <div width="100%" class="header">
              <div style="text-align:right;">
                <form action={root+"/Special:Search"} style="display:inline;">
                  <input type="text" class="search" width="15em" name="query"/>
                </form>
                &nbsp;&nbsp;&nbsp;
                <select class="lang" id="lang" onchange="window.location='?lang='+$('#lang').val()">
                  {makeLangOption(app.l10n,"en","English",app.lang.toString)}
                  {makeLangOption(app.l10n,"de","German",app.lang.toString)}
                </select>
                {
                  app.userID match {
                    case Some(user) => {
                        <span class="disabled menuitem"><a class="menuitem" href={root+"/Special:Account"}>{app.l10n("Account")}</a></span>
                        <span class="menuitem"><a class="menuitem" href={root+"/logout"}>{app.l10n("Log out")}</a></span>
                      }
                    case None => {
                        <button class="menuitem" onclick="call('login')">{app.l10n("Log in")}</button>
                      }
                  }
                }
              </div>
            </div>
            <div id="main">
              {main}
            </div>
          </div>
          <div class="leftcolumn">
            <img src={root+"/images/logo.png"}/>
            <div>
              <div class="menuitem"><a class="menuitem" href={root}>{app.l10n("Home")}</a></div>
              {
                if(app.userID == None) {
                  <div class="menuitem"><a class="menuitem disabled_link">{app.l10n("Generate (log-in required)")}</a></div>
                  <div class="menuitem"><a class="menuitem disabled_link">{app.l10n("Import")}</a></div>
                } else {
                  <div class="menuitem"><a class="menuitem" onclick="call('generate')">{app.l10n("Generate")}</a></div>
                  <div class="menuitem"><a class="menuitem" onclick="call('import')">{app.l10n("Import")}</a></div>
                }
              }
              {
                if(app.showHelp) {
                  <div class="menuitem" id="help"><a class="menuitem" href="?help=off">{app.l10n("Help: on")}</a></div>
                } else {
                  <div class="menuitem" id="help"><a class="menuitem" href="?help=on">{app.l10n("Help: off")}</a></div>
                }
              }
              <div class="menuitem"><a class="menuitem" href={root+"/Special:PublicLexica"}>{app.l10n("Public Lexica")}</a></div>
              {
                if(app.userID == None) {
                  <div class="menuitem"><a class="menuitem disabled_link">{app.l10n("My Lexica")}</a></div>
                } else {
                  <div class="menuitem"><a class="menuitem" href={root+"/Special:PrivateLexica"}>{app.l10n("My Lexica")}</a></div>
                }
              }
              <div class="menuitem" onclick="hideShowDumps()">{app.l10n("Dumps")}</div>
              <div id="dumps" style="display:none;padding-left:10px;">
                <div class="menuitem"><a class="menuitem" href={root+"/Special:Dump/wiktionary.tar.bz2"}>Wiktionary</a></div>
                <div class="menuitem"><a class="menuitem" href={root+"/Special:Dump/wordnet.zip"}>WordNet</a></div>
              </div>
            </div>
          </div>
        </div>
        {
          if(app.getProgress._1 >= 0) {
            new Tracker(app).tag
          } else {
            <span/>
          }
        }
        <div id="landing_area" style="visibility:hidden;float:left;">
        </div>
      </body>
    </html>
  }
}
