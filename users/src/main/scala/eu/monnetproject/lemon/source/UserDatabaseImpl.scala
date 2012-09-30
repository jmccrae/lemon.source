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

import java.io._
import java.util.Random
import scala.io._
import eu.monnetproject.lemon.source.app.User
import eu.monnetproject.lemon.source.app.UserDatabase

/**
 * 
 * @author John McCrae
 */
class UserDatabaseImpl() extends UserDatabase {
  var users = Map[String,User]()
  private val userFile = new File(System.getProperty("user.home")+File.separator+".lemonsource/userdb")
  val nonce = readUserFile
    
  def readUserFile : String = {
    if(userFile.exists()) {
      System.err.println("userFile read from " + userFile.getPath)
      var in : BufferedSource = null
      try {
        in = new BufferedSource(new FileInputStream(userFile))
        val lines = in.getLines()
        val nonce = lines.next
        System.err.println("nonce: " + nonce)
        for(line <- lines) {
          val elems = line.split("\t")
          if(elems.length == 4) {
            val firstName = elems(2) match {
              case "" => None
              case s => Some(s)
            }
            val lastName = elems(3) match {
              case "" => None
              case s => Some(s)
            }
            users += elems(0) -> UserImpl(elems(0),elems(1),firstName,lastName)
          } else if(elems.length == 2) {
            users += elems(0) -> UserImpl(elems(0),elems(1),None,None)
          } else {
            System.err.println("Bad user line: " + line)
          }
        }
        nonce
      } finally {
        if(in != null)
          in.close()
      }
    } else {
      val nc = new Random().nextInt.toHexString
      val parent : File = userFile.getParentFile()
      if(!parent.exists()) {
        parent.mkdirs()
      }
      val out = new PrintWriter(userFile)
      out.println(nc)
      out.close
      System.err.println("New user db @ " + userFile.getPath + " nonce: " + nc)
      nc
    }
  }
  
  def add(user : User) {
    if(users.contains(user.id)) {
      throw new IllegalArgumentException("User already exists")
    }
    if(!userFile.exists) {
      try {
        if(!userFile.createNewFile()) {
          //log.severe("Could not create user @ "+ userFile.getPath())
        }
      } catch {
        case x : Exception => x.printStackTrace
      }
    }
    if(userFile.exists) {
      val f = new BufferedWriter(new FileWriter(userFile,true))
      f.write(user.id + "\t" + user.auth + "\t" + user.firstName.getOrElse("").replaceAll("\\s"," ") + "\t" + user.lastName.getOrElse("").replaceAll("\\s", " ")+"\n")
      f.close
    }
    users += user.id -> user
  }
  
  def get(id : String) = users.get(id)
  
  def get(id : String, auth : String, nonce : String) : Option[User] = {
    val user = users.get(id)
    user match {
      case None => System.err.println("user not loaded") ; None
      case Some(user2) => if(user2.verifyUser(auth, nonce)) {
          user
        } else {
          None
        }
    }
  }
  
  def newUser(id : String, auth : String) : Option[User] = {
    val u = UserImpl(id,auth,None,None)
    if(users.contains(id)) {
      None
    } else {
      add(u)
      Some(u)
    }
  }
  
  def updateUser(id : String, newUser : User) {
    assert(newUser.id == id)
    users += id -> newUser
    val f = new BufferedWriter(new FileWriter(userFile))
    for((id2,user) <- users) {
      f.write(user.id + "\t" + user.auth + "\t" + user.firstName.getOrElse("").replaceAll("\\s"," ") + "\t" + user.lastName.getOrElse("").replaceAll("\\s", " ")+"\n")
    }
    f.close
  }
}
