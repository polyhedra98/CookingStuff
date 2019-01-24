package com.mishenka.cookingstuff.data

import com.beust.klaxon.Json

data class Ingredient (
        @Json(name = "separator")
        var isSeparator : Boolean? = null,
        var text : String? = null
)