package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.core.scenario.Simulation
import requests.NPA._
import requests.Authentication


import utils.Environment._

import scala.concurrent.duration._
import scala.language.postfixOps


class NPAAPI extends Simulation {

  val dmBaseURL = dmStoreURL

  /* TEST TYPE DEFINITION */
  /* pipeline = nightly pipeline against the AAT environment (see the Jenkins_nightly file) */
  /* perftest (default) = performance test against the perftest environment */
  val testType = scala.util.Properties.envOrElse("TEST_TYPE", "perftest")

  //set the environment based on the test type
  val environment = testType match{
    case "perftest" => "perftest"
    case "pipeline" => "perftest" //updated pipeline to run against perftest - change to aat to run against AAT
    case _ => "**INVALID**"
  }
  /* ******************************** */

  /* ADDITIONAL COMMAND LINE ARGUMENT OPTIONS */
  val debugMode = System.getProperty("debug", "off") //runs a single user e.g. ./gradle gatlingRun -Ddebug=on (default: off)
  val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat
  /* ******************************** */

  /* PERFORMANCE TEST CONFIGURATION */
  val rampUpDurationMins = 5
  val rampDownDurationMins = 5
  val testDurationMins = 60

  /*Hourly Volumes for NPA requests*/
  val getMarkupHourlyTarget:Double = 12000
  val createMarkupHourlyTarget:Double = 500
  val burnRedactionHourlyTarget:Double = 200


  /*Rate Per Second Volume for DM Store Requests */
  val getMarkupRatePerSec = getMarkupHourlyTarget / 3600
  val createMarkupRatePerSec = createMarkupHourlyTarget / 3600
  val burnRedactionRatePerSec = burnRedactionHourlyTarget/ 3600


  /* PIPELINE CONFIGURATION */
  val numberOfPipelineUsers = 1

  /* SIMULATION FEEDER FILES */
  /* NPA */
  val NPAgetMarkupFeeder = csv("feeders/ANNO_DocumentData.csv").circular
  val NPABurnMarkupFeeder = csv("feeders/NPA_BurnRedaction.csv").random



  //If running in debug mode, disable pauses between steps
  val pauseOption:PauseType = debugMode match{
    case "off" => constantPauses
    case _ => disabledPauses
  }
  /* ******************************** */

  //  /* PIPELINE CONFIGURATION */
  //  val numberOfPipelineUsersSole:Double = 5
  //  val numberOfPipelineUsersJoint:Double = 5
  /* ******************************** */

  val httpProtocol = HttpProtocol
    .baseUrl(dmBaseURL)
    .doNotTrackHeader("1")
    .inferHtmlResources()
    .silentResources

  before{
    println(s"Test Type: ${testType}")
    println(s"Test Environment: ${env}")
    println(s"Debug Mode: ${debugMode}")
  }


  //defines the Gatling simulation model, based on the inputs
  def simulationProfile(simulationType: String, userPerSecRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(
            rampUsersPerSec(0.00) to (userPerSecRate) during (rampUpDurationMins minutes),
            constantUsersPerSec(userPerSecRate) during (testDurationMins minutes),
            rampUsersPerSec(userPerSecRate) to (0.00) during (rampDownDurationMins minutes)
          )
        }
        else{
          Seq(atOnceUsers(1))
        }
      case "pipeline" =>
        Seq(rampUsers(numberOfPipelineUsers.toInt) during (2 minutes))
      case _ =>
        Seq(nothingFor(0))
    }
  }

  //defines the test assertions, based on the test type
  def assertions(simulationType: String): Seq[Assertion] = {
    simulationType match {
      case "perftest" =>
        Seq(global.successfulRequests.percent.gte(95))
      case "pipeline" =>
        Seq(global.successfulRequests.percent.gte(95))
      case _ =>
        Seq()
    }
  }

  /* NPA SCENARIOS*/

  //scenario for NPA Get Markups Download
  val ScnNPAGetMarkup = scenario("NPA_GET_Markup")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .feed(NPAgetMarkupFeeder)
        .exec(MarkupService.MarkupGetMarkups)
    }

  //scenario for NPA Post and Delete Markups (Redaction)
  val ScnNPACreateDeleteMarkup = scenario("NPA_POST_DELETE_Markup")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .feed(NPAgetMarkupFeeder)
        .exec(MarkupService.MarkupCreateMarkups)
        .exec(MarkupService.MarkupDeleteMarkup)
    }

  //scenario for Burn Markup
  val ScnNPABurnMarkup = scenario("NPA_POST_BurnRedaction")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .feed(NPABurnMarkupFeeder)
        .exec(MarkupService.MarkupCreateMarkups)
        .exec(RedactionsService.MarkupBurnMarkups)
    }




  /*NPA SIMULATIONS */

  setUp(
    //NPA Simulations
    ScnNPAGetMarkup.inject(simulationProfile(testType, getMarkupRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnNPACreateDeleteMarkup.inject(simulationProfile(testType, createMarkupRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnNPABurnMarkup.inject(simulationProfile(testType, burnRedactionRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
