package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Environment {

  val dmStoreURL = "http://dm-store-#{env}.service.core-compute-#{env}.internal"
  val dmStoreHost = "dm-store-#{env}.service.core-compute-#{env}.internal"
  val rpeAPIURL = "http://rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
  val rpeHost = "rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
  val docAssemblyURL = "http://dg-docassembly-#{env}.service.core-compute-#{env}.internal"
  val docAssenblyHost = "dg-docassembly-#{env}.service.core-compute-#{env}.internal"
  val IdamURL = "https://idam-api.#{env}.platform.hmcts.net"
  val IdamHost = "idam-api.#{env}.platform.hmcts.net"
  val annoAPIURL = "http://em-anno-#{env}.service.core-compute-#{env}.internal"
  val annoHost = "em-anno-#{env}.service.core-compute-#{env}.internal"
  val npaAPIURL = "http://em-npa-#{env}.service.core-compute-#{env}.internal"
  val npaHost = "em-npa-#{env}.service.core-compute-#{env}.internal"
  val emStitchingURL = "http://em-stitching-#{env}.service.core-compute-#{env}.internal"
  val emStitchingHost = "em-stitching-#{env}.service.core-compute-#{env}.internal"
  val ccdOrchestratorAPIURL = "http://em-ccd-orchestrator-#{env}.service.core-compute-#{env}.internal"
  val ccdOrchestratorHost = "em-ccd-orchestrator-#{env}.service.core-compute-#{env}.internal"

  val HttpProtocol = http



}
