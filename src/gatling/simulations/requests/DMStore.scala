package requests

import io.gatling.core.Predef._
import io.gatling.http.Predef.{RawFileBodyPart, _}
import utils.Environment._
import utils.Headers._


object DMStore {


  /* POST /documents request to DM Store
      The design currently has a randomised weighting on the size of the file to be uploaded and this is subject to change.
      The request also allows for data preparation runs where prefix filenames can be assigned for either Delete or Get document data preparation.
      During tests, TEST prefix should be assigned.
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStorePostDocumentHeader
      The variable documentLink containing the document url link is saved on a successful POST
   */

  def DMStoreDocumentUpload(TestType: String) = {

    exec(session => TestType match {
      case "DELETE_DATA_PREP" => session.set("filePrefix", "DELETE")
      case "GET_DATA_PREP" => session.set("filePrefix", "GET")
      case _ => session.set("filePrefix", "TEST")}
    )
    .group("DMStore_DocUpload") {
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
          RawFileBodyPart("files", "data/${fileSize}MB.pdf")
            .contentType("application/pdf")
            .fileName("${filePrefix}_EM_DMStore${fileSize}MB.pdf")).asMultipartForm
          .formParam("classification", "PUBLIC")
          .check(jsonPath("$._embedded.documents[0]._links.self.href").saveAs("documentLink")))
    }
  }

   /* GET /documents request to DM Store.  Retrieves JSON representation of a Stored Document
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreGetDocumentHeader
      ${documentId} variable is assigned from the GETDocument.csv feeder file.  The feeder is defined in the Simulation file
   */

  val DMStoreDocDownload =

    group("DMStore_DocDownload") {
      exec(http("GET_Documents")
        .get(dmStoreURL + "/documents/${documentId}")
        .headers(dmStoreGetDocumentHeader)
        .check(jsonPath("$._links.self.href").is(dmStoreURL + "/documents/${documentId}")))
    }

   /* GET /documents request to DM Store.  Retrieves content of most recent Document Content version
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreGetDocumentHeader
      ${documentId} variable is assigned from the GETDocumentBinary.csv feeder file.  The feeder is defined in the Simulation file
   */

  val DMStoreDocDownloadBinary =

    group("DMStore_DocDownloadBinary") {
      exec(http("GET_Documents_binary")
        .get(dmStoreURL + "/documents/${documentId}/binary")
        .headers(dmStoreGetDocumentHeader)
        .check(bodyBytes.transform(_.size > 100).is(true)))
    }

  /* DELETE /documents request to DM Store.  Deletes the document from DM Store
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreDeleteDocumentHeader
      ${documentId} variable is assigned from the sql feeder file.  The feeder is defined in the Simulation file.
      Response code returned must be 204 for the DELETE.
   */

  val DMStoreDocDelete =

    group("DMStore_DocDelete") {
      exec(http("DELETE_Documents")
        .delete(dmStoreURL + "/documents/{documentId}")
        .headers(dmStoreDeleteDocumentHeader)
        .check(status is 204))
    }




} // end of
