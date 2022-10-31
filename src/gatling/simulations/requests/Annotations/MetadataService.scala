package requests.Annotations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Environment._
import utils.Headers._


object MetadataService {

  /* GET request for getting all metadata associated with a document.  The request requires both an S2S token and Idam token and this should be
     called prior to the request being made.  A documentId should be supplied for the request.
     A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these documents can be used to create the bookmarks*/


  val MetadataGetMetadata =

    group("Annotations_Metadata") {
        exec(http("GET_Metadata")
          .get(annoAPIURL + "/api/metadata/#{documentId}")
          .headers(annoGetMetadataHeader))
    }




}
