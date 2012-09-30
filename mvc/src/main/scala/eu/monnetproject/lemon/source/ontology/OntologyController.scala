//package eu.monnetproject.lemon.source.common.ontology
//
//import eu.monnetproject.ontology._
//import eu.monnetproject.lemon.source.common._
//
//abstract class OntologyController(val view : UIView[Ontology] with State) 
//  extends HasState(view) with GenericController[Ontology] {
//    
//  val title = "Ontologies"
//  val addText = "Add ontology"
//  val removeText = "Remove ontology"
//    
//  def add(ontology : Ontology) = {
//    app.addOntology(ontology)
//    view add ontology
//  }
//  
//  def remove(ontology : Ontology) = {
//    throw new UnsupportedOperationException()
//  }
//  
//  def load {
//    for(ontology <- app.ontologies) {
//      view add ontology
//    }
//  }
//  
//  def unwrap(ontology : Ontology) = ontology.getURI().toString() :: Nil
//}
