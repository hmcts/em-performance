package requests.Docmosis

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment

object Docmosis {

  val Render =

    group("Docmosis_Render") {
      exec(http("POST_Render")
        .post(Environment.docmosisURL + "/rs/render")
        .header("content-type", "multipart/form-data")
        .formParam("templateName", "FL-FRM-APP-ENG-00002.docx")
        .formParam("accessKey", "dyVv8pXwQ03RRyJZQIPX2RWP9LgJJGTU08kc9dA8ATJoA9EZXQEWe7L1Uwe")
        .formParam("outputName", "d3743940-2356-4efc-9b2f-f72e890726ea.pdf")
        .formParam("data", "{“a”: 1}")
        .check(status in (200, 404, 204)))
    }

  val Convert =

    group("Docmosis_Convert") {
      exec(http("POST_Convert")
        .post(Environment.docmosisURL + "/rs/convert")
        .header("Content-Type", "multipart/form-data")
        .header("Accept", "PDF_CONTENT_TYPE")
        .bodyPart(RawFileBodyPart("file", "data/1MB.docx")
          .fileName("1MB.docx")
          .contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .formParam("accessKey", "dyVv8pXwQ03RRyJZQIPX2RWP9LgJJGTU08kc9dA8ATJoA9EZXQEWe7L1Uwe")
        .formParam("outputName", "1mb-file.pdf")
        .check(status in(200, 404, 204)))
    }

}
