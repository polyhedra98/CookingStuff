package com.mishenka.cookingstuff.data

data class NonParcelableStep(
        var stepDescription : String? = null,
        var firstPicUri : String? = null,
        var secondPicUri : String? = null,
        var thirdPicUri : String? = null
)