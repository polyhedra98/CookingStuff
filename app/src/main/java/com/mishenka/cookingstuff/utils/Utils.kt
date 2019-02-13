package com.mishenka.cookingstuff.utils

object Utils {
    //ids
    const val UPLOAD_SERVICE_ID = 12

    //keys
    const val RECIPE_ID_KEY = "recipe_id_key"
    const val UPLOAD_DATA_KEY = "upload_data_key"

    //tags
    const val UPLOAD_SERVICE_TAG = "upload_service_tag"

    //other
    const val IMAGE_CONTENT_TYPE = "image/jpg"

    //db
    const val DB_NAME = "cooking_database"
    const val DB_UPLOAD_DATA_TABLE = "upload_data_table"
    const val DB_UPLOAD_DATA_ID = "id"
    const val DB_UPLOAD_DATA_CREATION = "creation_timestamp"
    const val DB_UPLOAD_DATA_EXPIRATION = "expiration_timestamp"
    const val DB_UPLOAD_DATA_DATA = "data"
    const val CHILD_RECIPE = "recipes"
    const val CHILD_WHOLE_RECIPE = "whole_recipes"
    const val CHILD_USER = "users"
    const val CHILD_STARRED_POSTS = "starredPosts"
    const val CHILD_STEPS_PHOTOS = "steps_pictures"
    const val CHILD_RECIPE_READ_COUNT = "readCount"
    const val CHILD_RECIPE_STAR_COUNT = "starCount"
    const val WHOLE_RECIPE_NAME_CHILD = "name"
    const val WHOLE_RECIPE_MAIN_PIC_CHILD = "mainPicUri"
    const val WHOLE_RECIPE_INGREDIENTS_LIST_CHILD = "ingredientsList"
    const val WHOLE_RECIPE_STEPS_LIST_CHILD = "stepsList"
    const val WHOLE_RECIPE_STEP_DESCRIPTION_CHILD = "stepDescription"
    const val WHOLE_RECIPE_STEP_FIRST_URL_CHILD = "firstPicUri"
    const val WHOLE_RECIPE_STEP_SECOND_URL_CHILD = "secondPicUri"
    const val WHOLE_RECIPE_STEP_THIRD_URL_CHILD = "thirdPicUri"
}