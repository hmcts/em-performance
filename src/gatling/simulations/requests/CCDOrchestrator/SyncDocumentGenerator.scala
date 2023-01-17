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
                            |  "case_details": {
                            |    "id": 1664960528242233,
                            |    "jurisdictionId": "SSCS",
                            |    "caseTypeId": "Benefit",
                            |    "state": null,
                            |    "created_date": null,
                            |    "last_modified": null,
                            |    "locked_by_user_id": null,
                            |    "security_level": null,
                            |    "case_data": {
                            |      "addLegalRepDeadline": "2023-10-19T16:00:00",
                            |      "caseDocuments": [""".stripMargin


    var jsonDocumentString = """{
                               |          "value": {
                               |            "document_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId",
                               |            "document_binary_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/docId/binary",
                               |            "document_filename": "docFilename",
                               |            "document_hash": ""
                               |          },
                               |          "name": "docFilename"
                               |        }""".stripMargin

    var jsonDocumentBottom = """],
                               |      "caseDocument1Name": "Sealed Claim Form with LiP Claim Form",
                               |      "allPartyNames": "Example applicant1 company V Example respondent1 company",
                               |      "caseListDisplayDefendantSolicitorReferences": "Respondent Reference",
                               |      "unassignedCaseListDisplayOrganisationReferences": "Claimant policy reference",
                               |      "solicitorReferences": {
                               |        "applicantSolicitor1Reference": "Applicant Reference",
                               |        "respondentSolicitor1Reference": "Respondent Reference"
                               |      },
                               |      "respondentSolicitor2Reference": "some reference",
                               |      "courtLocation": {
                               |        "applicantPreferredCourt": "344"
                               |      },
                               |      "applicant1": {
                               |        "type": "COMPANY",
                               |        "companyName": "Example applicant1 company",
                               |        "primaryAddress": {
                               |          "AddressLine1": "Flat 2",
                               |          "AddressLine2": "Caversham House 15-17",
                               |          "AddressLine3": "Church Road",
                               |          "PostTown": "Reading",
                               |          "County": "Kent",
                               |          "Country": "United Kingdom",
                               |          "PostCode": "RG4 7AA"
                               |        },
                               |        "partyName": "Example applicant1 company",
                               |        "partyTypeDisplayValue": "Company"
                               |      },
                               |      "applicantSolicitor1UserDetails": {
                               |        "email": "hmcts.civil+organisation.1.solicitor.1@gmail.com",
                               |        "id": "9c5e5972-618f-47c2-9c88-88db317051a1"
                               |      },
                               |      "addApplicant2": "No",
                               |      "addRespondent2": "Yes",
                               |      "respondent1": {
                               |        "type": "COMPANY",
                               |        "companyName": "Example respondent1 company",
                               |        "primaryAddress": {
                               |          "AddressLine1": "Flat 2",
                               |          "AddressLine2": "Caversham House 15-17",
                               |          "AddressLine3": "Church Road",
                               |          "PostTown": "Reading",
                               |          "County": "Kent",
                               |          "Country": "United Kingdom",
                               |          "PostCode": "RG4 7AA"
                               |        },
                               |        "partyName": "Example respondent1 company",
                               |        "partyTypeDisplayValue": "Company"
                               |      },
                               |      "respondent2": {
                               |        "type": "COMPANY",
                               |        "companyName": "Example respondent2 company",
                               |        "primaryAddress": {
                               |          "AddressLine1": "Flat 2",
                               |          "AddressLine2": "Caversham House 15-17",
                               |          "AddressLine3": "Church Road",
                               |          "PostTown": "Reading",
                               |          "County": "Kent",
                               |          "Country": "United Kingdom",
                               |          "PostCode": "RG4 7AA"
                               |        },
                               |        "partyName": "Example respondent2 company",
                               |        "partyTypeDisplayValue": "Company"
                               |      },
                               |      "respondent1DetailsForClaimDetailsTab": {
                               |        "type": "COMPANY",
                               |        "companyName": "Example respondent1 company",
                               |        "primaryAddress": {
                               |          "AddressLine1": "Flat 2",
                               |          "AddressLine2": "Caversham House 15-17",
                               |          "AddressLine3": "Church Road",
                               |          "PostTown": "Reading",
                               |          "County": "Kent",
                               |          "Country": "United Kingdom",
                               |          "PostCode": "RG4 7AA"
                               |        },
                               |        "partyName": "Example respondent1 company",
                               |        "partyTypeDisplayValue": "Company"
                               |      },
                               |      "respondent2DetailsForClaimDetailsTab": {
                               |        "type": "COMPANY",
                               |        "companyName": "Example respondent2 company",
                               |        "primaryAddress": {
                               |          "AddressLine1": "Flat 2",
                               |          "AddressLine2": "Caversham House 15-17",
                               |          "AddressLine3": "Church Road",
                               |          "PostTown": "Reading",
                               |          "County": "Kent",
                               |          "Country": "United Kingdom",
                               |          "PostCode": "RG4 7AA"
                               |        },
                               |        "partyName": "Example respondent2 company",
                               |        "partyTypeDisplayValue": "Company"
                               |      },
                               |      "respondent1Represented": "No",
                               |      "respondent2Represented": "Yes",
                               |      "respondent2OrgRegistered": "Yes",
                               |      "respondentSolicitor2EmailAddress": "civilunspecified@gmail.com",
                               |      "uploadParticularsOfClaim": "Yes",
                               |      "detailsOfClaim": "Details of the claim text",
                               |      "claimValue": {
                               |        "statementOfValueInPennies": "3000000"
                               |      },
                               |      "claimFee": {
                               |        "calculatedAmountInPence": "150000",
                               |        "code": "FEE0209",
                               |        "version": "3"
                               |      },
                               |      "applicantSolicitor1PbaAccounts": {
                               |        "value": {
                               |          "code": "777e5b4c-4de6-49ad-aa52-24d42cb02912",
                               |          "label": "PBA0088192"
                               |        },
                               |        "list_items": [
                               |          {
                               |            "code": "777e5b4c-4de6-49ad-aa52-24d42cb02912",
                               |            "label": "PBA0088192"
                               |          },
                               |          {
                               |            "code": "319cda51-5458-4b7a-a835-ae9181cf2406",
                               |            "label": "PBA0078095"
                               |          }
                               |        ]
                               |      },
                               |      "claimType": "PERSONAL_INJURY",
                               |      "personalInjuryType": "ROAD_ACCIDENT",
                               |      "applicantSolicitor1ClaimStatementOfTruth": {
                               |        "name": "John Smith",
                               |        "role": "Solicitor"
                               |      },
                               |      "legacyCaseReference": "000DC006",
                               |      "allocatedTrack": "MULTI_CLAIM",
                               |      "claimIssuedPaymentDetails": {
                               |        "status": "SUCCESS",
                               |        "reference": "RC-1664-9605-3507-4587",
                               |        "customerReference": "abcdefg"
                               |      },
                               |      "applicant1OrganisationPolicy": {
                               |        "Organisation": {
                               |          "OrganisationID": "Q1KOKP2"
                               |        },
                               |        "OrgPolicyReference": "Claimant policy reference",
                               |        "OrgPolicyCaseAssignedRole": "[APPLICANTSOLICITORONE]"
                               |      },
                               |      "respondent1OrganisationPolicy": {
                               |        "OrgPolicyCaseAssignedRole": "[RESPONDENTSOLICITORONE]"
                               |      },
                               |      "respondent2OrganisationPolicy": {
                               |        "Organisation": {
                               |          "OrganisationID": "79ZRSOU"
                               |        },
                               |        "OrgPolicyReference": "Defendant policy reference",
                               |        "OrgPolicyCaseAssignedRole": "[RESPONDENTSOLICITORTWO]"
                               |      },
                               |      "applicantSolicitor1ServiceAddressRequired": "Yes",
                               |      "applicantSolicitor1ServiceAddress": {
                               |        "AddressLine1": "Flat 2",
                               |        "AddressLine2": "Caversham House 15-17",
                               |        "AddressLine3": "Church Road",
                               |        "PostTown": "Reading",
                               |        "County": "Kent",
                               |        "Country": "United Kingdom",
                               |        "PostCode": "RG4 7AA"
                               |      },
                               |      "respondentSolicitor2ServiceAddressRequired": "Yes",
                               |      "respondentSolicitor2ServiceAddress": {
                               |        "AddressLine1": "Flat 2",
                               |        "AddressLine2": "Caversham House 15-17",
                               |        "AddressLine3": "Church Road",
                               |        "PostTown": "Reading",
                               |        "County": "Kent",
                               |        "Country": "United Kingdom",
                               |        "PostCode": "RG4 7AA"
                               |      },
                               |      "servedDocumentFiles": {
                               |        "scheduleOfLoss": [
                               |          {
                               |            "id": "c2b02b20-a2b1-4726-b25b-25f95731369c",
                               |            "value": {
                               |              "document": {
                               |                "document_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/1ca6ef5e-1b0e-403f-91e7-80dcdf5113da",
                               |                "document_binary_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/1ca6ef5e-1b0e-403f-91e7-80dcdf5113da/binary",
                               |                "document_filename": "examplePDF.pdf"
                               |              }
                               |            }
                               |          }
                               |        ],
                               |        "particularsOfClaimDocument": [
                               |          {
                               |            "id": "083e89af-2799-4797-b5a6-ac331bc42e2c",
                               |            "value": {
                               |              "document_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/0897530d-e0cd-4045-a11e-e552278710df",
                               |              "document_binary_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/0897530d-e0cd-4045-a11e-e552278710df/binary",
                               |              "document_filename": "examplePDF.pdf"
                               |            }
                               |          }
                               |        ],
                               |        "certificateOfSuitability": [
                               |          {
                               |            "id": "2f4b87ff-4eeb-4931-a46e-c24b9e39d13d",
                               |            "value": {
                               |              "document": {
                               |                "document_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/5d86153c-7b64-4c2d-98dc-56237bd4a88d",
                               |                "document_binary_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/5d86153c-7b64-4c2d-98dc-56237bd4a88d/binary",
                               |                "document_filename": "examplePDF.pdf"
                               |              }
                               |            }
                               |          }
                               |        ]
                               |      },
                               |      "businessProcess": {
                               |        "processInstanceId": "64a7011b-448c-11ed-8f41-aedf32043728",
                               |        "status": "STARTED",
                               |        "activityId": "GenerateClaimForm",
                               |        "camundaEvent": "CREATE_CLAIM"
                               |      },
                               |      "applicant1LitigationFriendRequired": "Yes",
                               |      "applicant1LitigationFriend": {
                               |        "fullName": "John Smith",
                               |        "hasSameAddressAsLitigant": "No",
                               |        "primaryAddress": {
                               |          "AddressLine1": "Flat 2",
                               |          "AddressLine2": "Caversham House 15-17",
                               |          "AddressLine3": "Church Road",
                               |          "PostTown": "Reading",
                               |          "County": "Kent",
                               |          "Country": "United Kingdom",
                               |          "PostCode": "RG4 7AA"
                               |        },
                               |        "certificateOfSuitability": [
                               |          {
                               |            "id": "c4b60a56-7805-4486-81de-da1ee38e869a",
                               |            "value": {
                               |              "document": {
                               |                "document_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/3b89bc9f-3a12-4012-8d93-fb6ce3658011",
                               |                "document_binary_url": "http://dm-store-perftest.service.core-compute-perftest.internal/documents/3b89bc9f-3a12-4012-8d93-fb6ce3658011/binary",
                               |                "document_filename": "examplePDF.pdf"
                               |              }
                               |            }
                               |          }
                               |        ]
                               |      },
                               |      "submittedDate": "dateToday",
                               |      "paymentSuccessfulDate": "dateToday",
                               |      "caseBundles": []
                               |    },
                               |    "security_classification": null,
                               |    "callback_response_status": null
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