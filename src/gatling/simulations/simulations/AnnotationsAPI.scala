package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.core.scenario.Simulation
import requests.Annotations._
import requests.{Authentication, DMStore}
import utils.Environment._

import scala.concurrent.duration._
import scala.language.postfixOps


class AnnotationsAPI extends Simulation {

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
  /*Hourly Volumes for Annotation requests*/
  val createBookmarkHourlyTarget:Double = 250
  val getBookmarksHourlyTarget:Double = 10000
  val getMetadataHourlyTarget:Double = 10000
  /*Rate Per Second Volume for Annotation Requests */
  val createBookmarkRatePerSec = createBookmarkHourlyTarget / 3600
  val getBookmarksRatePerSec = getBookmarksHourlyTarget / 3600
  val getMetadataRatePerSec = getMetadataHourlyTarget / 3600

  /* PIPELINE CONFIGURATION */
  val numberOfPipelineUsers = 1

  /* SIMULATION FEEDER FILES*/
  val AnnoCreateBookmarkFeeder = csv("feeders/ANNO_DocumentData.csv").circular

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

  /* ANNOTATION SCENARIOS*/

  //scenario for DM Store Document Upload
  val ScnAnnoCreateBookmark = scenario("Annotations Create Bookmark")
    .exitBlockOnFail {
      exec(  _.set("env", s"${env}"))
        .feed(AnnoCreateBookmarkFeeder)
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .exec(BookmarkService.BookmarkCreateBookmark)
    }

  val ScnAnnoGetBookmarks = scenario("Annotations Get Bookmarks")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(AnnoCreateBookmarkFeeder)
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .exec(BookmarkService.BookmarkGetBookmarks)
    }

  val ScnAnnoGetMetadata = scenario("Annotations Get Metadata")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(AnnoCreateBookmarkFeeder)
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .exec(MetadataService.MetadataGetMetadata)
    }




  /*DM STORE SIMULATIONS */

  setUp(
    ScnAnnoCreateBookmark.inject(simulationProfile(testType, createBookmarkRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnAnnoGetBookmarks.inject(simulationProfile(testType, getBookmarksRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnAnnoGetMetadata.inject(simulationProfile(testType, getMetadataRatePerSec, numberOfPipelineUsers)).pauses(pauseOption)
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
