package com.mishenka.cookingstuff.data

import android.os.Parcel
import android.os.Parcelable

data class Step(
        var stepDescription: String? = null,
        var firstPicUri: String? = null,
        var secondPicUri: String? = null,
        var thirdPicUri: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(stepDescription)
        dest?.writeString(firstPicUri)
        dest?.writeString(secondPicUri)
        dest?.writeString(thirdPicUri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Step> {
        override fun createFromParcel(source: Parcel): Step {
            return Step(source)
        }

        override fun newArray(size: Int): Array<Step?> {
            return arrayOfNulls(size)
        }
    }
}