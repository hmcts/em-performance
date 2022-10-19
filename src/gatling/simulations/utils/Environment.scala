package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Environment {

  val dmStoreURL = "http://dm-store-#{env}.service.core-compute-#{env}.internal"
  val dmStoreHost = "dm-store-#{env}.service.core-compute-#{env}.internal"
  val rpeAPIURL = "http://rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
  val rpeHost = "rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"

  val HttpProtocol = http



}
