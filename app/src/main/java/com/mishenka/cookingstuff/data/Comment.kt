package com.mishenka.cookingstuff.data

data class Comment(
        var userAvatarUrl: String? = null,
        var text: String? = null,
        var likeCount: Int? = null
)