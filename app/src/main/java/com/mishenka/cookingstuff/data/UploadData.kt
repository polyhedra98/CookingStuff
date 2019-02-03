package com.mishenka.cookingstuff.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UploadData (
        val name : String,
        val authorUID : String,
        val author : String?,
        val mainPicUri : String? = null,
        val ingredientsList : List<Ingredient>? = null,
        val stepsList : List<Step>? = null
) : Parcelable