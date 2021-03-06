package com.mishenka.cookingstuff.utils

object Utils {
    //size
    const val THUMBNAIL_SIZE = 0.05f

    //ids
    const val UPLOAD_SERVICE_ID = 12

    //keys
    const val RECIPE_ID_KEY = "recipe_id_key"
    const val IS_BOOKMARKED_KEY = "bookmarked"
    const val UPLOAD_DATA_KEY = "upload_data_key"
    const val BOOKMARK_DATA_KEY = "bookmark_data_key"
    const val BOOKMARK_UID_KEY = "bookmark_uid_key"

    //tags
    const val UPLOAD_SERVICE_TAG = "upload_service_tag"

    //other
    const val RESERVED_CHARS = "[|?*<\":>+/'%=\\-&.]"
    const val IMAGE_CONTENT_TYPE = "image/jpg"
    const val BOOKMARK_FRAGMENT_OPTION = "bookmark"
    const val HOME_FRAGMENT_OPTION = "home"
    const val IMAGES_DIR = "images"

    //db
    const val DB_NAME = "cooking_database"
    const val DB_UPLOAD_DATA_TABLE = "upload_data_table"
    const val DB_BOOKMARK_TABLE = "bookmark_table"
    const val DB_UPLOAD_DATA_ID = "id"
    const val DB_BOOKMARK_ID = "id"
    const val DB_BOOKMARK_DATA = "data"
    const val DB_UPLOAD_DATA_CREATION = "creation_timestamp"
    const val DB_UPLOAD_DATA_EXPIRATION = "expiration_timestamp"
    const val DB_UPLOAD_DATA_DATA = "data"
    const val CHILD_RECIPE = "recipes"
    const val CHILD_WHOLE_RECIPE = "whole_recipes"
    const val CHILD_USER = "users"
    const val CHILD_USER_CREATED_POSTS = "createdPosts"
    const val CHILD_USER_TOTAL_READ_COUNT = "totalReadCount"
    const val CHILD_USER_TOTAL_STAR_COUNT = "totalStarCount"
    const val CHILD_USER_TOTAL_POSTS_COUNT = "totalPostsCount"
    const val CHILD_STARRED_POSTS = "starredPosts"
    const val CHILD_USER_LIKED_COMMENTS = "likedComments"
    const val CHILD_COOKING_PHOTOS = "cooking_pictures"
    const val CHILD_RECIPE_KEY = "key"
    const val CHILD_RECIPE_NAME = "name"
    const val CHILD_RECIPE_AUTHOR = "author"
    const val CHILD_RECIPE_AUTHOR_UID = "authorUID"
    const val CHILD_RECIPE_COMMENTS = "commentsAllowed"
    const val CHILD_RECIPE_MAIN_PIC_URL = "mainPicUrl"
    const val CHILD_RECIPE_DESCRIPTION = "description"
    const val CHILD_RECIPE_READ_COUNT = "readCount"
    const val CHILD_RECIPE_STAR_COUNT = "starCount"
    const val WHOLE_RECIPE_INGREDIENTS_LIST_CHILD = "ingredientsList"
    const val WHOLE_RECIPE_STEPS_LIST_CHILD = "stepsList"
    const val WHOLE_RECIPE_COMMENTS = "comments"
    const val STEP_SNAPSHOT_DESCRIPTION = "stepDescription"
    const val STEP_SNAPSHOT_PIC_URLS = "picUrls"
    const val INGREDIENT_SNAPSHOT_TEXT = "text"
    const val INGREDIENT_SNAPSHOT_SEPARATOR = "separator"
    const val COMMENT_SNAPSHOT_TEXT = "text"
    const val COMMENT_SNAPSHOT_AUTHOR = "user"
    const val COMMENT_SNAPSHOT_AVATAR_URL = "userAvatarUrl"
    const val COMMENT_SNAPSHOT_LIKE_COUNT = "likeCount"
    const val COMMENT_SNAPSHOT_KEY = "key"
}