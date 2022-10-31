package requests.Annotations

import utils.Common._
import utils.Environment._
import utils.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._


object BookmarkService {

  val BookmarkCreateBookmark =

    group("Annotations_CreateBookmark") {
      exec(_.set("xBookmarkPosition", getRandomNumberDoubleBetweenValues(1, 100)).set("yBookmarkPosition", getRandomNumberDoubleBetweenValues(1, 100))
        .set("pageNumber", getRandomNumberIntBetweenValues(1, 5)))
        .exec(http("POST_Bookmark")
          .post(annoAPIURL + "/api/bookmarks")
          .headers(annoCreateBookmarkHeader)
          .body(ElFileBody("bodies/ANNO_CreateBookmark.json")).asJson
          .check(status is 201)
          .check(regex("#{documentId}")))
    }


}
