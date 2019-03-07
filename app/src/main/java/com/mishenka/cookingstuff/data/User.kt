package com.mishenka.cookingstuff.data

data class User (
        var createdPosts: HashMap<String, Boolean>? = null,
        var starredPosts: HashMap<String, Boolean>? = null,
        var totalReadCount: Long? = 0,
        var totalStarCount: Long? = 0,
        var totalPostsCount: Long? = 0
)