package requests.DMStore

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common._
import utils.Environment._
import utils.Headers._

object StoreDocumentUpdateService {


  /* PATCH /documents request to DM Store.  Updates the TTL value on any document in the doc store
  The request requires an S2SToken so Authentication.S2SAuth should be called prior to running this request.
  The S2SToken is sent within the dmStoreUpdateDocumentHeader
  #{documentId} variable is assigned from the feeder file.  The feeder is defined in the Simulation file.
  Response code returned must be 200 for the Update and also a json success payload.
*/

  val DMStoreUpdateDoc =

    group("DMStore_BulkUpdate") {
      exec(session => {
        session.setAll("ttlDate" -> currentDateTimePlus1Year("yyyy-MM-dd"), "ttlTime" -> currentDateTime("HH:mm:ss"))
      })
        .exec(http("PATCH_BulkUpdate")
          .patch(dmStoreURL + "/documents")
          .body(ElFileBody("bodies/DMStore_BulkUpdate.json")).asJson
          .headers(dmStoreUpdateDocumentHeader)
          .check(regex("Success")))
    }


}
