package com.mishenka.cookingstuff.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Step(
        var stepDescription : String? = null,
        var firstPicUri : String? = null,
        var secondPicUri : String? = null,
        var thirdPicUri : String? = null
) : Parcelable