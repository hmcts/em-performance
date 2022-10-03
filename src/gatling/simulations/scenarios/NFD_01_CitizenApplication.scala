package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment}

import scala.concurrent.duration._
import scala.util.Random

object NFD_01_CitizenApplication {

  val BaseURL = Environment.baseURL
  val PaymentURL = Environment.paymentURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val rnd = new Random()

  val LandingPage =

    group("NFD01CitApp_010_YourDetailsSubmit") {
      exec(http("Your Details Submit")
        .post(BaseURL + "/your-details")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Has your marriage broken down irretrievably")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)


}
