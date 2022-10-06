package requests

import io.gatling.core.Predef._
import io.gatling.http.Predef.{RawFileBodyPart, _}
import utils.Environment._
import utils.Headers._



  /* POST /documents request to DM Store
     The design currently has a randomised weighting on the size of the file to be uploaded and this is subject to change
     The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
     The S2SToken is sent within the dmStorePostDocumentHeader
     The variable documentLink containing the document url link is saved on a successful POST
  */

object DMStore {

  val DMStoreDocUpload =
    group("DMStore_DocUpload") {
      randomSwitch(35d -> exec(_.set("fileSize", "2")),
                              30d -> exec(_.set("fileSize", "5")),
                              20d -> exec(_.set("fileSize", "10")),
                              10d -> exec(_.set("fileSize", "25")),
                               5d -> exec(_.set("fileSize", "50")),
      )
      .exec(http("POST_Documents_${fileSize}MB")
        .post(dmStoreURL + "/documents")
        .headers(dmStorePostDocumentHeader)
        .bodyPart(
        RawFileBodyPart ("files", "data/${fileSize}MB.pdf")
          .contentType("application/pdf")
          .fileName("data/${fileSize}MB.pdf")).asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(jsonPath("$._embedded.documents[0]._links.self.href").saveAs("documentLink")))
  }

   /* GET /documents request to DM Store.  Retrieves JSON representation of a Stored Document
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreGetDocumentHeader
      Hardcoded for first commit but will assign feeders on later updates when data is created
   */

  val DMStoreDocDownload =

    group("DMStore_DocDownload") {
      exec(http("GET_Documents")
        .get(dmStoreURL + "/documents/9ce621dc-82f0-4ecc-b242-0e0ca9b90a59")
        .headers(dmStoreGetDocumentHeader)
        .check(jsonPath("$._links.self.href").is(dmStoreURL + "/documents/9ce621dc-82f0-4ecc-b242-0e0ca9b90a59")))
    }

   /* GET /documents request to DM Store.  Retrieves content of most recent Document Content version
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreGetDocumentHeader
      Hardcoded for first commit but will assign feeders on later updates when data is created
   */

  val DMStoreDocDownloadBinary =

    group("DMStore_DocDownloadBinary") {
      exec(http("GET_Documents_binary")
        .get(dmStoreURL + "/documents/9ce621dc-82f0-4ecc-b242-0e0ca9b90a59/binary")
        .headers(dmStoreGetDocumentHeader)
        .check(bodyBytes.transform(_.size > 100).is(true)))
    }

} // end of

