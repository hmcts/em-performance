package requests.Annotations

import utils.Common._
import utils.Environment._
import utils.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._


object BookmarkService {

  /* POST request for creating a bookmark within a document.  The request requires both an S2S token and Idam token and this should be
     called prior to the request being made.  The request also requires a page number and both x and y coordiantes for the bookmark to be created
     and these are generated via functions and stored in session.  the x and y coordinates have ot be stored as double precision in the database
     A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these documents can be used to create the bookmarks*/


  val BookmarkCreateBookmark =

    group("Annotations_Bookmark") {
      exec(_.set("xBookmarkPosition", getRandomNumberDoubleBetweenValues(1, 100)).set("yBookmarkPosition", getRandomNumberDoubleBetweenValues(1, 100))
        .set("pageNumber", getRandomNumberIntBetweenValues(1, 5)))
        .exec(http("POST_Bookmark")
          .post(annoAPIURL + "/api/bookmarks")
          .headers(annoCreateBookmarkHeader)
          .body(ElFileBody("bodies/ANNO_CreateBookmark.json")).asJson
          .check(status is 201)
          .check(jsonPath("$.id").saveAs("bookmarkId")))
    }


  /* GET request for retrieving all bookmarks associated with a document.  The request requires both an S2S token and Idam token and this should be
      called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
      documents can be used to retrieve the bookmarks.  Azure metrics indicate that when a bookmark is not found, then a 401 is returned therefore a 401 is possible*/

  val BookmarkGetBookmarks =

    group("Annotations_Bookmark") {
       exec(http("GET_Bookmarks")
        .get(annoAPIURL + "/api/#{documentId}/bookmarks")
        .headers(annoCreateBookmarkHeader)
        .check(status in (404,401)))
    }


  /* DELETE request for multiple existing bookmark objects.  The request requires both an S2S token and Idam token and this should be
      called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
      documents can be used to retrieve the bookmarks.  The document delete function needs a page number and x and y coordiantes.  The
        design of the API call requires the creation of a bookmark to be made first as there are session variables created in the JSON*/

  val BookmarkDeleteMultipleBookmarks =

    group("Annotations_Bookmark") {
      exec(http("DELETE_Bookmarks")
        .delete(annoAPIURL + "/api/bookmarks_multiple")
        .body(ElFileBody("bodies/ANNO_DeleteMultipleBookmarks.json")).asJson
        .headers(annoCreateBookmarkHeader))
    }


  /* PUT request for updating existing bookmark objects.  The request requires both an S2S token and Idam token and this should be
      called prior to the request being made.  A feeder file was created with documents with prefix name "ANNO_EM_DMStore" and these
      documents can be used to update bookmarks.   */

  val BookmarkUpdateExistingBookmarks =

    group("Annotations_Bookmark") {
      exec(_.set("xBookmarkPosition", getRandomNumberDoubleBetweenValues(1, 100)).set("yBookmarkPosition", getRandomNumberDoubleBetweenValues(1, 100))
        .set("pageNumber", getRandomNumberIntBetweenValues(1, 5)))
      .exec(http("PUT_Bookmarks")
        .put(annoAPIURL + "/api/bookmarks")
        .body(ElFileBody("bodies/ANNO_UpdateExistingBookmark.json")).asJson
        .headers(annoCreateBookmarkHeader))
    }




}
