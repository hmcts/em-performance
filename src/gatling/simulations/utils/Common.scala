package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random
import java.time._
import java.text.SimpleDateFormat
import java.util.Calendar





object Common {

  val rnd = new Random()
  val now = LocalDate.now()
  val patternDay = DateTimeFormatter.ofPattern("dd")
  val patternMonth = DateTimeFormatter.ofPattern("MM")
  val patternYear = DateTimeFormatter.ofPattern("yyyy")
  val patternDate = DateTimeFormatter.ofPattern("yyyyMMdd")


  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getDate(): String = {
    now.format(patternDate)
  }


  def getDay(): String = {
    (1 + rnd.nextInt(28)).toString.format(patternDay).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  def getMonth(): String = {
    (1 + rnd.nextInt(12)).toString.format(patternMonth).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  //Dob >= 25 years
  def getMarriageYear(): String = {
    now.minusYears(25 + rnd.nextInt(70)).format(patternYear)
  }

  def getPostcode(): String = {
    randomString(2).toUpperCase() + rnd.nextInt(10).toString + " " + rnd.nextInt(10).toString + randomString(2).toUpperCase()
  }

  // returns a date in format specified in the input pattern e.g. 'yyyy-MM-dd'
  def currentDate(pattern:String): String = {
    val currentDateFormatted = LocalDateTime.now.format(DateTimeFormatter.ofPattern(pattern))
    currentDateFormatted
  }

  // returns current time in format specified in the input pattern eg 'HH:mm:ss'
  def currentTime(pattern:String): String = {
    val currentTimeFormatted = LocalDateTime.now.format(DateTimeFormatter.ofPattern(pattern))
    currentTimeFormatted
  }



}