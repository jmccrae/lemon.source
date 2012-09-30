//package eu.monnetproject.lemon.source.common.ontology
//
//import eu.monnetproject.lemon.source.common._
//import eu.monnetproject.ontology.{Class=>OClass,_}
//import java.net.URI
//import scala.collection.JavaConversions._
//
//class ClassController(val view : UIView[OClass] with State, ontology : Ontology) 
//extends HasState(view) with GenericController[OClass] {
//  
//  val title = "Classes"
//  val addText = "Add Class"
//  val removeText = "Remove Class"
//  
//  def add(clazz : OClass) = NoResult
//  
//  def remove(clazz : OClass) = NoResult
//  
//  def load {
//    for(clazz <- ontology.getClasses) {
//      view.add(clazz)
//    }
//  }
//  
//  def wrap(vals : List[String]) = vals match {
//    case List(uri) => Some(ontology.getFactory().makeClass(URI.create(uri)))
//    case _ => None
//  }
//  
//  def unwrap(clazz : OClass) = List(clazz.getURI().toString())
//}
