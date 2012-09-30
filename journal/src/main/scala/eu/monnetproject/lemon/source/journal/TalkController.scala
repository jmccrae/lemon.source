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
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.User
import eu.monnetproject.lemon.source.app.UserDatabase
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import eu.monnetproject.util._
import java.text._
import java.util.Date
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
object Anon extends User {
  def id = "Anonymous"
  def auth = ""
  def firstName = None
  def firstName_=(v : Option[String]) { throw new UnsupportedOperationException("Modifying the anon user... not a good idea") }
  def lastName = None
  def lastName_=(v : Option[String]) { throw new UnsupportedOperationException("Modifying the anon user... not a good idea") }
  def displayName = "Anonymous"
  def verifyUser(reqAuth : String, nonce : String) = false
}

case class TalkEntry(val user : User, val time : Date, val comment : String)

class TalkController[C,O<:C](state : State, element : LemonElement, journaller : Journaller, userDB : UserDatabase, uielems : UIElems[C,O])
extends StdController[TalkEntry,C,O](new TalkModel(element,journaller,userDB,state.app.userID.getOrElse(Anon)), new TalkView(state,state.app.userID.getOrElse(Anon),uielems))

class TalkModel(element : LemonElement, journaller : Journaller,  userDB : UserDatabase, user : User)
extends StdModel[TalkEntry] {  
  private def elementName : String = element.getURI match {
    case null => "_:"+element.getID
    case uri => uri.toString
  }  
  private implicit def tuple2Pair(t : (String,String)) = new Pair[String,String]() {
    def getFirst = t._1
    def getSecond = t._2
  }
  
  def add(entry : TalkEntry) = {
    val je = new JournalEntry(user.id,List("element"->elementName,"comment"->entry.comment):List[Pair[String,String]])
    journaller.journal(je)
  }
  
  def remove(entry : TalkEntry) = {
    throw new UnsupportedOperationException()
  }
  
  val js = new JournalSignature("element","comment")
  
  def elements = (for(je <- journaller.getEntries("element", elementName, js)) yield {
      TalkEntry(userDB.get(je.getSource).getOrElse(Anon),
              je.getTimeStamp,
              je.getEntries.find(_.getFirst == "comment") match {
        case Some(e) => e.getSecond
        case None => "#ERROR#"
      })
  }).toSeq
}

class TalkView[C,O<:C](state : State, user : User, val uielems : UIElems[C,O])
extends HasState(state) with StdPanel[TalkEntry,C,O] {
  import UIElems._
  import uielems._
  
  val dateFormat = DateFormat.getDateTimeInstance
  val title = "Comments"
  val addText = "Add Comment"
  val removeText = "Remove Comment"
  val description = "Comments to this entry"
  val addComment = new AddCommentWindow(app,uielems)
  
  protected def makeElem(entry : TalkEntry, 
               remove : () => Result,
               update : TalkEntry => Result,
               change : TalkEntry => Unit) = {
    if(entry.user == user) {
      new TalkElement(entry,remove,Some(() => update(entry)),uielems)
    } else {
      new TalkElement(entry,remove,None,uielems)
    }
  }
  
  protected def makeDisplay(entry : TalkEntry) = {
    html("")
  }
  
  private def wrap(s : List[String]) = s match {
    case List(c) => Some(TalkEntry(user,new Date(),c))
    case _ => None
  }
  
  def dialog(value : Option[TalkEntry], after : (TalkEntry) => Result) : Result = {
    addComment show {
      comment => wrap(comment:: Nil) match {
        case Some(te) => after(te)
        case None => NoResult
      }
    }
  }
}