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
package eu.monnetproject.lemon.source.app

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._

/**
 * Generic used to describe the state of all controllers and views. The purpose of this is to avoid passing
 * lots of references around
 * @author John McCrae
 */
trait State {
  def app : LemonEditorApp 
  def model : LemonModel 
  def lemonFactory : LemonFactory 
  def lexicon : Lexicon 
  def lexiconName : String
  def lingOnto : LinguisticOntology
  def privacy : Boolean
}

/**
 * Extend this class to have states automatically copied from another state
 * @author John McCrae
 */
class HasState(state : State) extends State {
  val app : LemonEditorApp = state.app
  val model : LemonModel = state.model
  val lemonFactory : LemonFactory = state.lemonFactory
  val lexicon : Lexicon = state.lexicon
  val lexiconName : String = state.lexiconName
  val lingOnto : LinguisticOntology = state.lingOnto
  val privacy = state.privacy
}
