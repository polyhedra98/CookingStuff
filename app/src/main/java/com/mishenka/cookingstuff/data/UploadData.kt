package com.mishenka.cookingstuff.data

import android.os.Parcel
import android.os.Parcelable

data class UploadData (
        val name: String,
        val authorUID: String,
        val author: String?,
        val description: String? = null,
        val mainPicUri: String? = null,
        val commentsAllowed: Boolean? = null,
        val ingredientsList: List<Ingredient>? = null,
        val stepsList: List<Step>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            arrayListOf<Ingredient>().apply {
                parcel.readList(this, Ingredient::class.java.classLoader)
            },
            arrayListOf<Step>().apply {
                parcel.readList(this, Step::class.java.classLoader)
            }
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(authorUID)
        dest?.writeString(author)
        dest?.writeString(description)
        dest?.writeString(mainPicUri)
        if (commentsAllowed != null) {
            dest?.writeByte((if (commentsAllowed) 1 else 0).toByte())
        } else {
            dest?.writeByte(0.toByte())
        }
        dest?.writeList(ingredientsList)
        dest?.writeList(stepsList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UploadData> {
        override fun createFromParcel(source: Parcel): UploadData {
            return UploadData(source)
        }

        override fun newArray(size: Int): Array<UploadData?> {
            return arrayOfNulls(size)
        }
    }
}