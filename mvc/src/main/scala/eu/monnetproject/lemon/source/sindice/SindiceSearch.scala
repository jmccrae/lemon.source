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
 package eu.monnetproject.lemon.source.sindice

import eu.monnetproject.util._
import java.io._
import java.net._
import scala.xml._

object SindiceSearch {
  private val log = Logging.getLogger(this)
  
  def search(query : String, page : Int = 1) : Option[List[URI]] = {
    val queryURL = new URL("http://api.sindice.com/v2/search?q="+query+"&qt=term&formats=rdfxml&page="+page)
    try {
      val inputStream = queryURL.openStream()
      val xml = XML.load(inputStream)
      val totalResults = (xml \\ "totalResults").text.toInt
      if(totalResults < page*10) {
        None
      } else {
        Some(
          (for(result <- xml \\ "entry"
                if ((result \ "format") exists { _.text == "RDF" })
                  ) yield {
                  URI.create((result \ "id").text)
                }
            ).toList
          )
       }
    } catch {
            case x : Exception => log.warning(x.getMessage) ; None
    }
  }
}
