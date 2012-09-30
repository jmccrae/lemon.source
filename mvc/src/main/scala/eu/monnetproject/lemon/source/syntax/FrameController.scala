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
import java.net.URI
import scala.collection.JavaConversions._
import eu.monnetproject.util.Logging

class FrameController[C,O<:C](state : State, entry : LexicalEntry, uielems : UIElems[C,O])
extends StdController[Frame,C,O](new FrameModel(state,entry), new FrameView(state,entry,uielems))

class FrameModel(state : State, entry : LexicalEntry) extends StdModel[Frame] {
  def add(frame : eu.monnetproject.lemon.model.Frame) = {
    entry.addSynBehavior(frame)
    val frameClass = frame.getTypes.headOption.getOrElse(throw new RuntimeException)
    if(frame.getSynArgs.isEmpty) {
      for(synArg <- state.lingOnto.getSynArgsForFrame(frameClass)) {
        frame.addSynArg(synArg, state.lemonFactory.makeArgument(state.app.namer.name(entry,"arg")))
      }
    }
  }
  
  def remove(frame : Frame) = {
    entry.removeSynBehavior(frame)
    for{(synArg,args) <- frame.getSynArgs.toSeq
        arg <- args} {
      frame.removeSynArg(synArg,arg)
    }
  }
  
  def elements = entry.getSynBehaviors.toSeq
}

class FrameView[C,O<:C](state : State, entry : LexicalEntry, val uielems : UIElems[C,O])
extends HasState(state) with StdPanel[Frame,C,O] {
  private val log = Logging.getLogger(this)
  import UIElems._
  import uielems._
  val title = "Syntactic Behaviors"
  val addText = "Add Frame"
  val removeText = "Remove Frame"
  val description = "A frame is a prototypical syntactic usage of an entry. For example, the transitive or intransitive usage of a verb."
  
  private def frameClass(frame : Frame) = frame.getTypes.headOption.getOrElse(throw new RuntimeException).getFragment
  
  def makeElem(frame : Frame,
               remove : () => Result,
               update : Frame => Result,
               change : Frame => Unit) = {
    new StdColElement(List(frameClass(frame)),views(frame),remove, () => update(frame),uielems)
  }
  
  override def views(frame : Frame) = List(
    new CorrespondenceController(state,frame,entry,uielems)
  )
  
  def makeDisplay(frame : Frame) = html("<li>" + frameClass(frame)+"</li>")
  
  def dialog(frame : Option[Frame], after : Frame => Result) = {
    val defFrameClass = frame match {
      case Some(f) => Some(f.getTypes.headOption.getOrElse(throw new RuntimeException()))
      case None => None
    }
    val fcs = (for(fc <- lingOnto.getFrameClasses) yield {
            (fc,fc.getFragment)
          }).toList.sortBy(_._2)
   
    def exampleFunc(e : URI) : String = {
      "e.g., " + app.lingOnto.getExamples(e).headOption.map(_.value).getOrElse("")
    }
    uielems.dialog("Set frame class", 
                   uielems.select("Frame class",defFrameClass,exampleFunc)(fcs:_*)(e => NoResult)
    ) {
      frameClass => {
        val newFrame = lemonFactory.makeFrame(app.namer.name(entry,"frame"))
        newFrame.addType(frameClass)
        after(newFrame)
      }
    }              
  }
}

//
//case class FrameSensePair(val frame : Frame, val senses : List[LexicalSense])
//
///**
// * Controller for frames
// * @author John McCrae
// */
//class FrameController(val view : UIView[FrameSensePair] with State, entry : LexicalEntry)
//extends HasState(view) with GenericController[FrameSensePair] {
//  private val log = Logging.getLogger(this)
//  val title = "Syntactic Behaviors"
//  val addText = "Add Frame"
//  val removeText = "Remove Frame"
//  
//  private lazy val frameURIs = Set[URI]() ++ lingOnto.getFrameClasses()
//  
//  private def frameClass(frame : Frame) : Option[URI] = frame.getTypes() find { frameType =>
//    frameURIs contains frameType
//  } 
//  
//  def unwrap(frame : FrameSensePair) = frameClass(frame.frame) match {
//    case Some(frameType) => frameType.getFragment() :: ((frame.senses map {
//            sense => sense.getReference().toString()
//          }).toList)
//    case None => List(app.l10n("FRAME TYPE NOT FOUND"))
//  }
//  
//  def wrap(vals : List[String]) : Option[FrameSensePair] = vals match {
//    case frameName :: senseNames => {
//        frameURIs find { 
//          _.getFragment() == frameName 
//        } match {
//          case Some(frameType) => {
//              val frame : Frame = entry.getSynBehaviors() find { 
//                x => (x.getTypes() find { 
//                    y => y.toString() == frameType 
//                  }) != None 
//              } match {
//                case Some(f) => f
//                case None => {
//                    val f = lemonFactory.makeFrame()
//                    f.addType(frameType)
//                    f
//                  }
//              }
//              Some(FrameSensePair(frame,(entry.getSenses() filter { 
//                      sense => senseNames.contains(sense.getReference().toString) 
//                    }).toList))
//            }
//          case None => log.warning("Did not locate frame called " + frameName) ; None
//        }
//      }
//    case _ => log.severe("Unexpected list length") ; None
//  }
//  
//  def load = {
//    val frameSensePairs = (entry.getSynBehaviors map {
//        frame => {
//          (frame, entry.getSenses filter {
//              sense => !(frame.getSynArgs().values().flatMap(x => x).toSet & (Set() ++ sense.getSubjOfProps() ++ sense.getObjOfProps() ++ sense.getIsAs())).isEmpty
//            })
//        }
//      }) foreach {
//      case (frame, senses) => view.add(FrameSensePair(frame,senses.toList))
//    }
//  }
//  
//  def add(frameSense : FrameSensePair) = {
//    entry.addSynBehavior(frameSense.frame)
//    val synArgs : List[SynArg] = lingOnto.getSynArgsForFrame(frameClass(frameSense.frame).get).toList
//    frameSense.senses match {
//      case Nil => for(synArg <- synArgs) {
//          val arg = lemonFactory.makeArgument()
//          frameSense.frame.addSynArg(synArg,arg)
//        }
//      case _ => for(sense <- frameSense.senses) {
//          synArgs.size() match {
//            case 1 => {
//                val arg = lemonFactory.makeArgument()
//                sense.addIsA(arg)
//                frameSense.frame.addSynArg(synArgs(0),arg)
//              }
//            case 2 => {
//                val arg1 = lemonFactory.makeArgument()
//                val arg2 = lemonFactory.makeArgument()
//                sense.addSubjOfProp(arg1)
//                sense.addObjOfProp(arg2)
//                synArgs partition { 
//                  synArg => synArg.getURI().toString().contains("ubj") ||	
//                  synArg.getURI().toString().contains("copulativeArg") 
//                } match {
//                  case (Buffer(subject : SynArg),Buffer(objekt : SynArg)) => {
//                      frameSense.frame.addSynArg(subject,arg1)
//                      frameSense.frame.addSynArg(objekt,arg2)
//                    }
//                  case _ => {
//                      log.warning("Unexpected partioning of subject/object")
//                      frameSense.frame.addSynArg(synArgs(0),arg1)
//                      frameSense.frame.addSynArg(synArgs(1),arg2)
//                    }
//                }
//              }
//            case _ => { 
//                log.warning("Unsupported number of arguments")
//              }
//          }
//        }
//    }
//    view.add(frameSense)
//  }
//  
//  def remove(frameSense : FrameSensePair) = {
//    for((synArg,args) <- frameSense.frame.getSynArgs()) {
//      for(arg <- args) {
//        for(sense <- frameSense.senses) {
//          sense.removeIsA(arg)
//          sense.removeSubjOfProp(arg)
//          sense.removeObjOfProp(arg)
//        }
//        frameSense.frame.removeSynArg(synArg,arg)
//      }
//    }
//    entry.removeSynBehavior(frameSense.frame)
//    view.remove(frameSense)
//  }
//  
//  def updateFrame(fsp : FrameSensePair, frameName : String) : Result = {
//    frameClass(fsp.frame) match {
//      case Some(fc) => { 
//          remove(fsp)
//          fsp.frame.removeType(fc)
//        }
//      case None => remove(fsp)
//    }
//    frameURIs find { 
//      _.getFragment() == frameName
//    } match {
//      case Some(fc) => fsp.frame.addType(fc)
//      case None => throw new RuntimeException("Invalid frame type")
//    }
//    add(fsp)
//    view.add(fsp)
//  }
//}
