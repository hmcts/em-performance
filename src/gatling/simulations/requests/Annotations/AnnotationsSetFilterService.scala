package requests.Annotations

import utils.Environment._
import utils.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object AnnotationsSetFilterService {

  /* GET request for filtering an annotation set associated with a document.  The request requires both an S2S token and Idam token and this should be
      called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
      documents can be used to retrieve the annotations.  An annotation should exist on the document in the first place for a 200 response, otherwise a 404
      message is returned (even with a valid request) when an annotation does not exist!  The test looks for both of these conditions to avoid failures*/

  val AnnotationsSetFilterGetFilterAnnotation =

    group("Annotations_FilterAnnotation") {
      exec(http("GET_Filter")
        .get(annoAPIURL + "/api/annotation-sets/filter")
        .queryParam("documentId","#{documentId}")
        .headers(annoSetFilterAnnotationHeader)
        .check(status in (200,404)))
    }

}
