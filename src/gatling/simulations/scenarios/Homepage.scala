package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, CsrfCheck}
import scala.concurrent.duration._

object Homepage {

  val BaseURL = Environment.baseURL
  val IdamURL = Environment.idamURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader

  def NFDHomepage(URLSuffix: String) =

    exec(flushHttpCache)
    .exec(flushCookieJar)

    .group(s"NFD_001_${URLSuffix}HomePage") {

      exec(http("Load Homepage")
        .get(BaseURL + "/" + URLSuffix)
        .headers(CommonHeader)
        .header("sec-fetch-site", "none")
        .check(CsrfCheck.save)
        .check(substring("Sign in or create an account")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}