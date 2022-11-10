package requests.DocAssembly

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment._
import utils.Headers._

object TemplateRendition {

  /*  Need to add commentary here once working.
   */



  val DocAssemblyRenderTemplate =

    group("DocAssembly_RenderTemplate") {
      exec(http("POST_DocAssemblyRenderTemplate")
        .post(docAssemblyURL + "/api/template-renditions")
        .headers(docAssemblyConvertHeader))
    }

}
