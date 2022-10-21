package requests
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment._
import utils.Headers._
import com.typesafe.config.ConfigFactory

object Authentication {

  val clientSecret = ConfigFactory.load.getString("auth.clientSecret")

    /*  Idam Authentication request to Idam service and returns a token
        The def requires a user type as input so that relevant session variables can be set
        The session variable bearerToken is created and can then be used in headers for EM requests
     */
  //userType must be "Caseworker", "Legal" or "Citizen"
  def IdamAuth(userType: String) =

    exec(session => userType match {
      case "Caseworker" => session.set("emailAddressCCD", "ccdloadtest-cw@gmail.com").set("userId", "ccdloadtest-cw@gmail.com").set("passwordCCD", "Password12").set("userRole", "caseworker")
      case "Legal" => session.set("emailAddressCCD", "ccdloadtest-la@gmail.com").set("passwordCCD", "Password12")
      case "Citizen" => session.set("emailAddressCCD", session("emailAddress").as[String]).set("passwordCCD", session("password").as[String])
    })

      .exec(http("IdamAuthentication")
        .post(IdamURL + "/o/token?grant_type=password&scope=openid%20profile%20roles%20openid%20roles%20profile" +
          "&username=#{userId}&password=#{passwordCCD}&client_id=ccd_gateway&client_secret=" + clientSecret)
        .headers(authenticateIdamPostHeader)
        .check(jsonPath("$.access_token").saveAs("bearerToken")))

  /*  S2S Authentication request to rpe service and returns a token
      The def requires a user type (currently using Caseworker) and a valid microService as input so that relevant session
      variables can be set.  The microservice correspond to json files in the bodies folder.
      The session variable authToken is created and can then be used in headers for EM requests
   */

  //userType must be "Caseworker", "Legal" or "Citizen"
  def S2SAuth(userType: String, microServiceType: String) =

    exec(session => userType match {
      case "Caseworker" => session.set("emailAddressCCD", "ccdloadtest-cw@gmail.com").set("userId", "ccdloadtest-cw@gmail.com").set("passwordCCD", "Password12").set("userRole", "caseworker").set("microService", microServiceType)
      case "Legal" => session.set("emailAddressCCD", "ccdloadtest-la@gmail.com").set("passwordCCD", "Password12").set("microService", microServiceType)
      case "Citizen" => session.set("emailAddressCCD", session("emailAddress").as[String]).set("passwordCCD", session("password").as[String]).set("microService", microServiceType)
    })

      .exec(http("S2SAuthentication")
        .post(rpeAPIURL + "/testing-support/lease")
        .headers(authenticateS2SPostHeader)
        .body(ElFileBody("bodies/#{microService}_MicroService.json")).asJson
        .check(regex("(.+)").saveAs("authToken")))

}
