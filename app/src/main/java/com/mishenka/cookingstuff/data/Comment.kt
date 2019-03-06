package com.mishenka.cookingstuff.data

data class Comment(
        var key: String? = null,
        var user: String? = null,
        var userAvatarUrl: String? = null,
        var text: String? = null,
        var likeCount: Long? = null
)