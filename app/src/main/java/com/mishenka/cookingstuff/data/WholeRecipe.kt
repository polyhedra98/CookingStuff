package com.mishenka.cookingstuff.data

data class WholeRecipe (
        var key : String? = null,
        var name : String? = null,
        var author : String? = null,
        var authorUID : String? = null,
        var mainPicUri : String? = null,
        var stepsList : List<Step>? = null
)
