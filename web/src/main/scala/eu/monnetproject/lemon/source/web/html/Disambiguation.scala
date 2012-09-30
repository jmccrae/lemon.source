package eu.monnetproject.lemon.source.web.html

import java.net.URI

object Disambiguation {

  def page(results : List[AnyRef], l10n : String => String) = {
    <div class="panel">
      <div class="title">{l10n("Disambiguation")}</div><ul>{
        for(result <- results if result.isInstanceOf[URI]) yield {
          <li><a href={result.toString} class="entry_link">{result.toString()}</a></li>
        }
      }</ul>
    </div>
  }
}
