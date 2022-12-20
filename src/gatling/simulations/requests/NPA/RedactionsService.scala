package requests.NPA

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Headers._
import utils.Environment._


object RedactionsService {

  /* POST request for 'burning' a redactionId onto a document.  The request requires both an S2S token and Idam token and this should be
        called prior to the request being made.  A markup also needs to exist within the document so this needs to be created in advance
        of calling this request i.e. cannot burn a markup if no markup exists.    A markup can be created using the MarkupService
        The request will 'burn' markups into a new document for the end user and present the document back to the user.
        This will not save a new document to DMStore.
        A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
        documents can be used for the documentId required*/

  val MarkupBurnMarkups =

    group("NPA_Markup") {
        exec(http("POST_Redaction")
          .post(npaAPIURL + "/api/redaction")
          .headers(npaPostMarkupsHeader)
          .body(ElFileBody("bodies/NPA_POST_Burn.json")).asJson)
    }

}
