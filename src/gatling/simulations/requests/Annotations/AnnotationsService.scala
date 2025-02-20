package requests.Annotations

import utils.Common._
import utils.Environment._
import utils.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._



object AnnotationsService {

  /*function to create an annotation.  Firstly a call to get filter on annotation is made to find if an existing annotation exists.
  if it does then delete the annotation and annotation-set using the relevant delete commands.  Create the annotation and then do a
  delete tidy up on the annotations again.  this means that the data set can be used again multiple times.  */

  def createAnnotation() = {

    exec(AnnotationsSetFilterService.AnnotationsSetFilterGetFilterAnnotation)
    .doIf("#{annotationIdExists.exists()}")
     {
       exec(session => {
         val annotationIdValue = session("annotationIdExists").as[String]
         session.set("annotationId",annotationIdValue)
       })
       .exec(AnnotationSetService.AnnotationsSetDeleteAnnotation)
       .exec(session => session.removeAll("annotationIdExists","annotationId"))
     }
    .exec(AnnotationsService.AnnotationsCreateAnnotation)
    .exec(AnnotationSetService.AnnotationsSetDeleteAnnotation)
  }

    /* POST request for creating an annotation within a document.  The request requires both an S2S token and Idam token and this should be
       called prior to the request being made.  */


    val AnnotationsCreateAnnotation =
      group("Annotations_Annotations") {
        exec(session => {
          session.setAll("annotationId" -> getUUID(), "createdBy" -> getUUID(), "annotationSetId" -> getUUID(), "rectangleId" -> getUUID(),
          "rectangleX" -> getRandomNumberIntBetweenValues(1,100), "rectangleY" -> getRandomNumberIntBetweenValues(1,100),
          "rectangleWidth" -> getRandomNumberIntBetweenValues(1,100), "rectangleHeight" -> getRandomNumberIntBetweenValues(1,100),
          "commentsId" -> getUUID(), "currentDateTime" -> currentDateTime("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        })
        .exec(http("POST_Annotation")
          .post(annoAPIURL + "/api/annotations")
          .headers(annoCreateAnnotationHeader)
          .body(ElFileBody("bodies/ANNO_Annotations.json")).asJson
          .check(status is 201))
      }



    /* DELETE request for annotation objects.  The request requires both an S2S token and Idam token and this should be
        called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
        documents can be used to delete the annotations.  There needs to be an active annotationsId available to delete so it needs to be run after
        an annotations creation*/

    val AnnotationsDeleteAnnotation =

      group("Annotations_Annotations") {
        exec(http("DELETE_Annotation")
          .delete(annoAPIURL + "/api/annotations/#{annotationId}")
          .headers(annoDeleteAnnotationHeader)
          .check(status is 200))
      }


  }
