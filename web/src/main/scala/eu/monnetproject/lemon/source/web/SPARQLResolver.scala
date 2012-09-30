package eu.monnetproject.lemon.source.web

import java.net._
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import scala.collection.mutable.HashMap
import org.w3c.dom.Element

object SPARQLResolver {
  def sparqlProp(uri : URI, prop: URI, graphs : List[URI], endpoint : String) : List[AnyRef] = {
    val query = new StringBuilder("SELECT *");
    for (graph <- graphs) {
      query.append(" FROM <").append(graph.toString()).append(">");
    }
    query.append(" WHERE { ");
    query.append("<").append(uri.toString()).append("> ");
    query.append("<").append(prop.toString()).append("> ");
    query.append("?o ");
    query.append("}");
    val queryURL = new URL(endpoint + "?query=" + URLEncoder.encode(query.toString(), "UTF-8"));
    //System.err.println(query.toString());
    val connection = queryURL.openConnection();
    connection.setRequestProperty("Accept", "application/sparql-results+xml");
    val dbf = DocumentBuilderFactory.newInstance();
    val db = dbf.newDocumentBuilder();
    val in = connection.getInputStream();
    val document = db.parse(in);
    in.close();
    var result : List[AnyRef] = Nil
    val resultsTags = document.getElementsByTagName("result");
    for (i <- 0 until resultsTags.getLength()) {
      val node = resultsTags.item(i);
      if (node.isInstanceOf[Element]) {
        val element = node.asInstanceOf[Element];
        val resultTags = element.getElementsByTagName("binding");
        for (j <- 0 until resultTags.getLength()) {
          val resultElem = resultTags.item(j).asInstanceOf[Element];
          val varName = resultElem.getAttribute("name");
          var value : Option[AnyRef] = None
          varName match {
            case "o" => {
                value = Option(readResult(resultElem));
              }
            case _ => throw new RuntimeException("Unexpected variable " + varName)
          }
          if (value == None) {
            throw new RuntimeException("Query results lacked predicate or object");
          } else {
            result = value.get :: result
          }
        }
      }
    }
    return result
  }
  
  def sparqlAll(uri : URI, graphs : List[URI], endpoint : String) : Map[URI,List[AnyRef]] = {
    val query = new StringBuilder("SELECT *");
    for (graph <- graphs) {
      query.append(" FROM <").append(graph.toString()).append(">");
    }
    query.append(" WHERE { ");
    query.append("<").append(uri.toString()).append("> ");
    query.append(" ?p ?o ");
    query.append("}");
    val queryURL = new URL(endpoint + "?query=" + URLEncoder.encode(query.toString(), "UTF-8"));
    //System.err.println(query.toString());
    val connection = queryURL.openConnection();
    connection.setRequestProperty("Accept", "application/sparql-results+xml");
    val dbf = DocumentBuilderFactory.newInstance();
    val db = dbf.newDocumentBuilder();
    val in = connection.getInputStream();
    val document = db.parse(in);
    in.close();
    var result = new HashMap[URI,List[AnyRef]]()
    val resultsTags = document.getElementsByTagName("result");
    for (i <- 0 until resultsTags.getLength()) {
      val node = resultsTags.item(i);
      if (node.isInstanceOf[Element]) {
        val element = node.asInstanceOf[Element];
        val resultTags = element.getElementsByTagName("binding");
        for (j <- 0 until resultTags.getLength()) {
          val resultElem = resultTags.item(j).asInstanceOf[Element];
          val varName = resultElem.getAttribute("name");
          var pred : Option[URI] = None
          var value : Option[AnyRef] = None
          varName match {
            case "p" => {
                val r = readResult(resultElem);
                if (r.isInstanceOf[URI]) {
                  pred = Some(r.asInstanceOf[URI])
                } else {
                  throw new RuntimeException("SPARQL results had non-URI tag as predicate");
                }
              }
            case "o" => {
                value = Option(readResult(resultElem));
              }
            case _ => throw new RuntimeException("Unexpected variable " + varName)
          }
          if (pred == None || value == None) {
            throw new RuntimeException("Query results lacked predicate or object");
          }
          if(result.contains(pred.get)) {
            result += (pred.get -> (value.get :: result(pred.get)))
          } else {
            result += (pred.get -> (value.get :: Nil))
          }
        }
      }
    }
    return result.toMap
  }
  
  private def readResult(resultTag : Element) : AnyRef = {
    val childNodes = resultTag.getChildNodes();
    for (i <- 0 until childNodes.getLength()) {
      val child = childNodes.item(i);
      if (child.isInstanceOf[Element]) {
        val c = child.asInstanceOf[Element]
        if (c.getTagName().equals("uri")) {
          if(c.getTextContent().startsWith("_:")) {
            return c.getTextContent().substring(2);
          } else {
            return URI.create(c.getTextContent());
          }
        } else if (c.getTagName().equals("literal")) {
          if (c.getAttribute("xml:lang") != null) {
            return new StringLang(c.getTextContent(), c.getAttribute("xml:lang"));
          } else {
            return new StringLang(c.getTextContent(), null);
          }
        } else if (c.getTagName().equals("bnode")) {
          if (c.getTextContent() != null && c.getTextContent().startsWith("nodeID://")) {
            // Virtuoso does this... for various reasons it is now better to treat this like
            // it was the URI all along
            return URI.create(c.getTextContent().substring(9, c.getTextContent().length()));
          } else {
            return c.getTextContent();
          }
        } else {
          throw new RuntimeException("Unexpected tag in binding " + c);
        }
      }
    }
    throw new RuntimeException("No tag in result set");
  }
  
  case class StringLang(val value : String, val lang : String)
}
