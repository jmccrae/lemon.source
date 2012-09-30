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
package eu.monnetproject.lemon.source.common

//import eu.monnetproject.util.Logging
//import info.aduna.iteration.Iteration
//import java.io._
//import java.net.URL
//import org.openrdf.model._
//import org.openrdf.query._
//import org.openrdf.repository._
//import org.openrdf.rio._

/**
 * @author John McCrae
 */
class TimingRepository { }
//class TimingRepository(_repo : Repository) extends Repository {
//
//  val log = Logging.getLogger(this)
//  
//  class TimingConnection(_conn : RepositoryConnection) extends RepositoryConnection {
//    
//    private def time[E](name : String)(func : => E) : E = {
//      val l = System.currentTimeMillis
//      val e = func
//      log.info("Time (" + name + "): " + (System.currentTimeMillis - l))
//      e
//    }
//    
//    def getRepository() = _conn.getRepository()
//
//    def getValueFactory() = _conn.getValueFactory()
//
//    def isOpen() = _conn.isOpen()
//
//    def close() = _conn.close()
//
//    def prepareQuery(ql : QueryLanguage, string : String) = _conn.prepareQuery(ql,string)
//
//    def prepareQuery(ql : QueryLanguage, string : String, string1 : String) = _conn.prepareQuery(ql,string,string1)
//
//    def prepareTupleQuery(ql : QueryLanguage, string : String) = time("prepareTupleQuery2") { _conn.prepareTupleQuery(ql,string) }
//
//    def prepareTupleQuery(ql : QueryLanguage, string : String, string1 : String) = time("prepareTupleQuery3") { _conn.prepareTupleQuery(ql,string,string1) }
//
//    def prepareGraphQuery(ql : QueryLanguage, string : String) = _conn.prepareGraphQuery(ql,string)
//
//    def prepareGraphQuery(ql : QueryLanguage, string : String, string1 : String) = _conn.prepareGraphQuery(ql,string,string1)
//
//    def prepareBooleanQuery(ql : QueryLanguage, string : String) = _conn.prepareBooleanQuery(ql,string)
//
//    def prepareBooleanQuery(ql : QueryLanguage, string : String, string1 : String) = _conn.prepareBooleanQuery(ql,string,string1)
//
//    def getContextIDs() = _conn.getContextIDs()
//
//    def getStatements(rsrc : Resource, uri : URI, value : Value, bln : Boolean, rsrcs : Resource*)  = time("getStatements") { _conn.getStatements(rsrc,uri,value,bln,rsrcs:_*) }
//
//    def hasStatement(rsrc : Resource, uri : URI, value : Value, bln : Boolean, rsrcs : Resource*) = time("hasStatement4") { _conn.hasStatement(rsrc,uri,value,bln,rsrcs:_*) }
//
//    def hasStatement(stmnt : Statement, bln : Boolean, rsrcs : Resource*) = time("hasStatement3") { _conn.hasStatement(stmnt,bln,rsrcs:_*) }
//
//    def exportStatements(rsrc : Resource, uri : URI, value : Value, bln : Boolean, rdfh : RDFHandler, rsrcs : Resource*)  = _conn.exportStatements(rsrc,uri,value,bln,rdfh,rsrcs:_*)
//
//    def export(rdfh : RDFHandler, rsrcs : Resource*) = _conn.export(rdfh,rsrcs:_*)
//    
//    def size(rsrcs : Resource*) = _conn.size(rsrcs:_*)
//
//    def isEmpty() = _conn.isEmpty()
//
//    def setAutoCommit(bln : Boolean) = _conn.setAutoCommit(bln)
//
//    def isAutoCommit() = _conn.isAutoCommit()
//
//    def commit() = _conn.commit()
//
//    def rollback() = _conn.rollback()
//
//    def add(in : InputStream, string : String, rdff : RDFFormat, rsrcs : Resource*) = _conn.add(in,string,rdff,rsrcs:_*)
//
//    def add(reader : Reader, string : String, rdff : RDFFormat, rsrcs : Resource*) = _conn.add(reader,string,rdff,rsrcs:_*)
//
//    def add(url : URL, string : String, rdff : RDFFormat, rsrcs : Resource*) = _conn.add(url,string,rdff,rsrcs:_*)
//
//    def add(file : File, string : String, rdff : RDFFormat, rsrcs : Resource*) = _conn.add(file,string,rdff,rsrcs:_*)
//
//    def add(rsrc : Resource, uri : URI, value : Value, rsrcs : Resource*) = _conn.add(rsrc,uri,value,rsrcs:_*)
//
//    def add(stmnt : Statement, rsrcs : Resource*) = _conn.add(stmnt,rsrcs:_*)
//
//    def add(itrbl : java.lang.Iterable[_ <: Statement], rsrcs : Resource*) = _conn.add(itrbl,rsrcs:_*)
//
//    def add[E <: Exception](itrtn : Iteration[_ <: Statement,E], rsrcs : Resource*) = _conn.add(itrtn,rsrcs:_*)
//
//    def remove(rsrc : Resource, uri : URI, value : Value, rsrcs : Resource*) = _conn.remove(rsrc,uri,value,rsrcs:_*)
//
//    def remove(stmnt : Statement, rsrcs : Resource*) = _conn.remove(stmnt,rsrcs:_*)
//
//    def remove(itrbl : java.lang.Iterable[_ <: Statement], rsrcs : Resource*) = _conn.remove(itrbl,rsrcs:_*)
//
//    def remove[E <: Exception](itrtn : Iteration[_ <: Statement,E], rsrcs : Resource*) = _conn.remove(itrtn,rsrcs:_*)
//
//    def clear(rsrcs : Resource*) = _conn.clear(rsrcs:_*)
//
//    def getNamespaces() = _conn.getNamespaces()
//
//    def getNamespace(string : String) = _conn.getNamespace(string)
//
//    def setNamespace(string : String, string1 : String) = _conn.setNamespace(string,string1)
//
//    def removeNamespace(string : String) = _conn.removeNamespace(string)
//
//    def clearNamespaces() = _conn.clearNamespaces()
//  }
//  
//  def getConnection() = new TimingConnection(_repo.getConnection())
//  def getDataDir() = _repo.getDataDir()
//  def getValueFactory() = _repo.getValueFactory()
//  def initialize() = _repo.initialize()
//  def isWritable() = _repo.isWritable()
//  def setDataDir(dataDir : File) = _repo.setDataDir(dataDir)
//  def shutDown() = _repo.shutDown()
//}
