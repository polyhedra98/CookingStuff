package com.mishenka.cookingstuff.data


data class Recipe (
    var key: String? = null,
    var name: String? = null,
    var author: String? = null,
    var authorUID: String? = null,
    var description: String? = null,
    var commentsAllowed: Boolean? = null,
    var readCount: Int = 0,
    var mainPicUrl: String? = null
)
