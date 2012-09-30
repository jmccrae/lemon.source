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

import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.common._
import java.net.URI
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class StatusController (val view : AbstractStatusView, entry : LexicalEntry) 
extends HasState(view) {
  private val reviewStatus = "http://monnetproject.deri.ie/reviewstatus"
  private val reviewStatusURI = URI.create(reviewStatus)
  
  def entryStatus = entry.getAnnotations(reviewStatusURI).headOption match {
    case Some(uri : URI) => uri
    case None => URI.create(reviewStatus+"/unknown")
  }
  
  def statusImg(status : URI) = {
    "review_" + status.toString.substring(status.toString.lastIndexOf("/")+1) + ".png"
  }
  
  def onclick : Result = {
    view.dialog {
      newReviewStatus => {
        val oldStatuses = List(entry.getAnnotations(reviewStatusURI).toSeq:_*)
        for(oldStatus <- oldStatuses) {
          entry.removeAnnotation(reviewStatusURI, oldStatus)
        }
        entry.addAnnotation(reviewStatusURI, newReviewStatus)
        view.setButtonImg(statusImg(newReviewStatus))
        new ChangeStatusResult(view.name)
      }
    }
  }
}

trait AbstractStatusView extends State with UIView[URI] {
  def setButtonImg(image : String) : Unit
  def dialog(after : URI => Result) : Result
  def deselect(elem : URI) = NoResult
  def select(elem : URI) = NoResult
  def initialize { }
  def setEnabled(isEnabled : Boolean) = NoResult
  def add(elem : URI) = NoResult
  def remove(elem : URI) = NoResult
  def update(oldElem : URI, newElem : URI) = NoResult
  def reset = NoResult
  def dialog(elem : Option[URI], after : (URI) => Result) = NoResult
  def name : String
  
}
