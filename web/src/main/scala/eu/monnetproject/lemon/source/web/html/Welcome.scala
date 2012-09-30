/****************************************************************************
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
 ********************************************************************************/

package eu.monnetproject.lemon.source.web.html

import eu.monnetproject.lang.Language
import scala.xml._

/**
 * 
 * @author John McCrae
 */
object Welcome {
  def make(lang : Language) = lang match {
    case Language.ENGLISH => {
        <div width="100%" class="panel">
          <div class="title">Welcome to lemon source</div>
          <div class="text">
            lemon source is a space for the collaborative development of 
            ontology-lexica. Here you can upload your lexica and access them 
            from anywhere as well as sharing them with your colleagues. 
            In addition we maintain a set of publically available lexica that 
            can be edited by anyone. To view the set of public lexica click here.
          </div>
          <div class="text">
            lemon source builds on the lemon model which is a flexible model 
            for creating and sharing lexica on the semantic web and uses the 
            LexInfo model for describing data categories in the model.
          </div>
          <div class="text">
            In addition lemon source integrates a state-of-the-art automatic 
            system for generating lexica using NLP tools, that allows lemon 
            lexica to be automatically inferred from ontology files specified 
            in OWL. Note that this feature is only available to logged-in users, 
            which is performed by OpenID so you can uses existing accounts for 
            Google or Yahoo.
          </div>
          <div class="text">
            lemon source was developed as part of the Monnet project
          </div>
          <img src="images/monnet.png"/>
        </div>
      }
    case _ => { <div width="100%" class="panel">Unsupported Language</div> }
  }
}
