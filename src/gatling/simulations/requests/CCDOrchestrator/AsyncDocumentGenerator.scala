package requests.CCDOrchestrator

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.gatling.core.Predef._
import utils.Common._
import io.gatling.http.Predef._

/* Async Document Generator for creating multiple documents for a stitching request.  The purpose of the generator is to
   build a JSON payload by appending the top of the JSON to a list of documents that are randomly picked from a feeder file
   The document list can be dynamic and is dependent on a input number that determines how many documents are to be stitched.
   The JSON has been split up into the top, example of a document list entry and the bottom of the JSON.
   The function documentListGenerator creates a list of documents and then builds the complete JSON payload
   This is a first version of this JSON builder.  There are other functions and classes that have been created but not used (using the circe libraries)
   but a working solution using these libraries has not been built yet, so using the less elegant code for now.
 */

object AsyncDocumentGenerator {

      val documentStitchFeeder = csv("feeders/STITCHCDAM_DocumentData.csv").random


      var jsonDocumentTop = """{
                              |  "case_details": {
                              |    "case_data": {
                              |      "bundleConfiguration": "prl-bundle-config.yaml",
                              |      "id": "1673557328951614",
                              |      "data": {
                              |        "applicantCaseName": "1 Test Case C100 31212",
                              |        "caseNumber": "1670864481337521",
                              |        "hearingDetails": {
                              |          "hearingVenueAddress": "STRAND, LONDON",
                              |          "hearingDateAndTime": "2023-01-13T10:00",
                              |          "hearingJudgeName": null
                              |        },
                              |        "orders": [],
                              |        "allOtherDocuments": [],
                              |        "applications": [""".stripMargin


   var jsonDocumentString = """{
                              |            "id": null,
                              |            "value": {
                              |              "documentLink": {
                              |                "document_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId",
                              |                "document_binary_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId/binary",
                              |                "document_filename": "docFilename",
                              |                "document_hash": null,
                              |                "document_creation_date": null
                              |              },
                              |              "documentFileName": "docFilename",
                              |              "documentGroup": "applicantApplication"
                              |            }
                              |          }""".stripMargin


  var jsonDocumentBottom = """]
                             |      }
                             |    },
                             |    "id": "Applicant Firstname Applicant Lastname"
                             |  },
                             |  "caseTypeId": "Benefit",
                             |  "jurisdictionId": "SSCS",
                             |  "event_id": "createBundle"
                             |}""".stripMargin

  //  /* function to create a list of documents and add it within the JSON payload for document stitching
  //   takes an argument that indicate the number of documents required to be stitched.  The full payload is then saved
  //   to session as documentJSON */
  //
    def documentListGenerator(numberOfDocuments: Int) = {
      //take the number of documents required and repeat creating a list of documents randomly from a feeder file
      var jsonDocumentBuilder = ""
      var documentJSON = ""
      var pageCount = 0
      repeat(numberOfDocuments, "docNumber") {
        feed(documentStitchFeeder)
        .exec(session => {
          var counter = session("docNumber").as[Int]
          counter += 1
          val docName = "doc" + counter
          //get the documentId and document name from the feeder file in session
          val documentId = session("documentId").as[String]
          val documentName = session("originaldocumentname").as[String]
          val documentPageCount = session("pages").as[Int]
          pageCount = pageCount + documentPageCount
          //replace some hard coded values with the document name and documentId
          documentJSON = jsonDocumentString.replace("docName", docName)
          documentJSON = documentJSON.replace("docId", documentId)
          documentJSON = documentJSON.replace("docFilename", documentName)
          //add a comma at the end of the document JSON for the next document in the list
          jsonDocumentBuilder = jsonDocumentBuilder + documentJSON + ","
          session
        })
      }
        //create the full JSON payload using the top, JSON document list and the JSON at the bottom.
      .exec(session => {
        //get a UUID for the bundle ID
        val bundleId = getUUID()
        var completeJSON = jsonDocumentTop + jsonDocumentBuilder + jsonDocumentBottom
        jsonDocumentBuilder = ""
        //replace the bundleId hard coded value with the created bundleId.  Also replace the last comma from the document list
        //completeJSON = completeJSON.replace("bundleId", bundleId)
        completeJSON = completeJSON.replace(",]", "]")
        //round the page counts down to nearest 10 to reduce unique transaction names
        val roundedPageCount = roundHundred(pageCount)
        pageCount = 0
        //store the complete JSON in a session variable
        session.setAll("documentJSON" -> completeJSON, "pageCount" -> roundedPageCount)
      })
    }

}