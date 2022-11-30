package requests.DMStore

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Environment._
import utils.Headers._

object StoreDocumentService {


  /* POST /documents request to DM Store
      The design currently has a randomised weighting on the size of the file to be uploaded and this is subject to change.
      All document uploads must have a file prefix and a file type and this is set in the function.  The file uploads are split into
      groups that describe the types of file that are being prepared depending on the test required.
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
      case "STITCHING_DATA_PREP" => session.set("filePrefix", "STITCHING")
      case _ => session.set("filePrefix", "TEST").set("fileType", "pdf")
    })
      .doIf(session => session("filePrefix").as[String].equals("GET")
        || session("filePrefix").as[String].equals("DELETE")
        || session("filePrefix").as[String].equals("TEST")
        || session("filePrefix").as[String].equals("ANNO")
        || session("filePrefix").as[String].equals("CRON_DELETE")) {
        randomSwitch(80d -> exec(_.set("fileSize", "2").set("fileSizeName", "2")),
          10d -> exec(_.set("fileSize", "5").set("fileSizeName", "5")),
          5d -> exec(_.set("fileSize", "10").set("fileSizeName", "10")),
          3d -> exec(_.set("fileSize", "25").set("fileSizeName", "25")),
          2d -> exec(_.set("fileSize", "50").set("fileSizeName", "50")))
          .exec(DMStoreDocumentUpload)
      }
      .doIf(session => session("filePrefix").as[String].equals("DOC_ASSEMBLY")) {
        randomSwitch(60d -> exec(_.set("fileSize", "0.5").set("fileSizeName", "0.5")),
          20d -> exec(_.set("fileSize", "1").set("fileSizeName", "1")),
          15d -> exec(_.set("fileSize", "5").set("fileSizeName", "5")),
          5d -> exec(_.set("fileSize", "15").set("fileSizeName", "15")))
          .exec(DMStoreDocumentUpload)
      }
      .doIf(session => session("filePrefix").as[String].equals("STITCHING")) {
        //PDF documents
        exec(_.set("fileSizeName", "_230_5").set("fileSize", "5").set("fileType", "pdf"))
        .exec(DMStoreDocumentUpload)
        .exec(_.set("fileSizeName", "_464_10").set("fileSize", "10"))
        .exec(DMStoreDocumentUpload)
        .exec(_.set("fileSizeName", "_1158_25").set("fileSize", "25"))
        .exec(DMStoreDocumentUpload)
        .exec(_.set("fileSizeName", "_2320_50").set("fileSize", "50"))
        .exec(DMStoreDocumentUpload)
        //Word Documents
        .exec(_.set("fileSizeName", "_5_1").set("fileSize", "1").set("fileType", "docx"))
        .exec(DMStoreDocumentUpload)
        .exec(_.set("fileSizeName", "_78_5").set("fileSize", "5"))
        .exec(DMStoreDocumentUpload)
        .exec(_.set("fileSizeName", "_175_15").set("fileSize", "15"))
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
            .fileName("#{filePrefix}_EM_DMStore#{fileSizeName}MB.#{fileType}")).asMultipartForm
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
