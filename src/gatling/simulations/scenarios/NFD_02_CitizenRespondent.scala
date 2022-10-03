package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, Common, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_02_CitizenRespondent {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val RespondentApplication =

    group("NFD02CitResp_010_RespAccessCodeSubmit") {

      exec(http("Respondent Submit Access Code")
        .post(BaseURL + "/applicant2/enter-your-access-code")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("caseReference", "${caseId}")
        .formParam("accessCode", "${accessCode}")
        .check(substring("Latest update"))
        //check that Response is the current step
        .check(regex(""" aria-current="step">(?s).*?<span class="hmcts-progress-bar__icon"></span>(?s).*?Response(?s).*?</li>""")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD02CitResp_020_RespContinueApplication") {

      exec(http("Respondent Continue Application")
        .get(BaseURL + "/respondent/review-the-application")
        .headers(CommonHeader)
        .header("sec-fetch-site", "none")
        .check(CsrfCheck.save)
        .check(substring("Review the divorce application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD02CitResp_030_RespReviewApplication") {

      exec(http("Respondent Review Application")
        .post(BaseURL + "/respondent/review-the-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("confirmReadPetition", List("", Case.Checkbox.Checked))
        .check(CsrfCheck.save)
        .check(substring("How do you want to respond")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD02CitResp_040_RespHowDoYouWantToRespond") {

      exec(http("Respondent How Do You Want To Respond")
        .post(BaseURL + "/respondent/how-do-you-want-to-respond")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("disputeApplication", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("The legal power")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD02CitResp_050_RespJurisdiction") {

      exec(http("Respondent Jurisdiction")
        .post(BaseURL + "/respondent/legal-jurisdiction-of-the-courts")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("jurisdictionAgree", Case.YesOrNo.Yes)
        .formParam("reasonCourtsOfEnglandAndWalesHaveNoJurisdiction", "")
        .formParam("inWhichCountryIsYourLifeMainlyBased", "")
        .check(CsrfCheck.save)
        .check(substring("Other court cases")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD02CitResp_060_RespOtherCourtCases") {

      exec(http("Respondent Other Court Cases")
        .post(BaseURL + "/respondent/other-court-cases")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant2LegalProceedings", Case.YesOrNo.No)
        .check(CsrfCheck.save)
        .check(substring("How the court will contact you")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD02CitResp_070_RespHowCourtWillContactYou") {
      exec(http("How the court will contact you")
        .post(BaseURL + "/respondent/how-the-court-will-contact-you")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("applicant2AgreeToReceiveEmails", List("", Case.Checkbox.Checked))
        .formParam("applicant2PhoneNumber", "")
        .check(CsrfCheck.save)
        .check(substring("What language do you want to receive emails and documents in?")))
    }

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .group("NFD02CitResp_080_RespLanguageToReceiveDocs") {
        exec(http("English or Welsh?")
          .post(BaseURL + "/respondent/english-or-welsh")
          .headers(CommonHeader)
          .headers(PostHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("applicant2EnglishOrWelsh", "english")
          .check(CsrfCheck.save)
        .check(substring("Check your answers")))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD02CitResp_090_RespCheckYourAnswers") {
      exec(http("Check your answers")
        .post(BaseURL + "/respondent/check-your-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam("aosStatementOfTruth", List("", Case.Checkbox.Checked))
        .check(substring("Latest update"))
        .check(substring("You have responded"))
        //check for two completed sections
        .check(substring("progress-bar__icon--complete").count.is(2)))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
