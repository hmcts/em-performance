package requests.NPA

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Headers._
import utils.Environment._


object RedactionsService {

  /* POST request for 'burning' a redactionId onto a document.  The request requires both an S2S token and Idam token and this should be
        called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
        documents can be used for the documentId required*/

  val MarkupBurnMarkups =

    group("NPA_Markup") {
      exec(_.set("rectangleId", getUUID()))
        .exec(_.set("redactionId", getUUID()))
        .exec(http("POST_Markups")
          .post(npaAPIURL + "/api/redaction")
          .headers(npaPostMarkupsHeader)
          .body(ElFileBody("bodies/NPA_POST_Burn.json")).asJson)
      //.check(status is 201)
      //.check(jsonPath("$.redactionId").saveAs("redactionId")))
    }

}
