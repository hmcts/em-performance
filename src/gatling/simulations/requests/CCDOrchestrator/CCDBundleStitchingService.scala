package requests.CCDOrchestrator
import io.circe.Json
import utils.Environment._
import utils.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._

import java.util.UUID


object CCDBundleStitchingService {


  /* POST request for creating a document bundle.  The request requires both an S2S token and Idam token and this should be
        called prior to the request being made (the tokens are sent in the header).  A case bundle and new document will be created on
        successful stitching of the file as this is a synchronous call that will immediately respond with the new document details
        The function documentListGenerator will automatically generate the JSON required for the payload - currently set by up to 3
        documents being stitched with a mixture of pdf and word documents currently being used.*/

  val CCDBundleCreateBundleSync =

    group("CCDBundle_CreateSyncBundle") {
        DocumentGenerator.documentListGenerator(getRandomNumberIntBetweenValues(2,5))
        .exec(http("POST_CCD_Sync_Bundle_#{pageCount}")
            .post(ccdOrchestratorAPIURL + "/api/stitch-ccd-bundles")
            .headers(ccdBundlePostTaskHeader)
            .body(StringBody("#{documentJSON}")).asJson
            .check(jsonPath("$.data.caseBundles[0].value.id")))
    }


  val CCDBundleCreateBundleAsync =

    group("CCDBundle_CreateAsyncBundle") {
      DocumentGenerator.documentListGenerator(getRandomNumberIntBetweenValues(2,3))
        .exec(http("POST_CCD_ASync_Bundle_#{pageCount}")
          .post(ccdOrchestratorAPIURL + "/api/new-bundle")
          .headers(ccdBundlePostTaskHeader)
          .body(StringBody("#{documentJSON}")).asJson)
    }

}
