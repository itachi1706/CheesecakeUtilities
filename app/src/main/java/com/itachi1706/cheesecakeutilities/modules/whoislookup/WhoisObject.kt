package com.itachi1706.cheesecakeutilities.modules.whoislookup

import com.google.gson.JsonObject

/**
 * Created by Kenneth on 27/3/2019.
 * for com.itachi1706.cheesecakeutilities.modules.WhoisLookup in CheesecakeUtilities
 */
data class WhoisObject (val msg: Int = 0, // Error code
    val validdomain: Boolean = false, val available: Boolean = false, val raw: String = "No Data", val error: String ?= null, val domain: String = "",
    val tld: String = "", val subdomain: String = "", val whoisservers: JsonObject ?= null, val whoisserver: String = "Unknwon WHOIS Server", val subwhois: String ?= null)
