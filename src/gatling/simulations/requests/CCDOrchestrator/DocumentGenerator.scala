package requests.CCDOrchestrator

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.gatling.core.Predef._
import utils.Common._
import io.gatling.http.Predef._

/* Document Generator for creating multiple documents for a stitching request.  The purpose of the generator is to
   build a JSON payload by appending the top of the JSON to a list of documents that are randomly picked from a feeder file
   The document list can be dynamic and is dependent on a input number that determines how many documents are to be stitched.
   The JSON has been split up into the top, example of a document list entry and the bottom of the JSON.
   The function documentListGenerator creates a list of documents and then builds the complete JSON payload
   This is a first version of this JSON builder.  There are other functions and classes that have been created but not used (using the circe libraries)
   but a working solution using these libraries has not been built yet, so using the less elegant code for now.
 */

object DocumentGenerator {

  val documentStitchFeeder = csv("feeders/STITCH_DocumentData.csv").random


  var jsonDocumentTop = """{
                          |  "case_details": {
                          |    "case_data": {
                          |      "caseBundles": [
                          |        {
                          |          "value": {
                          |            "id": "bundleId",
                          |            "title": "Bundle title",
                          |            "description": "Test bundle",
                          |            "eligibleForStitching": "yes",
                          |            "eligibleForCloning": "no",
                          |            "documents": [
                          |            """.stripMargin


  var jsonDocumentString = """{
                             |                "value": {
                             |                  "name": "docName",
                             |                  "description": "description",
                             |                  "sourceDocument": {
                             |                    "document_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId",
                             |                    "document_filename": "docFilename",
                             |                    "document_binary_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId/binary"
                             |                  }
                             |                }
                             |              }
                             |              """.stripMargin


  var jsonDocumentBottom = """],
                             |            "folders": [],
                             |            "fileName": "fileName",
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




  /* function to create a list of documents and add it within the JSON payload for document stitching
   takes an argument that indicate the number of documents required to be stitched.  The full payload is then saved
   to session as documentJSON */

  def documentListGenerator(numberOfDocuments: Int) = {
    //take the number of documents required and repeat creating a list of documents randomly from a feeder file
    var jsonDocumentBuilder = ""
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
        var documentJSON = jsonDocumentString.replace("docName", docName)
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
      //replace the bundleId hard coded value with the created bundleId.  Also replace the last comma from the document list
      completeJSON = completeJSON.replace("bundleId", bundleId)
      completeJSON = completeJSON.replace(",],", "],")
      //println("the JSON is " + completeJSON)
      //round the page counts down to nearest 10 to reduce unique transaction names
      val roundedPageCount = RoundDownTen(pageCount)
      //store the complete JSON in a session variable
      session.setAll("documentJSON" -> completeJSON, "pageCount" -> roundedPageCount)
    })
  }


  /*This code is not currently used */

  def BundleDocumentsGenerator: Json = {
    BundleDocuments(
      documents = DocumentGenerator.BundleDocumentGenerator
    ).asJson
  }


  def BundleDocumentGenerator: List[BundleDocument] = List[BundleDocument] {
    BundleDocument(
      value = DocumentItemsGenerator
    )
  }

  def DocumentItemsGenerator: DocumentItems = {
    DocumentItems(
      name = "docName",
      description = "description",
      sourceDocument = DocumentDataGenerator
    )
  }

  def DocumentDataGenerator: DocumentData = {
    DocumentData(
      document_url = "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId",
      document_filename = "docFilename",
      document_binary_url = "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId/binary"
    )
  }


}