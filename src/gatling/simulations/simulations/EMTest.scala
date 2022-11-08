package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.core.scenario.Simulation
import requests.Annotations._
import requests.NPA._
import requests.CCDOrchestrator._
import requests._
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
  val docAssemblyConvertHourlyTarget: Double = 600
  /*Rate Per Second Volume for DM Store Requests */
  val docAssemblyConvertRatePerSec = docAssemblyConvertHourlyTarget / 3600

  /*Hourly Volumes for Annotations requests*/
  val AnnoCreateBookmarkHourlyTarget: Double = 600
  val getBookmarksHourlyTarget:Double = 10000
  val getMetadataHourlyTarget:Double = 10000
  val createDeleteAnnotationsHourlyTarget: Double = 400
  val getSetFilterAnnotations: Double = 400
  /*Rate Per Second Volume for DM Store Requests */
  val AnnoCreateBookmarkRatePerSec = AnnoCreateBookmarkHourlyTarget / 3600
  val getBookmarksRatePerSec = getBookmarksHourlyTarget /3600
  val getMetadataRatePerSec = getMetadataHourlyTarget / 3600
  val createDeleteAnnotationsRatePerSec = createDeleteAnnotationsHourlyTarget / 3600
  val getSetFilterAnnotationsRatePerSec = getSetFilterAnnotations / 3600

  /*Hourly Volumes for NPA requests*/
  val getMarkupHourlyTarget: Double = 10000
  val createMarkupHourlyTarget: Double = 300
  /*Rate Per Second Volume for DM Store Requests */
  val getMarkupRatePerSec = getMarkupHourlyTarget / 3600
  val createMarkupRatePerSec = createMarkupHourlyTarget / 3600

  /*Hourly Volumes for Stitching requests*/
  val CreateBundleHourlyTarget: Double = 400
  /*Rate Per Second Volume for DM Store Requests */
  val CreateBundleRatePerSec = CreateBundleHourlyTarget / 3600

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
  /* Annotations */
  val AnnoCreateBookmarkFeeder = csv("feeders/ANNO_DocumentData.csv").circular
  /* NPA */
  val NPAgetMarkupFeeder = csv("feeders/ANNO_DocumentData.csv").circular


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

  //scenario for Annotations Create Bookmark
  val ScnAnnoCreateBookmark = scenario("Annotations Create Bookmark")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
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

  val ScnAnnoCreateDeleteAnnotations = scenario("Annotations Create Annotations")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(AnnoCreateBookmarkFeeder)
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .exec(AnnotationsService.AnnotationsCreateAnnotation)
        .exec(AnnotationsService.AnnotationsDeleteAnnotation)
    }

  val ScnAnnoSetFilterGetFilter = scenario("Annotations Set Filter")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .feed(AnnoCreateBookmarkFeeder)
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .exec(AnnotationsSetFilterService.AnnotationsSetFilterGetFilterAnnotation)
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

  /* CCD STITCHING SCENARIOS*/
  val ScnStitchBundles = scenario("CCD_STITCH_BUNDLES")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}"))
        .exec(Authentication.S2SAuth("Caseworker", "EM_GW"))
        .exec(Authentication.IdamAuth("Caseworker"))
        .exec(CCDBundleStitchingService.CCDBundleCreateBundleSync)
        .exec(CCDBundleStitchingService.CCDBundleCreateBundleAsync)
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
    ScnDocAssemblyConvert.inject(simulationProfile(testType, docAssemblyConvertRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    //Annotations Simulations
    ScnAnnoCreateBookmark.inject(simulationProfile(testType, AnnoCreateBookmarkRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnAnnoGetBookmarks.inject(simulationProfile(testType, getBookmarksRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnAnnoGetMetadata.inject(simulationProfile(testType, getMetadataRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnAnnoCreateDeleteAnnotations.inject(simulationProfile(testType, createDeleteAnnotationsRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnAnnoSetFilterGetFilter.inject(simulationProfile(testType, getSetFilterAnnotationsRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    //NPA Simulations
    ScnNPAGetMarkup.inject(simulationProfile(testType, getMarkupRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    ScnNPACreateDeleteMarkup.inject(simulationProfile(testType, createMarkupRatePerSec, numberOfPipelineUsers)).pauses(pauseOption),
    //STITCHING Simulations
    ScnStitchBundles.inject(simulationProfile(testType, CreateBundleRatePerSec, numberOfPipelineUsers)).pauses(pauseOption)
  ).protocols(httpProtocol)
    .assertions(assertions(testType))

}
