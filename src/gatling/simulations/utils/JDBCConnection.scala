package utils

import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilderBase
import io.gatling.jdbc.Predef._
import com.typesafe.config.ConfigFactory


object JDBCConnection {

  /*creates a db connection object to the specified database environment.
  The user must enter a sqlUserName and sqlPassword (These are obtained from the AI Key Vaults/secrets) for the connection to work.
  The db connection relies on a driver file jdbc dependency within the build.gradle file (gatling 'org.postgresql:postgresql:42.5.0')
  and the information for this was obtained from https://mvnrepository.com/artifact/org.postgresql/postgresql/42.5.0
  the connString definition can be used for any db connection.  The user just needs to add an additional case statement if another db
  is required*/

  def connString(envName: String, queryString: String) : FeederBuilderBase[Any] = {
    //receives an environment name and builds the appropriate dbConnectionString and username / password
    //initialise empty variables
    var dbConnectionString:String = null
    var sqlUserName:String = null
    var sqlPassword:String = null

    envName match {
      case "EVIDENCE" => {
        val dbName = "evidence"
        val sqlPort = "5432"
        sqlUserName = ConfigFactory.load.getString("auth.DMStoreDBUsername")
        sqlPassword = ConfigFactory.load.getString("auth.DMStoreDBPassword")
        dbConnectionString = "jdbc:postgresql://localhost:" + sqlPort + "/" + dbName +""
        println("Evidence JDBC connection string is " + dbConnectionString)
      }
      case _ => println("Invalid environment name specified")
    } //match

    //creare the feeder with the connection strings
    val sqlQueryFeeder =  jdbcFeeder(dbConnectionString,sqlUserName,sqlPassword,queryString)
    sqlQueryFeeder
  }
}