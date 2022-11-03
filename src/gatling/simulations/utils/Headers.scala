package utils
import utils.Environment._


object Headers {

  //S2S Authentication Header
  val authenticateS2SPostHeader = Map(
    "Accept" -> "*/*",
    "Host" -> rpeHost,
    "Content-Type" -> "application/json",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive"
  )


  /*DM Store Post Document Header
  serviceauthorization value obtained from Authentication */
  val dmStorePostDocumentHeader = Map(
    "serviceauthorization" -> "#{authToken}",
    "Content-Type" -> "multipart/form-data",
    "Accept" -> "*/*",
    "Host" -> dmStoreHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive"
  )

  /*DM Store Get Document Header
    serviceauthorization, userRole and userId value obtained from Authentication*/
  val dmStoreGetDocumentHeader = Map(
    "ServiceAuthorization" -> "#{authToken}",
    "Accept" -> "*/*",
    "Host" -> dmStoreHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-roles" -> "#{userRole}",
    "user-id" -> "#{userId}",
  )

  val dmStoreDeleteDocumentHeader = Map(
    "serviceauthorization" -> "#{authToken}",
    "Accept" -> "*/*",
    "Host" -> dmStoreHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive"
  )

  val docAssemblyConvertHeader = Map(
    "serviceauthorization" -> "#{authToken}",
    "Authorization" -> "Bearer #{bearerToken}",
    "Accept" -> "*/*",
    "Host" -> docAssenblyHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive"
  )

  val docAssemblyRenderTemplateHeader = Map(
    "serviceauthorization" -> "#{authToken}",
    "Authorization" -> "Bearer #{bearerToken}",
    "Accept" -> "*/*",
    "Host" -> docAssenblyHost,
    "Content-Type" -> "application/json",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-id" -> "#{userId}"
  )

  val dmStoreUpdateDocumentHeader = Map(
    "ServiceAuthorization" -> "#{authToken}",
    "Accept" -> "*/*",
    "Host" -> dmStoreHost,
    "Content-Type" -> "application/json",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-id" -> "#{userId}",
  )

  /*ANNOTATIONS HEADERS*/
  val annoCreateBookmarkHeader = Map(
    "ServiceAuthorization" -> "#{authToken}",
    "Authorization" -> "Bearer #{bearerToken}",
    "Accept" -> "*/*",
    "Host" -> annoHost,
    "Content-Type" -> "application/json",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-id" -> "#{userId}",
  )

  val annoGetMetadataHeader = Map(
    "ServiceAuthorization" -> "#{authToken}",
    "Authorization" -> "Bearer #{bearerToken}",
    "Accept" -> "*/*",
    "Host" -> annoHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    //"user-id" -> "#{userId}",
  )

  /*NPA HEADERS*/
  val npaGetMarkupsHeader = Map(
    "ServiceAuthorization" -> "#{authToken}",
    "Authorization" -> "Bearer #{bearerToken}",
    "Accept" -> "*/*",
    "Host" -> npaHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-id" -> "#{userId}"
  )

  val npaPostMarkupsHeader = Map(
    "ServiceAuthorization" -> "#{authToken}",
    "Authorization" -> "Bearer #{bearerToken}",
    "Content-Type" -> "application/json",
    "Accept" -> "*/*",
    "Host" -> npaHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-id" -> "#{userId}"
  )

    /*EM STITCHING HEADERS*/
    val emStitchingPostTaskHeader = Map(
      "ServiceAuthorization" -> "#{authToken}",
      "Authorization" -> "Bearer #{bearerToken}",
      "Content-Type" -> "application/json",
      "Accept" -> "*/*",
      "Host" -> emStitchingHost,
      "Accept-Encoding" -> "gzip, deflate, br",
      "Connection" -> "keep-alive",
      "user-id" -> "#{userId}"
    )

  /*CCD ORCHESTRATOR HEADERS*/
  val ccdBundlePostTaskHeader = Map(
    "ServiceAuthorization" -> "#{authToken}",
    "Authorization" -> "Bearer #{bearerToken}",
    "Content-Type" -> "application/json",
    "Accept" -> "*/*",
    "Host" -> ccdOrchestratorHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-id" -> "#{userId}"
  )












}
