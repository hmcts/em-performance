package requests.DocAssembly

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment._
import utils.Headers._

object DocumentConversion {

  /*  POST /api/convert/{documentId} request to doc Assembly.  This call triggers the conversion of a document to PDF for viewing.
      The request requires an S2SToken and Bearer (Idam) so Authentication.S2SAuth and Authentication.IdamAuth should be called prior to running this request.
      The auth tokens are sent within the docAssemblyConvertHeader
      #{documentId} variable is assigned from the POSTDocAssemblyConvert.csv feeder file.  The feeder is defined in the Simulation file
      Ideally, documents should be non-PDF to meet requirements for the call.
   */

  val DocAssemblyConvert =

    group("DocAssembly_DocConvert") {
      exec(http("POST_DocAssemblyConvert_#{fileSize}")
        .post(docAssemblyURL + "/api/convert/#{documentId}")
        .headers(docAssemblyConvertHeader)
        .check(bodyBytes.transform(_.size > 100).is(true)))
    }


}
