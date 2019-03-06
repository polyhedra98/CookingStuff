package com.mishenka.cookingstuff.data


data class Recipe (
    var key: String? = null,
    var name: String? = null,
    var author: String? = null,
    var authorUID: String? = null,
    var description: String? = null,
    var commentsAllowed: Boolean? = null,
    var mainPicUrl: String? = null,
    var readCount: Long? = 0,
    var starCount: Long? = 0
)
