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
    "serviceauthorization" -> "${authToken}",
    "Content-Type" -> "multipart/form-data",
    "Accept" -> "*/*",
    "Host" -> dmStoreHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive"
  )

  /*DM Store Get Document Header
    serviceauthorization, userRole and userId value obtained from Authentication*/
  val dmStoreGetDocumentHeader = Map(
    "serviceauthorization" -> "${authToken}",
    "Accept" -> "*/*",
    "Host" -> dmStoreHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive",
    "user-roles" -> "${userRole}",
    "user-id" -> "${userId}",
  )

  val dmStoreDeleteDocumentHeader = Map(
    "serviceauthorization" -> "${authToken}",
    "Content-Type" -> "multipart/form-data",
    "Accept" -> "*/*",
    "Host" -> dmStoreHost,
    "Accept-Encoding" -> "gzip, deflate, br",
    "Connection" -> "keep-alive"
  )




}
