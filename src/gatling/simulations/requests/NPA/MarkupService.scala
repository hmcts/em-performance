package requests.NPA

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Environment._
import utils.Headers._

object MarkupService {

  /* GET request for retrieving all markups associated with a document.  The request requires both an S2S token and Idam token and this should be
      called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
      documents can be used to retrieve the markups.  Following Azure production analysis, the request will return a 404 response if no markups are found
      therefore checking for this in the response.  There are thousands of 404 responses returned in production every hour.
      If the document does have a valid markup then a 200 response is returned so this is checked also*/

  val MarkupGetMarkups =

    group("NPA_Markup") {
      exec(http("GET_Markups")
        .get(npaAPIURL + "/api/markups/#{documentId}")
        .headers(npaGetMarkupsHeader)
        .check(status in (200, 404, 204)))
    }


  /* POST request for creating markups/redaction associated with a document.  The request requires both an S2S token and Idam token and this should be
        called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
        documents can be used to retrieve the markups.  A 201 response is expected on submission of a successful markup and a redactionId value is captured
        into session for use in the DELETE request*/

  val MarkupCreateMarkups =

    group("NPA_Markup") {
      exec(session => {
        session.setAll("rectangleId" -> getUUID(), "redactionId" -> getUUID(), "pageNumber" -> getRandomNumberIntBetweenValues(1, 5),
          "rectangleX" -> getRandomNumberIntBetweenValues(1, 100), "rectangleY" -> getRandomNumberIntBetweenValues(1, 100),
          "rectangleWidth" -> getRandomNumberIntBetweenValues(1, 100), "rectangleHeight" -> getRandomNumberIntBetweenValues(1, 100))
      })
      .exec(http("POST_Markups")
        .post(npaAPIURL + "/api/markups")
        .headers(npaPostMarkupsHeader)
        .body(ElFileBody("bodies/NPA_POST_Markup.json")).asJson
        .check(status is 201)
        .check(jsonPath("$.redactionId").saveAs("redactionId")))
    }

  /* DELETE request for deleting markups/redaction associated with a document.  The request requires both an S2S token and Idam token and this should be
        called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
        documents can be used to retrieve the markups.  This request also expects a redactionId to be available in session so ideally the request design
        is based upon a markup being created and redactionId stored so that it can be immediately deleted.  The deletion request will either be all redactions
        linked to a document or just 1 redactionId associated with a document (50/50 split)*/

  val MarkupDeleteMarkup =

    group("NPA_Markup") {
      doIf("#{redactionId.exists()}") {
        randomSwitch(50d -> exec(http("DELETE_Markup")
                                          .delete(npaAPIURL + "/api/markups/#{documentId}/#{redactionId}")
                                          .headers(npaPostMarkupsHeader)
                                          .check(status is 200)),
                                50d -> exec(http("DELETE_All_Markups")
                                          .delete(npaAPIURL + "/api/markups/#{documentId}")
                                          .headers(npaPostMarkupsHeader)
                                          .check(status is 200)))
      }
    }






}
