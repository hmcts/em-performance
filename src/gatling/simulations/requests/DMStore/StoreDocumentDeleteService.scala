package requests.DMStore

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Environment._
import utils.Headers._

object StoreDocumentDeleteService {


  /* DELETE /documents request to DM Store.  Deletes the document (soft delete) from DM Store and until the
      The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
      The S2SToken is sent within the dmStoreDeleteDocumentHeader
      #{documentId} variable is assigned from the DELETE_DocumentData.csv feeder file.  The feeder is defined in the Simulation file.
      Response code returned must be 204 for the DELETE.
   */

  val DMStoreDocDelete =

    group("DMStore_DocDelete") {
      exec(http("DELETE_Documents")
        .delete(dmStoreURL + "/documents/#{documentId}")
        .headers(dmStoreDeleteDocumentHeader)
        .check(status is 204))
    }

}
