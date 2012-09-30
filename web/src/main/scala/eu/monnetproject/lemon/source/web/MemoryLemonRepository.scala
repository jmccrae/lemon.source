package eu.monnetproject.lemon.source.web

import eu.monnetproject.lemon._
import java.net.URI


class MemoryLemonRepository extends LemonRepository {
  def connect(uri : URI) = LemonSerializer.newInstance();
}
