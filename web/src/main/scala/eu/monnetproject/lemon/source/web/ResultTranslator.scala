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
import eu.monnetproject.lemon.source.web.html._
import eu.monnetproject.util._
import scala.xml._

/**
 * 
 * @author John McCrae
 */
object ResultTranslator {
  import UIElems._
  private val log = Logging.getLogger(this)
  private def callBackPath(callIds : Stream[String]) = SourceWebServlet.path+"/ajax_onetime?id="+callIds.head
  
  def translate(result : Result, callId : Stream[String], deployServer : String) : (String,Seq[Option[NodeSeq]],Stream[String]) = result match {
    case LoadDialogResult(caption, dialogId, contents,widthPx) => {
        ("$('#landing_area').load('"+callBackPath(callId)+"', function() { $('#"+dialogId+"').dialog({'modal':true, 'width':"+widthPx+"}); });", 
         Seq(Some(contents)),callId.tail
        )
    }
    case AddComponentStyle(id,style) => ("$('#"+id+"').addClass('"+style+"');",Seq(None),callId.tail)
    case RemoveComponentStyle(id,style) => ("$('#"+id+"').removeClass('"+style+"');",Seq(None),callId.tail)
    case EnableComponent(id,enabled) => if(enabled) {
      ("$('#"+id+"').removeAttr('disabled');",Seq(None),callId.tail)  
    } else {
      ("$('#"+id+"').attr('disabled','disabled');",Seq(None),callId.tail)  
    }
    case PopupComponents(parentId,comps) => {
        val subtag = <ul class="popup">
          {for(comp <- comps) yield {
              <li>{comp.html}</li>
            }
          }
        </ul>
        ("$('<span/>').appendTo($('#"+parentId+"')).load('"+callBackPath(callId)+"');",Seq(Some(subtag)),callId.tail)
    }
    case ChangeContents(id,newContent) => {
        ("$('#"+id+"').load('"+callBackPath(callId)+"');",Seq(Some(newContent)),callId.tail)
      }
    case AppendContents(id,newContent) => {
        ("$('<span/>').appendTo($('#"+id+"')).load('"+callBackPath(callId)+"');",Seq(Some(newContent)),callId.tail)
    }
    case RemoveElement(id) => {
        ("$('#"+id+"').remove();",Seq(None),callId.tail)
    }
    case ChangeProperty(id,key,value) => {
        ("$('#"+id+"').prop('"+key+"','"+value+"');",Seq(None),callId.tail)
      }
    case NoResult => ("",Seq(None),callId.tail)
    case OKResult => ("noop();",Seq(None),callId.tail)
    case EnableComp(comp,enabled) => {
        val id = comp.asInstanceOf[WebComponent].id
        if(enabled) {
          ("$('"+id+"').removeAttr('disabled');",Seq(None),callId.tail)
        } else {
          ("$('"+id+"').attr('disabled','disabled');",Seq(None),callId.tail)
        }
    }
    case ReloadPanel(panel : WebContainer) => {
        ("$('#"+panel.id+"').load('"+callBackPath(callId)+"');",Seq(Some(panel.html)),callId.tail)
    }
    case ErrorResult(message) => {
        ("showError('"+message+"');",Seq(None),callId.tail)
    }
    case StartTrackerResult(tracker) => {
        ("$('<span/>').appendTo($('body')).load('"+callBackPath(callId)+"');",Seq(Some(tracker.asInstanceOf[WebComponent].html)),callId.tail)
    }
    case ChangeStatusResult(view) => {
        ("showError('todo (change status)');",Seq(None),callId.tail)
    }
    case OpenResourceResult(fileName,inputstream,mimeType) => {
        throw new IllegalArgumentException("OpenResource")
    }
    case SendRedirectResult(path) => {
        if(path.startsWith(deployServer)) {
          ("window.location='"+(path.drop(deployServer.length))+"';",Seq(None),callId.tail)
        } else {
          ("window.location='"+path+"';",Seq(None),callId.tail)
        }
        
    }
    case RefreshResult() => {
        ("location.reload(true);",Seq(None),callId.tail)
    }
    case multiple : MultipleResult => {
        var cid = callId
        val results = for(res <- multiple.results) yield {
          val r = translate(res,cid,deployServer)
          cid = r._3
          (r._1,r._2)
        }
        (
          {
            val sb = toBuilder("")
            for(result <- results) {
              sb ++= result._1
            }
            sb.toString
          },
          results flatMap (_._2),
          callId.drop(results.length)
        )
    }
    case unknown => {
        ("showError('unknown result " + unknown + "');",Seq(None),callId.tail)
    }
  }
}
