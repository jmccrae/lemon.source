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
package eu.monnetproject.lemon.source.users

import eu.monnetproject.lemon.source.app._
import eu.monnetproject.lemon.source.common._

/**
 * 
 * @author John McCrae
 */
 class UserPage[C,O<:C](user : User, userDB : UserDatabase, uielems : UIElems[C,O]) {
   import uielems._
   import UIElems._
   
   val page = frame()(
     border()(
       BorderLocation.MIDDLE -> label(user.displayName,style=Some("entry_title"))
     ),
     vertical()(
       immediateField("First Name", user.firstName) {
         NoResult
       } {
         case "" => {
             user.firstName = None
             userDB.updateUser(user.id,user)
             NoResult
           }
         case s => {
             user.firstName = Some(s) 
             userDB.updateUser(user.id,user)
             NoResult
           }
       },
       immediateField("Last Name", user.lastName) {
         NoResult
       } {
         case "" => {
             user.lastName = None
             userDB.updateUser(user.id,user)
             NoResult
           }
         case s => {
             user.lastName = Some(s)
             userDB.updateUser(user.id,user)
             NoResult
           }
       }
     )
   )
 }
