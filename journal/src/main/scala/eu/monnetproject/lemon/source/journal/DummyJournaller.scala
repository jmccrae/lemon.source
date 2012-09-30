package eu.monnetproject.lemon.source.journal

import eu.monnetproject.journalling._
import scala.collection.JavaConversions._

class DummyJournaller extends Journaller {
  override def journal(entry : JournalEntry) { }
  override def getEntries(source : String, signature : JournalSignature) = Nil
  override def getEntries(key : String, value : String, signature : JournalSignature) = Nil
}
