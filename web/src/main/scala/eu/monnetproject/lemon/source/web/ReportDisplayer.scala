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

import eu.monnetproject.lemon.generator._
import scala.collection.JavaConversions._

/**
 * @author John McCrae
 */
class ReportDisplayer(app : WebLemonEditor) {

  def display(report : GenerationReport) = {
    <div id="generation_report_dialog">
      <div class="report_title">{app.l10n("Generation result")}</div>
      <div style="max-height:640px;overflow-y:scroll;">
      <ul>{
          for((uri,entryReport) <- report.getEntries) yield {
            <li>{uri.toString} - {entryReport.getEntryURI.toString}</li>
            <ul>{
                for(actorReport <- entryReport.getActorReports) yield {
                  actorReport.getStatus match {
                    case "OK" => <li><span class="report_ok">{actorReport.getActorName}</span> {actorReport.getMessage}</li>
                    case "UNNECESSARY" => <li><span class="report_unnecessary">{actorReport.getActorName}</span> {actorReport.getMessage}</li>
                    case "NO_INFO" => <li><span class="report_noinfo">{actorReport.getActorName}</span> {actorReport.getMessage}</li>
                    case "FAILED" => <li><span class="report_failed">{actorReport.getActorName}</span> {actorReport.getMessage}</li>
                    case "EXCEPTION" => <li><span class="report_exception">{actorReport.getActorName}</span> {actorReport.getMessage}</li>
                    case _ => <li><span class="report_exception">{actorReport.getActorName}</span> {actorReport.getMessage}</li>
                  }
                }
              }</ul>
          }
        }</ul>
      </div>
      <div class="generation_report_footer"><a href={SourceWebServlet.path+ "/Special:LastGeneratedLexicon"}>{app.l10n("Download")}</a></div>
    </div>
  }
}
