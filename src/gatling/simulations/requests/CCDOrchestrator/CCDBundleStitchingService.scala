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
        successful stitching of the file as this is a synchronous call that will immediately respond with the new document details*/

  val CCDBundleCreateBundleSync =

    group("CCDBundle_CreateBundle") {
        DocumentGenerator.documentListGenerator(getRandomNumberIntBetweenValues(2,5))
        .exec(http("POST_CCD_Sync_Bundle")
            .post(ccdOrchestratorAPIURL + "/api/stitch-ccd-bundles")
            .headers(ccdBundlePostTaskHeader)
            .body(StringBody("#{documentJSON}")).asJson
            .check(jsonPath("$.data.caseBundles[0].value.id")))
    }


  val CCDBundleCreateBundleAsync =

    group("CCDBundle_CreateBundle") {
      DocumentGenerator.documentListGenerator(getRandomNumberIntBetweenValues(2,5))
        .exec(http("POST_CCD_ASync_Bundle")
          .post(ccdOrchestratorAPIURL + "/api/new-bundle")
          .headers(ccdBundlePostTaskHeader)
          .body(StringBody("#{documentJSON}")).asJson)
    }

}
