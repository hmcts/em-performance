package requests.Annotations

import utils.Environment._
import utils.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._



object AnnotationSetService {


  /* DELETE request for annotation sets.  The request requires both an S2S token and Idam token and this should be
      called prior to the request being made.  There needs to be an active annotationsId available to delete so it needs to be run after
      an annotations creation*/

  val AnnotationsSetDeleteAnnotation =

    group("Annotations_Set") {
      exec(http("DELETE_AnnotationSet")
        .delete(annoAPIURL + "/api/annotation-sets/#{annotationId}")
        .headers(annoDeleteAnnotationHeader)
        .check(status is 200))
    }


}
