package com.mishenka.cookingstuff.data

data class User (
        var postsCreated : Int = 0,
        var starredPosts : HashMap<String, Boolean>? = null
)