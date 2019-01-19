package com.mishenka.cookingstuff.data


data class Recipe (
    var key : String? = null,
    var name : String? = null,
    var author : String? = null,
    var authorUID : String? = null,
    var readCount : Int = 0,
    var mainPicUri : String? = null
)
