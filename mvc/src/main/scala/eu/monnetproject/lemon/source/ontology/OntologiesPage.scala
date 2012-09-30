package eu.monnetproject.lemon.source.ontology

import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.lemon.source.app.LemonEditorApp
import eu.monnetproject.lemon.source.common._

class OntologiesPage[C,O<:C](val app : LemonEditorApp, model : LemonModel, uielems : UIElems[C,O]) {
  import uielems._
  import UIElems._
  
  val state = app.state(None,model)
  
  val container = frame()(
    border()(
      BorderLocation.MIDDLE -> label("My Ontologies",style=Some("entry_title"))
    ),
    views(
      null//new OntologyView(state,uielems)
    )
  )
}
