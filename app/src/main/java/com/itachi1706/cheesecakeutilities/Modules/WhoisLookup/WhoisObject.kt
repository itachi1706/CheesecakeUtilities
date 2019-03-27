package com.itachi1706.cheesecakeutilities.Modules.WhoisLookup

import com.google.gson.JsonObject

/**
 * Created by Kenneth on 27/3/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.WhoisLookup in CheesecakeUtilities
 */
class WhoisObject {

    val msg: Int = 0 // Error code
    val isValiddomain: Boolean = false
    val isAvailable: Boolean = false
    val raw: String = "No Data"
    val domain: String = ""
    val tld: String = ""
    val subdomain: String = ""
    val whoisservers: JsonObject ?= null
}
