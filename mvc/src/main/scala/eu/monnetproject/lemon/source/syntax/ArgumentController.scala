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
package eu.monnetproject.lemon.source.syntax

import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.common._
import scala.collection.JavaConversions._
import eu.monnetproject.util.Logging


/**
 * Controller for arguments
 * @author John McCrae
 */
class ArgumentController[C,O<:C](state : State, frame : Frame, sense : LexicalSense, uielems : UIElems[C,O]) 
extends StdController[(SynArg,Argument),C,O](new ArgumentModel(frame,sense), new ArgumentView(state,frame,sense,uielems))

class ArgumentModel(frame : Frame, sense : LexicalSense) extends StdModel[(SynArg,Argument)] {
  private val log = Logging.getLogger(this)
  def add(arg : (SynArg,Argument)) = {
    frame.addSynArg(arg._1,arg._2) 
  }
  def remove(arg : (SynArg,Argument)) = {
    frame.removeSynArg(arg._1,arg._2)
  }
  def elements = (for {
      (argType,args) <- frame.getSynArgs.toSeq
      arg <- args
    } yield (argType,arg)).toSeq
}

class ArgumentView[C,O<:C](state : State, frame : Frame, sense : LexicalSense, val uielems : UIElems[C,O]) 
extends HasState(state) with StdPanel[(SynArg,Argument),C,O] {
  import UIElems._
  import uielems._
  
  val log = Logging.getLogger(this)
  
  val title = "Argument Map"
  val addText = "xxx"
  val removeText = "xxx"
  val description = "An argument indicates a slot in either a syntactic frame or in a triple or set of triples in instance data based on the ontology."
  
  protected override def canAdd = false
  
  def semType(synArg : SynArg) : String = {
    for(arg <- frame.getSynArg(synArg)) {
      if(sense.getIsAs.contains(arg)) {
        return "isA"
      } else if(sense.getSubjOfProps.contains(arg)) {
        return "subjOfProp"
      } else if(sense.getObjOfProps.contains(arg)) {
        return "objOfProp"
      }
    }
    return "Unknown"
  }
  
  protected def makeElem(synArg : (SynArg,Argument),
                         remove : () => Result,
                         update : ((SynArg,Argument)) => Result,
                         change : ((SynArg,Argument)) => Unit) = {
    val allSynArgs = for((synArg,args) <- frame.getSynArgs.toSeq ; arg <- args) yield ((synArg,arg), synArg.getURI.getFragment)
    new StdElement(views(synArg),remove,uielems) {
      def main = List(
        label(semType(synArg._1)),
        vertical()((uielems.select("",Some(synArg))(allSynArgs:_*)(e => {change(e) ; NoResult})).components:_*)
      )
      override def actions = Nil
    }
  }
  
  protected def makeDisplay(synArg: (SynArg,Argument)) = html(
    "<li>"+ semType(synArg._1) + " &lt;-&gt; " + synArg._1.getURI.getFragment + "</li>"
  )
  
  def dialog(x : Option[(SynArg,Argument)], after : ((SynArg,Argument)) => Result) = NoResult
}

//class ArgumentController(val view : UIView[Argument] with State, frame : Frame, sense : Option[LexicalSense])
//    extends HasState(view) with GenericController[Argument] {
//  private val log = Logging.getLogger(this)
//  val title = "Arguments"
//  val addText = "Add arguments"
//  val removeText = "" 
//  
//  def unwrap(arg : Argument) = {
//    List(frame.getSynArgs find { 
//      case (synArg,args) => args.contains(arg)
//    } match {
//      case Some(arg) => arg._1.getURI().getFragment()
//      case None => "ERROR"
//    },
//    sense match {
//      case Some(s) => {
//        //log.warning("unwrapping" + s.getURI())
//        if(s.getIsAs() contains arg) {
//          "isA"
//        } else if(s.getSubjOfProps() contains arg) {
//          "subjOfProp"
//        } else if(s.getObjOfProps() contains arg) {
//          "objOfProp"
//        } else {
//         "ERROR"
//        }
//      }
//      case None => app.l10n("No sense")
//    },
//    arg.getMarker match {
//      case null => app.l10n("No marker")
//      case entry : LexicalEntry => entry.getURI().getFragment() + " [Lexical Entry]"
//      case value : PropertyValue => value.getURI().getFragment() + " [Property Value]"
//    })  
//  }
//  
//  val lexEntry = """(.*) \[Lexical Entry\]"""r
//  val propValue = """(.*) \[Property Value\]"""r
//  
//  def wrap(values : List[String]) = values match {
//    case List(synArg,semArg,marker) => {
//      val argument = lemonFactory.makeArgument()
//      marker match {
//        case x : String if x == app.l10n("No marker") =>
//        case lexEntry(entryName) => {
//          app.namer.entryForName(lexiconName,entryName) match {
//            case Some(entry) => argument setMarker entry
//            case None =>
//          }
//        }
//        case propValue(propValName) => {
//          val propVal = lingOnto.getPropertyValue(propValName)
//          argument setMarker(propVal)
//          
//        }
//        case _ => throw new IllegalArgumentException()
//      }
//      Some(argument)
//    }
//    case _ => throw new IllegalArgumentException()
//  }
//  
//  def add(arg : Argument) = NoResult
//  
//  def remove(arg : Argument) = NoResult
//  
//  def load {
//    for((synArg, args) <- frame.getSynArgs()) {
//      for(arg <- args) {
//        view.add(arg)
//      }
//    }
//  }
//  
//  override def addAction = None
//  override def removeAction = None
//}
