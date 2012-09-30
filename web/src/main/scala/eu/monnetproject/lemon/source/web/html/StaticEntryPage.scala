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
package eu.monnetproject.lemon.source.web.html

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import java.net.URLDecoder
import java.net.URI
import scala.collection.JavaConversions._

object StaticEntryPage {

  def title(uri : URI) = if(uri.getFragment() != null) {
    URLDecoder.decode(uri.getFragment(),"UTF-8")
  } else if(uri.toString().lastIndexOf("/") != -1) {
    URLDecoder.decode(uri.toString().substring(uri.toString().lastIndexOf("/")),"UTF-8")
  } else {
    uri.toString
  }
  
  def title(entry : LexicalEntry) : String = if(entry.getCanonicalForm() != null && entry.getCanonicalForm().getWrittenRep() != null) {
    entry.getCanonicalForm().getWrittenRep().value
  } else if(entry.getURI().getFragment() != null) {
    URLDecoder.decode(entry.getURI().getFragment(),"UTF-8")
  } else if(entry.getURI().toString().lastIndexOf("/") != -1) {
    URLDecoder.decode(entry.getURI().toString().substring(entry.getURI().toString().lastIndexOf("/")),"UTF-8")
  } else {
    entry.getURI().toString
  }
  
  def propsString(element : LemonElement) : String = {
    if(element.getPropertys().isEmpty()) {
      ""
    } else {
      "(" +
      (for((prop,propVals) <- element.getPropertys ; propVal <- propVals) yield {
          title(prop.getURI) + "=" + title(propVal.getURI)
        }).mkString(", ") + ")"
    }
  }

  def makePrefString(sense : LexicalSense) : String = sense.getRefPref() match {
    case null => ""
    case LexicalSense.ReferencePreference.prefRef => " (Preferred)"
    case LexicalSense.ReferencePreference.altRef => " (Alternative)"
    case LexicalSense.ReferencePreference.hiddenRef => " (Hidden)"
  }
   
  
  def memberOfLexica(entry : LexicalEntry, model : LemonModel) = for(lexicon <- LemonModels.getLexicaByEntry(model,entry)) yield {
    lexicon.getURI()
  }
  
  def makePage(entry : LexicalEntry, l10n : String => String, model : LemonModel) = <span>
    <div class="tabbedFrame ui-tabs ui-widget ui-widget-content ui-corner-all">
      <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
        <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a href="?action=Entry">{l10n("Entry")}</a></li>
        <li class="ui-state-default ui-corner-top"><a href="?action=Edit">{l10n("Edit")}</a></li>
        <li class="ui-state-default ui-corner-top"><a href="?action=Talk">{l10n("Talk")}</a></li>
        <li class="ui-state-default ui-corner-top"><a href="?action=Changes">{l10n("Changes")}</a></li>
      </ul>
      <div class="ui-tabs-panel ui-widget-content ui-corner-bottom">
        <div class="border_right">
          <div xmlns="http://www.w3.org/1999/xhtml" class="horizontal spacing" id="n7827826560963630501">
            <a href={entry.getURI +".rdf"}><img class="imageButton" src="/lemonsource/images/rdf_flyer.png" /></a>
            <a href={entry.getURI + ".ttl"}><img class="imageButton" src="/lemonsource/images/icon_turtle.gif" /></a>
          </div>
        </div>
        <div class="vertical">
          <span class="label entry_title">{title(entry)}</span>
        </div>
        <div class="vertical">
          <span class="label sub_title">{l10n("Member of Lexica")}</span>
          {
            for(lexica <- memberOfLexica(entry,model)) yield {
              <span class="lexica_link"><br/><a href={lexica.toString}>{title(lexica)}</a></span>
            }
        
        
          }
        </div>
        <div class="vertical">
          <span class="label sub_title">{l10n("Properties")}</span>
          <table class="properties_table">
            {
              for((prop,propVals) <- entry.getPropertys() ; propVal <- propVals) yield {
                <tr>
                  <td>
                    <a href={prop.getURI().toString}>{title(prop.getURI())}</a>
                  </td>
                  <td>
                    <a href={propVal.getURI().toString}>{title(propVal.getURI())}</a>
                  </td>
                </tr>
              }
            }
          </table>
        </div>
        <div class="vertical">
          <span class="label sub_title">{l10n("Forms")}</span>
          <ul>
            {
              if(entry.getCanonicalForm() != null && entry.getCanonicalForm().getWrittenRep() != null) {
                <li><i>{"("+l10n("Canonical")+") "}</i>{entry.getCanonicalForm().getWrittenRep().value + propsString(entry.getCanonicalForm())}</li>
              } else {
                <li class="report_failed">{l10n("There was no canonical form")}</li>
              }
            }
            {
              for(form <- entry.getOtherForms() if form.getWrittenRep() != null) yield {
                <li>{entry.getCanonicalForm().getWrittenRep().value + propsString(form)}</li>
              }
            }
            {
              for(form <- entry.getAbstractForms() if form.getWrittenRep() != null) yield {
                <li><i>{"("+l10n("Abstract")+") "}</i>{form.getWrittenRep().value + propsString(form)}</li>
              }
            }
          </ul>
        </div>
        <div class="vertical">
          <span class="label sub_title">{l10n("Senses")}</span>
          <ol>
            {
              for(sense <- entry.getSenses if !sense.getDefinitions().isEmpty || sense.getReference != null) yield {
                <li> {
                    {
                      if(!sense.getDefinitions().isEmpty) {
                        <span class="label sub_sub_title">{l10n("Definitions:")}</span>
                        <ul>{
                            for((defnProperty, defns) <- sense.getDefinitions ; defn <- defns) yield {
                              <li><i>{
                                    if(defnProperty == Definition.definition) { 
                                      <span></span> 
                                    } else { 
                                      <a href={defnProperty.getURI().toString()}>{title(defnProperty.getURI())}</a> }
                                  }</i>{defn.getValue().value}</li>
                            }
                          }</ul>
                      } else { 
                        <span/> 
                      }
                    } ++
                    {if(sense.getReference != null) {
                        <div>{l10n("Ontological description: ")}<a href={sense.getReference().toString()}>{sense.getReference().toString() + makePrefString(sense)}</a></div>
                      } else {
                        <span/>
                      }
                    } ++
                    {if(!sense.getConditions().isEmpty()) {
                        <span class="label sub_sub_title">{l10n("Conditions:")}</span>
                        <ul>{
                            for((condProp,conditions) <- sense.getConditions ; condition <- conditions) yield {
                              <li><i>{
                                    if(condProp == Condition.condition) {
                                      <span/>
                                    } else {
                                      <a href={condProp.getURI().toString()}>{title(condProp.getURI())}</a>
                                    }
                                  }</i><a href={condition.getURI().toString()}>{title(condition.getURI())}</a></li>
                            }
                          }</ul>
                      } else {
                        <span/>
                      }
                    } :+
                    {
                      if(!sense.getContexts().isEmpty()) {
                        <span class="label sub_sub_title">{l10n("Contexts:")}</span>
                        <ul>{
                            for(context <- sense.getContexts()) yield {
                              <li>{context.getValue().value}</li>
                            }
                          }</ul>
                      }
                    } :+
                    {
                      if(!sense.getExamples().isEmpty()) {
                        <span class="label sub_sub_title">{l10n("Examples:")}</span>
                        <ul>{
                            for(example <- sense.getExamples()) yield {
                              <li>{example.getValue().value}</li>
                            }
                          }</ul>
                      }
                    } :+
                    {
                      for((senseRel, senses) <- sense.getSenseRelations()) yield {
                        <span class="label sub_sub_title">{title(senseRel.getURI())}</span>
                        <ul>{
                            for(trgSense <- senses) yield {
                              <li><a href={trgSense.getURI().toString()}>{title(trgSense.getURI())}</a></li>
                            }
                          }</ul>
                      }
                    }
                  }</li>
              }
            }
          </ol>
          {
            for((lexRel,trgEntrys) <- entry.getLexicalVariants()) yield {
              <span class="label sub_title">{title(lexRel.getURI())}</span>
              <ul>{
                  for(trgEntry <- trgEntrys) yield {
                    <li><a href={trgEntry.getURI().toString()}>{title(trgEntry.getURI())}</a></li>
                  }
                }</ul>
            }
          }
        </div></div></div></span>
}
