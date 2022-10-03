package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Case, CsrfCheck, Environment}

import scala.concurrent.duration._

object NFD_03_CitizenApplyForCO {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val ApplyForConditionalOrder =

    group("NFD03CitCO_010_${userType}StartConditionalOrder") {

      exec(http("Start Conditional Order")
        .post(BaseURL + "/${userTypeURL}hub-page")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}ApplyForConditionalOrderStarted", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(regex("Read your wife&#39;s response|Do you want to continue with your joint divorce")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val ContinueWithConditionalOrderSole =

    group("NFD03CitCO_020_ReadTheResponse") {

      exec(http("Read the response")
        .post(BaseURL + "/read-the-response")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .check(CsrfCheck.save)
        .check(substring("Do you want to continue with your divorce")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD03CitCO_030_ContinueWithYourApplication") {

      exec(http("Continue with your application")
        .post(BaseURL + "/continue-with-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1ApplyForConditionalOrder", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Review your divorce application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD03CitCO_040_ReviewYourApplication") {

      exec(http("Review your application")
        .post(BaseURL + "/review-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("applicant1ConfirmInformationStillCorrect", Case.YesOrNo.Yes)
        .formParam("applicant1ReasonInformationNotCorrect", "")
        .check(CsrfCheck.save)
        .check(substring("Check your answers")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val ContinueWithConditionalOrderJoint =

    group("NFD03CitCO_035_${userType}ContinueJointApplication") {

      exec(http("Continue joint application")
        .post(BaseURL + "/${userTypeURL}continue-with-your-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}ApplyForConditionalOrder", Case.YesOrNo.Yes)
        .check(CsrfCheck.save)
        .check(substring("Review your joint divorce application")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .group("NFD03CitCO_045_${userType}ReviewJointApplication") {

      exec(http("Review joint application")
        .post(BaseURL + "/${userTypeURL}review-your-joint-application")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("${userType}ConfirmInformationStillCorrect", Case.YesOrNo.Yes)
        .formParam("${userType}ReasonInformationNotCorrect", "")
        .check(CsrfCheck.save)
        .check(substring("Check your answers")))

    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

  val CompleteConditionalOrder =

    group("NFD03CitCO_050_${userType}CheckYourCOAnswers") {

      exec(http("Check your CO answers")
        .post(BaseURL + "/${userTypeURL}check-your-conditional-order-answers")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("_csrf", "${csrf}")
        .multivaluedFormParam(session => "co" + session("userType").as[String].replace("applicant", "Applicant") + "StatementOfTruth", List("", Case.Checkbox.Checked))
        .check(regex("You have applied for a `?conditional order`?|The court will check your application and send it to a judge")))
        // todo add check for completed sections as part of NFDIV-2334
        //.check(substring("progress-bar__icon--complete").count.in(1, 3)))
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}
