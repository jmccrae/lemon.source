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
package eu.monnetproject.lemon.source.common;

import eu.monnetproject.lemon.source.app.Result
import eu.monnetproject.lemon.source.app.NoResult
import eu.monnetproject.lemon.source.app.LemonEditorApp

/**
 * A controller attached to a particular view
 * @author John McCrae
 */
trait UIController[Elem] {
  /** Initialize this element. Must be called */
  def initialize : Unit
//  /** Reload this element */
//  def reload : Unit
  /** Set the strings that should be shown for the given element */
  def unwrap(elem : Elem) : List[String]
  /** Create an element from strings given by the user */
  def wrap(vals : List[String]) : Option[Elem]
  /** The title of the element */
  def title : String
}

/**
 * The generic controller that most controllers extend
 * @author John McCrae
 */
trait GenericController[Elem] extends UIController[Elem] {
  /** The view that this controller controls (normally override with a constructor val) */
  def view : UIView[Elem]
  /** The application reference (normally override with a constructor val) */
  def app : LemonEditorApp
  /** The text to be displayed as a tooltip on the add button */
  def addText : String
  /** The text to be displayed as a tooltip on the remove button(s) */
  def removeText : String
  /** Add an element, the controller should return view.add(elem) */
  def add(elem : Elem) : Result
  /** Remove an element, the controller should return view.remove(elem) */
  def remove(elem : Elem) : Result
  /** Load all elements to be displayed in the current view */
  def load : Unit
  /** The selected item */
  var selected : Option[Elem] = None
  /** The action when an element is to be added (create a new element dialog) */
  def addAction : Option[(String,()=>Result)] = Some(
    (addText, {
        () => {
          view.dialog(None,{ (elem : Elem) => add(elem) >> doSelect(elem) })
        }
      }
    )
  )
  /** The action to be performed when the delete button is pressed */
  def removeAction : Option[(String,()=>Result)] = Some((app.l10n(removeText),  {
        () => selected match {
          case Some(elem) => remove(elem) ; view.remove(elem)
          case None => NoResult
        }
      }))
    
  /** The action to be performed when a view element is selected */
  def selectAction : Elem => Result = doSelect
  /** Load the controller */
  def initialize {
    load
  }
	
  protected def doSelect(elem : Elem) : Result = {
    selected match {
      case Some(s) if s != elem => {
          view deselect s
        }
      case _ =>
    }
    
    selected = Some(elem)
    view select elem
  }
}


trait UIController2[Elem,Component,Container<:Component] {
  def model : StdModel[Elem]
  def view : UIPanel2[Elem,Component,Container]
  
  protected var selected : Option[Elem]
  
  def add() : Result = {
    view.dialog(None, { elem => 
        model.add(elem)
        view.add(elem,panel,() => remove(elem))
      })
  }
  
  def remove(elem : Elem) : Result = {
    model.remove(elem)
    view.remove(elem,panel)
  }
  
  def update(elem : Elem) : Result = {
    view.dialog(Some(elem), { elem2 => 
        model.update(elem,elem2)
        view.update(elem,elem2,panel,() => remove(elem2))
      })
  }
  
  def select(elem : Elem) : Result = {
    val result = selected match {
      case Some(elem) => view.deselect(elem)
      case None => NoResult
    }
    selected = Some(elem)
    result >> view.select(elem)
  }
  
  def unselect() : Result = {
    val result = selected match {
      case Some(elem) => view.deselect(elem)
      case None => NoResult
    }
    selected = None
    result
  }
  
  lazy val panel = view.panel(model.elements, add, remove)
  lazy val display = view.display(model.elements)
}