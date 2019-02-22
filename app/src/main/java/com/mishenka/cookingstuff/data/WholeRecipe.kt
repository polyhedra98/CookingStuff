package com.mishenka.cookingstuff.data

data class WholeRecipe (
        var key: String? = null,
        var ingredientsList: List<Ingredient>? = null,
        var stepsList: List<FirebaseStep>? = null
)
