package sqlQueries

object sqlDMStore {

  /* get a list of documentIds.  This query will be refined when the data preparation is better understood.
     for now using a basic query for valid documents created by the EM repo */

  val sqlGetDownloadDocuments = """SELECT a.id AS "documentId" from storeddocument a
                                  |JOIN documentcontentversion b ON a.id = b.storeddocument_id
                                  |WHERE a.createdon > '2022-10-10 06:00:00'
                                  |AND a.deleted = false
                                  |AND b.originaldocumentname like 'EM_DMStore%'""".stripMargin

}
