package requests.CCDOrchestrator
import utils.Environment._
import utils.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._

import java.util.UUID


object CCDBundleStitchingService {

  /* POST request for creating a document bundle.  The request requires both an S2S token and Idam token and this should be
        called prior to the request being made (the tokens are sent in the header).  A 201 response is expected on submission
        of a successful markup and a redactionId value is captured into session for use in the DELETE request*/

  val CCDBundleCreateBundleSync =

    group("CCDBundle_CreateBundle") {
        exec(session => {
          val bundleId = getUUID()
          session.setAll("bundleId" -> bundleId)
        })
          .exec(http("POST_CCD_Sync_Bundle")
            .post(ccdOrchestratorAPIURL + "/api/stitch-ccd-bundles")
            .headers(ccdBundlePostTaskHeader)
            .body(ElFileBody("bodies/CCD_POST_Bundle.json")).asJson
            .check(jsonPath("$.data.caseBundles[0].value.id")))
    }


  val CCDBundleCreateBundleAsync =

    group("CCDBundle_CreateBundle") {
      exec(session => {
        val bundleId = getUUID()
        session.setAll("bundleId" -> bundleId)
      })
        .exec(http("POST_CCD_ASync_Bundle")
          .post(ccdOrchestratorAPIURL + "/api/new-bundle")
          .headers(ccdBundlePostTaskHeader)
          .body(ElFileBody("bodies/CCD_POST_Bundle.json")).asJson)
          //.check(jsonPath("$.data.caseBundles[0].value.id")))
    }




}
