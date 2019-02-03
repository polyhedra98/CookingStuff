package com.mishenka.cookingstuff.data

import android.os.Parcelable
import com.beust.klaxon.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredient (
        @Json(name = "separator")
        var isSeparator : Boolean? = null,
        var text : String? = null
) : Parcelable