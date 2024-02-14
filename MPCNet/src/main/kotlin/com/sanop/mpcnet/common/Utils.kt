package com.sanop.mpcnet.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Utils {
    fun Map<String, String>.toJsonString(): String {
        return Json.encodeToString(this)
    }

    fun String.jsonToMap(): Map<String, String> {
        return Json.decodeFromString(this)
    }
}