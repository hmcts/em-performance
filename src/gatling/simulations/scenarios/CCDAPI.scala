package scenarios

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment

object CCDAPI {

  val BaseURL = Environment.baseURL
  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL
  val CcdAPIURL = Environment.ccdAPIURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val clientSecret = ConfigFactory.load.getString("auth.clientSecret")

  //userType must be "Caseworker", "Legal" or "Citizen"
  def Auth(userType: String) =

    exec(session => userType match {
      case "Caseworker" => session.set("emailAddressCCD", "ccdloadtest-cw@gmail.com").set("passwordCCD", "Password12")
      case "Legal" => session.set("emailAddressCCD", "ccdloadtest-la@gmail.com").set("passwordCCD", "Password12")
      case "Citizen" => session.set("emailAddressCCD", session("emailAddress").as[String]).set("passwordCCD", session("password").as[String])
    })

    .exec(http("NFD_000_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"nfdiv_case_api"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

    .exec(http("NFD_000_GetBearerToken")
      .post(IdamAPIURL + "/o/token")
      .formParam("grant_type", "password")
      .formParam("username", "${emailAddressCCD}")
      .formParam("password", "${passwordCCD}")
      .formParam("client_id", "ccd_gateway")
      .formParam("client_secret", clientSecret)
      .formParam("scope", "openid profile roles openid roles profile")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("bearerToken")))

    .pause(1)

    .exec(http("NFD_000_GetIdamID")
      .get(IdamAPIURL + "/details")
      .header("Authorization", "Bearer ${bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))

    .pause(1)

  val GetMarriageDetails =

    exec(Auth("Caseworker"))

    .exec(http("NFD_000_GetCase")
      .get(CcdAPIURL + "/caseworkers/${idamId}/jurisdictions/DIVORCE/case-types/NFD/cases/${caseId}")
      .header("Authorization", "Bearer ${bearerToken}")
      .header("ServiceAuthorization", "${authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$.case_data.marriageDate").saveAs("marriageDate"))
      .check(jsonPath("$.case_data.marriageApplicant1Name").saveAs("marriageApplicant1Name"))
      .check(jsonPath("$.case_data.marriageApplicant2Name").saveAs("marriageApplicant2Name")))

    .pause(1)

  val GetCaseIdAndAccessCode =

    exec(Auth("Citizen"))

    .exec(http("NFD_000_GetCase")
      .get(CcdAPIURL + "/citizens/${idamId}/jurisdictions/DIVORCE/case-types/NFD/cases")
      .header("Authorization", "Bearer ${bearerToken}")
      .header("ServiceAuthorization", "${authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$[0].id").saveAs("caseId"))
      .check(jsonPath("$[0].case_data.accessCode").saveAs("accessCode")))

    .pause(1)

  // allows the event to be used where the userType = "Caseworker" or "Legal"
  def CreateEvent(userType: String, eventName: String, payloadPath: String) =

    exec(_.set("eventName", eventName))

    .exec(Auth(userType))

    .exec(http("NFD_000_GetCCDEventToken")
      .get(CcdAPIURL + "/caseworkers/${idamId}/jurisdictions/DIVORCE/case-types/NFD/cases/${caseId}/event-triggers/${eventName}/token")
      .header("Authorization", "Bearer ${bearerToken}")
      .header("ServiceAuthorization", "${authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$.token").saveAs("eventToken")))

    .pause(1)

    .exec(http("NFD_000_CCDEvent-${eventName}")
      .post(CcdAPIURL + "/caseworkers/${idamId}/jurisdictions/DIVORCE/case-types/NFD/cases/${caseId}/events")
      .header("Authorization", "Bearer ${bearerToken}")
      .header("ServiceAuthorization", "${authToken}")
      .header("Content-Type", "application/json")
      .body(ElFileBody(payloadPath))
      .check(jsonPath("$.id")))

    .pause(1)

}
