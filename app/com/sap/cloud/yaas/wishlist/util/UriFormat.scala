package com.sap.cloud.yaas.wishlist.util

import play.api.libs.json._

/**
  * Json mapper for java URI format
  */
object UriFormat {
  implicit val uriReads = Reads {
    case JsString(s) => JsSuccess(java.net.URI.create(s))
    case _ => JsError("JsString expected to convert to URI")
  }

  implicit val uriWrites = Writes {
    uri: java.net.URI => JsString(uri.toString)
  }
}
