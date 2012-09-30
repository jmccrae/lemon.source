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
package eu.monnetproject.lemon.source.journal

import eu.monnetproject.journalling._
import eu.monnetproject.lemon.model.LemonElement
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.util._
import java.net.URI
import java.text._
import java.util.Date
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class ChangelogController[C,O<:C](state : State, element : LemonElement, journaller : Journaller, uielems : UIElems[C,O])
extends StdController[JournalEntry,C,O](new ChangelogModel(element,journaller), new ChangelogView(state,uielems)) 

class ChangelogModel(element : LemonElement, journaller : Journaller) extends StdModel[JournalEntry] {
  private val js = new JournalSignature("type","subject","predicate","object")
   def add(entry : JournalEntry) = NoResult
   def remove(entry : JournalEntry) = NoResult
   private def elementName : String = element.getURI match {
    case null => "_:"+element.getID
    case uri => uri.toString
  }
   def elements = journaller.getEntries("subject", elementName, js).toSeq 
}

class ChangelogView[C,O<:C](state : State, val uielems : UIElems[C,O])
extends HasState(state) with StdPanel[JournalEntry,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Changes"
  val addText = ""
  val removeText = ""
  val description = "Any changes that have been made to the page"
  val dateFormat = DateFormat.getDateTimeInstance
  
  private def unwrap(elem : JournalEntry) = {
    elem.getSource :: (dateFormat.format(elem.getTimeStamp)) :: 
    (elem.getEntries find (_.getFirst == "type") match {
        case Some(e) => e.getSecond
        case None => "#ERROR#"
      }) ::
    (elem.getEntries find (_.getFirst == "predicate") match {
        case Some(e) => e.getSecond
        case None => "#ERROR#"
      }) ::
    (elem.getEntries find (_.getFirst == "object") match {
        case Some(e) => e.getSecond
        case None => "#ERROR#"
      }) :: Nil
  
  }
  
  override val canAdd = false
  
  protected def makeElem(elem : JournalEntry,
                         remove : () => Result,
                         update : JournalEntry => Result,
                         change : JournalEntry => Unit) = {
    new StdColElement(unwrap(elem),Nil,remove,() => update(elem),uielems) {
      override def actions = Nil
    }
  } 
  
  protected def makeDisplay(elem : JournalEntry) = html("")
  
  def dialog(elem : Option[JournalEntry], after : (JournalEntry) => Result) = NoResult
}