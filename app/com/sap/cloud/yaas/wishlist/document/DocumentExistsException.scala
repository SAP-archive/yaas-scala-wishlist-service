package com.sap.cloud.yaas.wishlist.document

/**
 * Definition of conflicting documents exception
 */
class DocumentExistsException(val message: String) extends Exception(message)
