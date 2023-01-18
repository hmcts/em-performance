package requests.CCDOrchestrator

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.gatling.core.Predef._
import utils.Common._
import io.gatling.http.Predef._

/* Sync Document Generator for creating multiple documents for a stitching request.  The purpose of the generator is to
   build a JSON payload by appending the top of the JSON to a list of documents that are randomly picked from a feeder file
   The document list can be dynamic and is dependent on a input number that determines how many documents are to be stitched.
   The JSON has been split up into the top, example of a document list entry and the bottom of the JSON.
   The function documentListGenerator creates a list of documents and then builds the complete JSON payload
   This is a first version of this JSON builder.  There are other functions and classes that have been created but not used (using the circe libraries)
   but a working solution using these libraries has not been built yet, so using the less elegant code for now.
 */

object SyncDocumentGenerator {

    val documentStitchFeeder = csv("feeders/STITCHCDAM_SSCS_DocumentData.csv").random

    var jsonDocumentTop = """{
                            |  "caseTypeId": "Benefit",
                            |  "jurisdictionId": "SSCS",
                            |  "case_details": {
                            |    "case_data": {
                            |      "caseBundles": [
                            |        {
                            |          "value": {
                            |            "id": "94b12818-fea9-4f1c-9268-79066dd95e50",
                            |            "title": "Bundle title",
                            |            "description": "Test bundle",
                            |            "eligibleForStitching": "yes",
                            |            "eligibleForCloning": "no",
                            |            "stitchedDocument": null,
                            |            "documents": [""".stripMargin



  var jsonDocumentString = """{
                             |    "value": {
                             |      "name": "docFilename",
                             |      "description": null,
                             |      "sortIndex": index,
                             |      "sourceDocument": {
                             |      "document_url": "http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                             |      "document_filename": "docFilename",
                             |      "document_binary_url": "http://dm-store-aat.service.core-compute-aat.internal/documents/docId/binary",
                             |      "document_hash": "ebad12b8f9b8f7c1c779dc52441b86a891f6c10ace9995eefe4508b08b8e8abe"
                             |    }
                             |    }
                             |  }""".stripMargin


  var jsonDocumentBottom =     """],
                                 |            "folders": [],
                                 |            "fileName": "fileName",
                                 |            "fileNameIdentifier": null,
                                 |            "coverpageTemplate": null,
                                 |            "hasTableOfContents": "Yes",
                                 |            "hasCoversheets": "Yes",
                                 |            "hasFolderCoversheets": null,
                                 |            "stitchStatus": "",
                                 |            "paginationStyle": "off",
                                 |            "pageNumberFormat": "numberOfPages",
                                 |            "stitchingFailureMessage": null
                                 |          }
                                 |        }
                                 |      ]
                                 |    }
                                 |  }
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
          val counter = session("docNumber").as[String]
          //counter += 1
          //val docName = "doc" + counter
          //get the documentId and document name from the feeder file in session
          val documentId = session("documentId").as[String]
          val documentName = session("originaldocumentname").as[String]
          val documentPageCount = session("pages").as[Int]
          pageCount = pageCount + documentPageCount
          //replace some hard coded values with the document name and documentId
          //documentJSON = jsonDocumentString.replace("docName", docName)
          documentJSON = jsonDocumentString.replace("docId", documentId)
          documentJSON = documentJSON.replace("docFilename", documentName)
          documentJSON = documentJSON.replace("index", counter)
          //add a comma at the end of the document JSON for the next document in the list
          jsonDocumentBuilder = jsonDocumentBuilder + documentJSON + ","
          session
        })
      }
  //      //create the full JSON payload using the top, JSON document list and the JSON at the bottom.
      .exec(session => {
        //get a UUID for the bundle ID
        val bundleId = getUUID()
        var completeJSON = jsonDocumentTop + jsonDocumentBuilder + jsonDocumentBottom
        jsonDocumentBuilder = ""
        val dateToday = currentDateTime("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        //replace the bundleId hard coded value with the created bundleId.  Also replace the last comma from the document list
        completeJSON = completeJSON.replace("bundleId", bundleId)
        completeJSON = completeJSON.replace(",],", "],")
        completeJSON = completeJSON.replace("dateToday", dateToday)
        //round the page counts down to nearest 10 to reduce unique transaction names
        val roundedPageCount = roundHundred(pageCount)
        pageCount = 0
        //store the complete JSON in a session variable
        session.setAll("documentJSON" -> completeJSON, "pageCount" -> roundedPageCount)
      })
    }


}