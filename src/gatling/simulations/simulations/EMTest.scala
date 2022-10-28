package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.core.scenario.Simulation
import requests.{Authentication, DMStore, DocAssembly}
import utils.Environment._

import scala.concurrent.duration._
import scala.language.postfixOps


class EMTest extends Simulation {

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

  /*Hourly Volumes for DM Store requests*/
  val docUploadHourlyTarget:Double = 10000
  val docDownloadHourlyTarget:Double = 50000
  val docDownloadBinaryHourlyTarget:Double = 40000
  val docUpdateHourlyTarget:Double = 7500
  val docDeleteHourlyTarget: Double = 150
  /*Rate Per Second Volume for DM Store Requests */
  val docUploadRatePerSec = docUploadHourlyTarget / 3600
  val docDownloadRatePerSec = docDownloadHourlyTarget / 3600
  val docDownloadBinaryRatePerSec = docDownloadBinaryHourlyTarget / 3600
  val docUpdateRatePerSec = docUpdateHourlyTarget / 3600
  val docDeleteRatePerSec = docDeleteHourlyTarget / 3600

  /*Hourly Volumes for Doc Assembly requests*/
  val docAssemblyConvert: Double = 600
  /*Rate Per Second Volume for DM Store Requests */
  val docAssemblyConvertRatePerSec = docAssemblyConvert / 3600

  /* PIPELINE CONFIGURATION */
  val numberOfPipelineUsers = 1

  /* SIMULATION FEEDER FILES */
  /* DM Store */
  val DMDocumentDownloadFeeder = csv("feeders/GET_DocumentData.csv").circular
  val DMDocumentDownloadBinaryFeeder = csv("feeders/GET_DocumentData.csv").circular
  val DMDocumentDeleteFeeder = csv("feeders/DELETE_DocumentData.csv").random
  val DMDocumentUpdateFeeder = csv("feeders/GET_DocumentData.csv").random
  /* Doc Assembly */
  val DocAssemblyConvertFeeder = csv("feeders/POSTDocAssemblyConvert.csv").random


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

  /* DM STORE SCENARIOS*/

  //scenario for DM Store Document Upload
  val ScnDMStoreDocUpload = scenario("DMStore Document Upload")
    .exitBlockOnFail {
      exec(  _.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker", "CCD"))
        .exec(DMStore.DMStoreDocumentUploadSelector("TEST"))

    }

  //scenario for DM Store Document Download
  val ScnDMStoreDocDownload = scenario("DMStore Document Download")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker", "CCD"))
        .feed(DMDocumentDownloadFeeder)
        .exec(DMStore.DMStoreDocDownload)
    }

  //scenario for DM Store Document Download Binary
  val ScnDMStoreDocDownloadBinary = scenario("DMStore Document Download Binary")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker","CCD"))
        .feed(DMDocumentDownloadBinaryFeeder)
        .exec(DMStore.DMStoreDocDownloadBinary)
    }

  //scenario for DM Store Delete Document
  val ScnDMStoreDocDelete = scenario("DMStore Document Delete")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker","CCD"))
        .feed(DMDocumentDeleteFeeder)
        .exec(DMStore.DMStoreDocDelete)
    }

  //scenario for DM Store Bulk Update Document
  val ScnDMStoreUpdateDocument = scenario("DMStore Document Update")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker", "CCD"))
        .feed(DMDocumentUpdateFeeder)
        .exec(DMStore.DMStoreUpdateDoc)
    }




  //scenario for DocAssembly Convert Document
  val ScnDocAssemblyConvert = scenario("DocAssembly Document Convert")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(DocAssemblyConvertFeeder)
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .exec(DocAssembly.DocAssemblyConvert)
    }



  /*EM STORE SIMULATIONS */

  setUp(
    //DM Store Simulations
    ScnDMStoreDocUpload.inject(simulationProfile(testType, docUploadRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnDMStoreDocDownload.inject(simulationProfile(testType, docDownloadRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnDMStoreDocDownloadBinary.inject(simulationProfile(testType, docDownloadBinaryRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnDMStoreUpdateDocument.inject(simulationProfile(testType, docUpdateRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnDMStoreDocDelete.inject(simulationProfile(testType, docDeleteRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    //DocAssembly Simulations
    ScnDocAssemblyConvert.inject(simulationProfile(testType, docUpdateRatePerSec, numberOfPipelineUsers)).pauses(pauseOption)
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
