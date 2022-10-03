package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._
import scala.util.Random

object NFD_01_CitizenApplication {

  val BaseURL = Environment.baseURL
  val PaymentURL = Environment.paymentURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val postcodeFeeder = csv("postcodes.csv").random

  val rnd = new Random()

  val LandingPage =

    group("NFD01CitApp_010_YourDetailsSubmit") {
      exec(http("Your Details Submit")
        .post(BaseURL + "/your-details")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("gender", Case.Gender.Female)
        .formParam("sameSex", Case.Checkbox.Unchecked)
        .check(CsrfCheck.save)
        .check(substring("Has your marriage broken down irretrievably")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val MarriageBrokenDown =

    group("NFD01CitApp_020_${userType}MarriageBrokenDown") {
      exec(http("Marriage Broken Down Submit")
        .post(BaseURL + "/${userTypeURL}irretrievable-breakdown")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}ScreenHasUnionBroken", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(regex("When did you get married?|Enter your name")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val MarriageCertificate =

    group("NFD01CitApp_030_DateFromCertificateSubmit") {
      exec(http("Date From Certificate Submit")
        .post(BaseURL + "/date-from-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("relationshipDate-day", Common.getDay())
        .formParam("relationshipDate-month", Common.getMonth())
        .formParam("relationshipDate-year", Common.getMarriageYear())
        .check(CsrfCheck.save)
        .check(substring("Do you have your marriage certificate with you?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_040_HasMarriageCertificateSubmit") {
      exec(http("Has Marriage Certificate Submit")
        .post(BaseURL + "/do-you-have-your-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("hasCertificate", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Do you need help paying the fee for your divorce?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_050_HelpWithYourFeeSubmit") {
      exec(http("Help With Your Fee Submit")
        .post(BaseURL + "/help-with-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1HelpPayingNeeded", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("How do you want to apply for the divorce?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val HowDoYouWantToApply =

    group("NFD01CitApp_060_HowDoYouWantToApply") {
      exec(http("How do you want to apply")
        .post(BaseURL + "/how-do-you-want-to-apply")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicationType", "${appType}Application")
        .check(CsrfCheck.save)
        .check(regex("Did you get married in the UK?|Enter your wife&#39;s email address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val EnterTheirEmailAddress =

    group("NFD01CitApp_065_EnterTheirEmailAddress") {
      exec(http("Enter their email address")
        .post(BaseURL + "/their-email-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2EmailAddress", "${Applicant2EmailAddress}")
        .formParam("applicant1DoesNotKnowApplicant2EmailAddress", "")
        .check(CsrfCheck.save)
        .check(substring("Did you get married in the UK?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)


  val Jurisdictions =

    group("NFD01CitApp_070_InTheUKSubmit") {
      exec(http("In The UK Submit")
        .post(BaseURL + "/in-the-uk")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("inTheUk", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Check if you can get a divorce in England or Wales")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_080_CheckJurisdictionSubmit") {
      exec(http("Check Jurisdiction Submit")
        .post(BaseURL + "/check-jurisdiction")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Where your lives are based")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_090_WhereYourLivesAreBasedSubmit") {
      exec(http("Where Your Lives Are Based Submit")
        .post(BaseURL + "/where-your-lives-are-based")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1LifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .formParam("applicant2LifeBasedInEnglandAndWales", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("You can use English or Welsh courts to get a divorce")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_100_CanUseEnglishWelshCourt") {
      exec(http("English or Welsh courts")
        .post(BaseURL + "/you-can-use-english-welsh-courts")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("connections", List("", "", "", "", "", "", "", ""))
        .check(CsrfCheck.save)
        .check(substring("Enter your name")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val EnterYourName =

    group("NFD01CitApp_110_EnterYourName") {
      exec(http("Enter your name")
        .post(BaseURL + "/enter-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1FirstNames", Common.randomString(5))
        .formParam("applicant1MiddleNames", "")
        .formParam("applicant1LastNames", Common.randomString(5))
        .check(CsrfCheck.save)
        .check(regex("Enter your wife’s name")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val EnterYourNames =

    group("NFD01CitApp_115_${userType}EnterYourNames") {
      exec(http("Enter your names")
        .post(BaseURL + "/${userTypeURL}enter-your-names")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}FirstNames", Common.randomString(5))
        .formParam("${userType}MiddleNames", "")
        .formParam("${userType}LastNames", Common.randomString(5))
        .check(CsrfCheck.save)
        .check(regex("Your names on your marriage certificate|Changes to your name")))
    }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val EnterTheirName =

    group("NFD01CitApp_120_EnterTheirName") {
      exec(http("Enter Their name")
        .post(BaseURL + "/enter-their-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2FirstNames", Common.randomString(5))
        .formParam("applicant2MiddleNames", "")
        .formParam("applicant2LastNames", Common.randomString(5))
        .check(CsrfCheck.save)
        .check(substring("Your names on your marriage certificate")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val MarriageCertNames =

    group("NFD01CitApp_130_NamesOnMarriageCertificate") {
      exec(http("Names on your marriage certificate")
        .post(BaseURL + "/your-names-on-certificate")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1FullNameOnCertificate", Common.randomString(5) + " " + Common.randomString(5))
        .formParam("applicant2FullNameOnCertificate", Common.randomString(5) + " " + Common.randomString(5))
        .check(CsrfCheck.save)
        .check(substring("Changes to your name")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val YourContactDetails =

    group("NFD01CitApp_140_${userType}ChangesToYourName") {
      exec(http("Changes to your name")
        .post(BaseURL + "/${userTypeURL}changes-to-your-name")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}LastNameChangedWhenRelationshipFormed", Case.YesOrNo.No)
        .formParam("${userType}NameChangedSinceRelationshipFormed", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("How the court will contact you")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_150_${userType}HowCourtWillContactYou") {
      exec(http("How the court will contact you")
        .post(BaseURL + "/${userTypeURL}how-the-court-will-contact-you")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("${userType}AgreeToReceiveEmails", List("", Case.Checkbox.Checked))
        .formParam("${userType}PhoneNumber", "")
        .check(CsrfCheck.save)
        .check(substring("What language do you want to receive emails and documents in?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_160_${userType}LanguageToReceiveDocs") {
      exec(http("English or Welsh?")
        .post(BaseURL + "/${userTypeURL}english-or-welsh")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}EnglishOrWelsh", "english")
        .check(CsrfCheck.save)
        .check(substring("Do you need your contact details kept private from your")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_170_${userType}DetailsKeptPrivate") {
      exec(http("Keep contact details private from your wife?")
        .post(BaseURL + "/${userTypeURL}address-private")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}AddressPrivate", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Enter your postal address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_180_${userType}EnterYourPostcode") {
      feed(postcodeFeeder)
        .exec(http("Enter your postcode")
          .post(BaseURL + "/postcode-lookup")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("postcode", "${postcode}")
          .check(regex(""""fullAddress":"(?:.+?)","street1":"(.*?)","street2":"(.*?)","town":"(.*?)","county":"*(.*?)"*,"postcode":"(.+?)"""")
            .ofType[(String, String, String, String, String)].findRandom.saveAs("addressLines")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_190_${userType}EnterYourAddress") {
      exec(http("Enter your postal address")
        .post(BaseURL + "/${userTypeURL}enter-your-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}Address1", "${addressLines(0)}")
        .formParam("${userType}Address2", "${addressLines(1)}")
        .formParam("${userType}Address3", "")
        .formParam("${userType}AddressTown", "${addressLines(2)}")
        .formParam("${userType}AddressCounty", "${addressLines(3)}")
        .formParam("${userType}AddressPostcode", "${addressLines(4)}")
        .formParam("${userType}AddressCountry", "UK")
        .check(CsrfCheck.save)
        .check(regex("Other court cases|Does your wife have a solicitor")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val TheirContactDetails =

    group("NFD01CitApp_195_DoTheyHaveASolicitor") {
      exec(http("Do they have a solicitor")
        .post(BaseURL + "/do-they-have-a-solicitor")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1IsApplicant2Represented", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(regex("Enter your wife(&.+;)s email address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_200_EnterTheirEmailAddress") {
      exec(http("Enter your wife's email address")
        .post(BaseURL + "/their-email-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2EmailAddress", "${Applicant2EmailAddress}")
        .formParam("applicant1DoesNotKnowApplicant2EmailAddress", "")
        .check(CsrfCheck.save)
        .check(regex("Do you have your wife(&.+;)s postal address?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_210_DoYouKnowTheirAddress") {
      exec(http("Do you have your wife's postal address?")
        .post(BaseURL + "/do-you-have-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1KnowsApplicant2Address", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Enter your wife’s postal address")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_220_EnterTheirPostcode") {
      feed(postcodeFeeder)
        .exec(http("Enter their postcode")
          .post(BaseURL + "/postcode-lookup")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("postcode", "${postcode}")
          .check(regex(""""fullAddress":"(?:.+?)","street1":"(.*?)","street2":"(.*?)","town":"(.*?)","county":"*(.*?)"*,"postcode":"(.+?)"""")
            .ofType[(String, String, String, String, String)].findRandom.saveAs("addressLines")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_230_TheirAddress") {
      exec(http("Enter your wife’s postal address")
        .post(BaseURL + "/enter-their-address")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2Address1", "${addressLines(0)}")
        .formParam("applicant2Address2", "${addressLines(1)}")
        .formParam("applicant2Address3", "")
        .formParam("applicant2AddressTown", "${addressLines(2)}")
        .formParam("applicant2AddressCounty", "${addressLines(3)}")
        .formParam("applicant2AddressPostcode", "${addressLines(4)}")
        .formParam("applicant2AddressCountry", "UK")
        .check(CsrfCheck.save)
        .check(substring("Other court cases")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val DivorceDetails =

    group("NFD01CitApp_240_${userType}OtherCourtCases") {
      exec(http("Other court cases")
        .post(BaseURL + "/${userTypeURL}other-court-cases")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}LegalProceedings", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("Dividing your money and property")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_250_${userType}DividingMoneyAndProperty") {
      exec(http("Dividing money and property")
        .post(BaseURL + "/${userTypeURL}dividing-money-property")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Do you want to apply for a financial order?")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_260_${userType}ApplyForFinancialOrder") {
      exec(http("Do you want to apply for a financial order?")
        .post(BaseURL + "/${userTypeURL}do-you-want-to-apply-financial-order")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("${userType}WhoIsFinancialOrderFor", List("", ""))
        .formParam("${userType}ApplyForFinancialOrder", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(regex("Upload your documents|Check your husband&#39;s answers")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val DocumentUpload =

    group("NFD01CitApp_270_DocumentUpload") {
      exec(http("Upload your documents")
        .post(BaseURL + "/document-manager?_csrf=${csrf}")
        .header("accept", "application/json")
        .header("accept-encoding", "gzip, deflate, br")
        .header("accept-language", "en-GB,en;q=0.9")
        .header("content-type", "multipart/form-data")
        .header("sec-fetch-dest", "empty")
        .header("sec-fetch-mode", "cors")
        .header("sec-fetch-site", "same-origin")
        .header("x-requested-with", "XMLHttpRequest")
        .bodyPart(RawFileBodyPart("files[]", "2MB.pdf")
          .fileName("2MB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .check(status.is(200))
        .check(regex(""""id":"(.+)","name":"2MB.pdf"""").saveAs("documentId")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_280_DocumentUploadSubmit") {
      exec(http("Upload your documents")
        .post(BaseURL + "/upload-your-documents")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}UploadedFiles", "[{\"id\":\"" + "${documentId}" + "\",\"name\":\"2MB.pdf\"}]")
        .formParam("${userType}CannotUploadDocuments", "")
        .check(regex("Check your answers|Equality and diversity questions")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val CheckYourAnswersSole =

    group("NFD01CitApp_290_CheckYourAnswers") {
      exec(http("Check your answers")
        .post(BaseURL + "/check-your-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("${userType}IConfirmPrayer", List("", Case.Checkbox.Checked))
        .multivaluedFormParam("${userType}StatementOfTruth", List("", Case.Checkbox.Checked))
        .check(CsrfCheck.save)
        .check(substring("Pay your divorce fee")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val CheckYourAnswersJointApplicant1 =

    group("NFD01CitApp_290_CheckYourAnswersApplicant1") {
      exec(http("Check your answers")
        .post(BaseURL + "/check-your-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicationType", "jointApplication")
        .check(substring("Your answers have been sent to your wife to review")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val CheckYourAnswersJointApplicant2 =

    group("NFD01CitApp_295_CheckYourAnswersApplicant2") {
      exec(http("Check your answers")
        .post(BaseURL + "/applicant2/check-your-joint-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2Confirmation", Case.YesOrNo.Yes)
        .formParam("applicant2Explanation", "")
        .check(substring("Check your answers")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_297_LoadConfirmJointApplication") {
      exec(http("Load confirm joint application")
        .get(BaseURL + "/applicant2/confirm-your-joint-application")
        .headers(CommonHeader)
        .check(substring("Confirm your joint application")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val ConfirmYourJointApplication =

    group("NFD01CitApp_298_${userType}ConfirmJointApplication") {
      exec(http("Confirm joint application")
        .post(BaseURL + "/${userTypeURL}confirm-your-joint-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("${userType}IConfirmPrayer", List("", Case.Checkbox.Checked))
        .multivaluedFormParam("${userType}StatementOfTruth", List("", Case.Checkbox.Checked))
        .check(regex("Your husband needs to confirm your joint application|Pay and submit")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val PayAndSubmit =

    group("NFD01CitApp_300_PayYourFee") {
      exec(http("Pay your fee")
        .post(BaseURL + "/pay-your-fee")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(substring("Enter card details"))
        .check(css("input[name='csrfToken']", "value").saveAs("csrf"))
        .check(css("input[name='chargeId']", "value").saveAs("ChargeId")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_310_CheckCard") {
      exec(http("Check Card")
        .post(PaymentURL + "/check_card/${ChargeId}")
        .headers(CommonHeader)
        .headers(PostHeader)
        .header("sec-fetch-dest", "empty")
        .header("sec-fetch-mode", "cors")
        .formParam("cardNo", "4444333322221111")
        .check(jsonPath("$.accepted").is("true")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_320_CardDetailsSubmit") {
      exec(http("Card Details")
        .post(PaymentURL + "/card_details/${ChargeId}")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("chargeId", "${ChargeId}")
        .formParam("csrfToken", "${csrf}")
        .formParam("cardNo", "4444333322221111")
        .formParam("expiryMonth", Common.getMonth())
        .formParam("expiryYear", "25")
        .formParam("cardholderName", "Perf Tester" + Common.randomString(5))
        .formParam("cvc", (100 + rnd.nextInt(900)).toString())
        .formParam("addressCountry", "GB")
        .formParam("addressLine1", rnd.nextInt(1000).toString + " Perf" + Common.randomString(5) + " Road")
        .formParam("addressLine2", "")
        .formParam("addressCity", "Perf " + Common.randomString(5) + " Town")
        .formParam("addressPostcode", "PR1 1RF") //Common.getPostcode()
        .formParam("email", "nfdiv@perftest" + Common.randomString(8) + ".com")
        .check(regex("Confirm your payment"))
        .check(css("input[name='csrfToken']", "value").saveAs("csrf")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_330_ConfirmPayment") {
      exec(http("Confirm Payment")
        .post(PaymentURL + "/card_details/${ChargeId}/confirm")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("chargeId", "${ChargeId}")
        .formParam("csrfToken", "${csrf}")
        .check(regex("""Your reference number is:(?s).*?<div class="govuk-panel__body">(?s).*?<strong>([0-9-]{19})""").find.transform(str => str.replace("-", "")).saveAs("caseId"))
        .check(substring("Application submitted")))
    }

    .exec {
      session =>
        println("CASE ID: " + session("caseId").as[String])
        session
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val SaveAndSignout =

    group("NFD01CitApp_340_SaveAndSignout") {
      exec(http("Save and signout")
        .get(BaseURL + "/save-and-sign-out?lng=en")
        .headers(CommonHeader)
        .check(substring("Your application has been saved")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val Applicant2ContinueApplication =

    group("NFD01CitApp_360_App2AccessCodeSubmit") {

      exec(http("Applicant2 Submit Access Code")
        .post(BaseURL + "/applicant2/enter-your-access-code")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("caseReference", "${caseId}")
        .formParam("accessCode", "${accessCode}")
        .check(CsrfCheck.save)
        .check(substring("You need to review your joint application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD01CitApp_370_App2ContinueApp") {

      exec(http("Applicant2 Continue Application")
        .post(BaseURL + "/applicant2/you-need-to-review-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Has your marriage broken down irretrievably")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val ConfirmReceipt =

    group("NFD01CitApp_380_${userType}ConfirmReceipt") {

      exec(http("Confirm receipt")
        .post(BaseURL + "/${userTypeURL}hub-page")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}ConfirmReceipt", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("You have confirmed receipt of the divorce application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)


}
