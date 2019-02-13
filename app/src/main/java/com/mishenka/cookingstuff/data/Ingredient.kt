package com.mishenka.cookingstuff.data

import android.os.Parcel
import android.os.Parcelable
import com.beust.klaxon.Json

data class Ingredient (
        @Json(name = "separator")
        var isSeparator : Boolean? = null,
        var text : String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readString()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        if (isSeparator != null) {
            dest?.writeByte((if (isSeparator!!) 1 else 0).toByte())
        } else {
            dest?.writeByte(0.toByte())
        }
        dest?.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ingredient> {
        override fun createFromParcel(source: Parcel): Ingredient {
            return Ingredient(source)
        }

        override fun newArray(size: Int): Array<Ingredient?> {
            return arrayOfNulls(size)
        }
    }
}