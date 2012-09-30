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
package eu.monnetproject.lemon.source.semantics

import eu.monnetproject.lang.Language
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.State
import eu.monnetproject.lemon.source.app.HasState
import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.common._
import scala.collection.JavaConversions._

/**
 * Controller for examples
 * @author John McCrae
 */
class ExampleController[C,O<:C](state : State, sense : LexicalSense, uielems : UIElems[C,O])
extends StdController[Example,C,O](new ExampleModel(sense), new ExampleView(state,uielems,sense))

class ExampleModel(sense : LexicalSense) extends StdModel[Example] {
  def add(example : Example) = sense.addExample(example)
  def remove(example : Example) = sense.addExample(example)
  def elements = sense.getExamples.toSeq
}

class ExampleView[C,O<:C](state : State, val uielems : UIElems[C,O],sense : LexicalSense) 
extends HasState(state) with StdPanel[Example,C,O] {
  import UIElems._
  import uielems._
  
  val title = "Examples"
  val addText = "Add example"
  val removeText = "Remove example"
  val description = "This is an example of the usage of the word with this meaning"
  
  protected def makeElem(example : Example,
                         remove : () => Result,
                         update : Example => Result,
                         change : Example => Unit) = {
    new StdColElement(List(example.getValue.value),views(example),
                      remove,() => update(example),uielems)
  }
  
  protected def makeDisplay(example : Example) = html("<li>" + example.getValue.value + "</li>")
  
  def dialog(example : Option[Example], after : Example => Result) = {
    val defVal = example match {
      case Some(x) => x.getValue.value
      case None => ""
    }
    uielems.dialog("Set example",textArea("Example",defVal)) {
      s => {
        val ex = lemonFactory.makeExample(app.namer.name(sense,"example"))
        ex.setValue(new Text(s,lexicon.getLanguage))
        after(ex)
      }
    }
  }
}

//class ExampleController(val view : UIView[Example] with State, sense : LexicalSense)
//    extends HasState(view) with GenericController[Example] {
//  val title = "Examples"
//  val addText = "Add example"
//  val removeText = "Remove example"
//      
//  def unwrap(example : Example) = List(example.getValue().value)
//  
//  def wrap(values : List[String]) = values match {
//    case List(value) => {
//      val example = lemonFactory.makeExample()
//      example.setValue(new Text(value, Language.get(lexicon.getLanguage())))
//      Some(example)
//    }
//    case _ => None
//  }
//  
//  def load {
//    for(example <- sense.getExamples()) {
//      view.add(example)
//    }
//  }
//  
//  def add(example : Example) = {
//    sense addExample example
//    view add example
//  }
//  
//  def remove(example : Example) = {
//    sense removeExample example
//    view remove example
//  }
//  
//  def update(example : Example, newVal : String) {
//    example.setValue(new Text(newVal, Language.get(lexicon.getLanguage())))
//  }
//}
