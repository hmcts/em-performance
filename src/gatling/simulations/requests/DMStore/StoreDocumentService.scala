package requests.DMStore

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Environment._
import utils.Headers._

object StoreDocumentService {


  /* POST /documents request to DM Store
      The design currently has a randomised weighting on the size of the file to be uploaded and this is subject to change.
      The request also allows for data preparation runs where prefix filenames can be assigned for either Delete or Get document data preparation.
      During tests, TEST prefix should be assigned.
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStorePostDocumentHeader
      The variable documentLink containing the document url link is saved on a successful POST
   */

  def DMStoreDocumentUploadSelector(TestType: String) = {

    exec(session => TestType match {
      case "DELETE_DATA_PREP" => session.set("filePrefix", "DELETE").set("fileType", "pdf")
      case "GET_DATA_PREP" => session.set("filePrefix", "GET").set("fileType", "pdf")
      case "DOC_ASSEMBLY_DATA_PREP" => session.set("filePrefix", "DOC_ASSEMBLY").set("fileType", "docx")
      case "ANNOTATION_DATA_PREP" => session.set("filePrefix", "ANNO").set("fileType", "pdf")
      case "CRON_DELETE_JOB" => session.set("filePrefix", "CRON_DELETE").set("fileType", "pdf")
      case _ => session.set("filePrefix", "TEST").set("fileType", "pdf")
    })
      .doIf(session => session("filePrefix").as[String].equals("GET")
        || session("filePrefix").as[String].equals("DELETE")
        || session("filePrefix").as[String].equals("TEST")
        || session("filePrefix").as[String].equals("ANNO")
        || session("filePrefix").as[String].equals("CRON_DELETE")) {
        randomSwitch(80d -> exec(_.set("fileSize", "2")),
          10d -> exec(_.set("fileSize", "5")),
          5d -> exec(_.set("fileSize", "10")),
          3d -> exec(_.set("fileSize", "25")),
          2d -> exec(_.set("fileSize", "50")))
          .exec(DMStoreDocumentUpload)
      }
      .doIf(session => session("filePrefix").as[String].equals("DOC_ASSEMBLY")) {
        randomSwitch(60d -> exec(_.set("fileSize", "0.5")),
          20d -> exec(_.set("fileSize", "1")),
          15d -> exec(_.set("fileSize", "5")),
          5d -> exec(_.set("fileSize", "15")))
          .exec(DMStoreDocumentUpload)
      }

  }

  val DMStoreDocumentUpload =

    group("DMStore_DocUpload") {
      exec(http("POST_Documents_#{fileSize}MB")
        .post(dmStoreURL + "/documents")
        .headers(dmStorePostDocumentHeader)
        .bodyPart(
          RawFileBodyPart("files", "data/#{fileSize}MB.#{fileType}")
            .contentType("multipart/form-data")
            .fileName("#{filePrefix}_EM_DMStore#{fileSize}MB.#{fileType}")).asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(jsonPath("$._embedded.documents[0]._links.self.href").saveAs("documentLink")))
    }


  /* GET /documents request to DM Store.  Retrieves JSON representation of a Stored Document
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreGetDocumentHeader
      #{documentId} variable is assigned from the GETDocument.csv feeder file.  The feeder is defined in the Simulation file
   */

  val DMStoreDocDownload =

    group("DMStore_DocDownload") {
      exec(http("GET_Documents_#{fileSize}")
        .get(dmStoreURL + "/documents/#{documentId}")
        .headers(dmStoreGetDocumentHeader)
        .check(jsonPath("$._links.self.href").is(dmStoreURL + "/documents/#{documentId}")))
    }

  /* GET /documents request to DM Store.  Retrieves content of most recent Document Content version
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreGetDocumentHeader
      #{documentId} variable is assigned from the GETDocumentBinary.csv feeder file.  The feeder is defined in the Simulation file
   */

  val DMStoreDocDownloadBinary =

    group("DMStore_DocDownloadBinary") {
      exec(http("GET_Documents_binary_#{fileSize}")
        .get(dmStoreURL + "/documents/#{documentId}/binary")
        .headers(dmStoreGetDocumentHeader)
        .check(bodyBytes.transform(_.size > 100).is(true)))
    }


}
