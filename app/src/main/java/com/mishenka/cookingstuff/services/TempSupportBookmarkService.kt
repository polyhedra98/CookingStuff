package com.mishenka.cookingstuff.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mishenka.cookingstuff.data.*
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.utils.database.CookingDatabase
import com.mishenka.cookingstuff.utils.database.PersistableBookmark
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class TempSupportBookmarkService : IntentService(TempSupportBookmarkService::class.simpleName) {
    override fun onHandleIntent(intent: Intent) {
        val key = intent.getStringExtra(Utils.BOOKMARK_DATA_KEY)
        val uid = intent.getStringExtra(Utils.BOOKMARK_UID_KEY)
        val currentUserRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_USER).child(uid)
        val currentStarRef = currentUserRef.child(Utils.CHILD_STARRED_POSTS).child(key)
        currentStarRef.setValue(true)
        var stepsDeferred: Deferred<ArrayList<Step>?>? = null
        val ingredientsToSave = ArrayList<Ingredient>()

        val currentWholeRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_WHOLE_RECIPE).child(key)
        currentWholeRecipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val stepsList = p0.child(Utils.WHOLE_RECIPE_STEPS_LIST_CHILD).value
                if (stepsList != null) {
                    val mapper = Klaxon()
                    val stepsDict = mapper.parseArray<NonParcelableStep?>(mapper.toJsonString(stepsList))

                    stepsDict?.let { dict ->
                        stepsDeferred = GlobalScope.async {
                            val stepsToReturn = ArrayList<Step>()
                            for (step in stepsDict) {
                                val folder = File("${MainApplication.applicationContext().getDir(Utils.IMAGES_DIR, Context.MODE_PRIVATE)}")
                                if (!folder.exists()) {
                                    folder.mkdir()
                                }
                                step?.let { safeStep ->
                                    safeStep.picUrls?.let { safePicUrls ->
                                        for (url in safePicUrls) {
                                            try {
                                                val reservedSafeUrl = url.replace(Utils.RESERVED_CHARS.toRegex(), "_")
                                                val file = File("${folder.absolutePath}/${reservedSafeUrl.substring(0, reservedSafeUrl.indexOf("_alt_media_token_"))}.jpg")
                                                if (!file.exists()) {
                                                    file.createNewFile()
                                                    val requestOptions = RequestOptions()
                                                            .skipMemoryCache(true)
                                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    //TODO("This one can be done more async, but performance boost is quite questionable)
                                                    val bitmap = Glide.with(this@TempSupportBookmarkService)
                                                            .asBitmap()
                                                            .load(url)
                                                            .apply(requestOptions)
                                                            .submit()
                                                            .get()
                                                    val out = FileOutputStream(file)
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                                    out.flush()
                                                    out.close()
                                                }
                                                Log.i("NYA", "Step pic ${file.absolutePath} saved")
                                            } catch (e: Exception) {
                                                Log.i("NYA", "Error saving step $step")
                                            }
                                        }
                                    }
                                    val stepToReturn = Step(safeStep.stepDescription)
                                    safeStep.picUrls?.let { safePicUrls ->
                                        when {
                                            safePicUrls.size == 1 -> {
                                                val fReservedSafe = safePicUrls[0].replace(Utils.RESERVED_CHARS.toRegex(), "_")
                                                stepToReturn.firstPicUri = "${folder.absolutePath}/${fReservedSafe.substring(0, fReservedSafe.indexOf("_alt_media_token_"))}.jpg"
                                            }
                                            safePicUrls.size == 2 -> {
                                                val fReservedSafe = safePicUrls[0].replace(Utils.RESERVED_CHARS.toRegex(), "_")
                                                val sReservedSafe = safePicUrls[1].replace(Utils.RESERVED_CHARS.toRegex(), "_")
                                                stepToReturn.firstPicUri = "${folder.absolutePath}/${fReservedSafe.substring(0, fReservedSafe.indexOf("_alt_media_token_"))}.jpg"
                                                stepToReturn.secondPicUri = "${folder.absolutePath}/${sReservedSafe.substring(0, sReservedSafe.indexOf("_alt_media_token_"))}.jpg"
                                            }
                                            safePicUrls.size == 3 -> {
                                                val fReservedSafe = safePicUrls[0].replace(Utils.RESERVED_CHARS.toRegex(), "_")
                                                val sReservedSafe = safePicUrls[1].replace(Utils.RESERVED_CHARS.toRegex(), "_")
                                                val tReservedSafe = safePicUrls[2].replace(Utils.RESERVED_CHARS.toRegex(), "_")
                                                stepToReturn.firstPicUri = "${folder.absolutePath}/${fReservedSafe.substring(0, fReservedSafe.indexOf("_alt_media_token_"))}.jpg"
                                                stepToReturn.secondPicUri = "${folder.absolutePath}/${sReservedSafe.substring(0, sReservedSafe.indexOf("_alt_media_token_"))}.jpg"
                                                stepToReturn.thirdPicUri = "${folder.absolutePath}/${tReservedSafe.substring(0, tReservedSafe.indexOf("_alt_media_token_"))}.jpg"
                                            }
                                        }
                                    }
                                    stepsToReturn.add(stepToReturn)
                                }
                            }
                            return@async stepsToReturn
                        }
                    }
                }

                val ingredientsList = p0.child(Utils.WHOLE_RECIPE_INGREDIENTS_LIST_CHILD).value
                if (ingredientsList != null) {
                    val mapper = Klaxon()
                    val ingredientsDict = mapper.parseArray<NonParcelableIngredient>(mapper.toJsonString(ingredientsList))
                    ingredientsDict?.let { saveIngredientsDict ->
                        for (ingredient in saveIngredientsDict) {
                            ingredientsToSave.add(Ingredient(ingredient.isSeparator, ingredient.text))
                        }
                    }

                }
            }
        })

        var nameToSave: String = ""
        var authorUIDtoSave: String = ""
        var authorToSave: String? = null
        var descriptionToSave: String? = null
        var mainPicDeferred: Deferred<String?>? = null
        var commentsAllowed = false
        val currentRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(key)
        currentRecipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(p0: DataSnapshot) {
                nameToSave = p0.child(Utils.CHILD_RECIPE_NAME).value!!.toString()
                authorUIDtoSave = p0.child(Utils.CHILD_RECIPE_AUTHOR_UID).value!!.toString()
                authorToSave = p0.child(Utils.CHILD_RECIPE_AUTHOR).value?.toString()
                descriptionToSave = p0.child(Utils.CHILD_RECIPE_DESCRIPTION).value?.toString()
                val currentCommentsAllowed = p0.child(Utils.CHILD_RECIPE_COMMENTS).value as Boolean?
                currentCommentsAllowed?.let { safeAllowed ->
                    commentsAllowed = safeAllowed
                }

                val currentMainPicUrl = p0.child(Utils.CHILD_RECIPE_MAIN_PIC_URL).value
                currentMainPicUrl?.let { safeMainPicUrl ->
                    mainPicDeferred = GlobalScope.async {
                        try {
                            val folder = File("${MainApplication.applicationContext().getDir(Utils.IMAGES_DIR, Context.MODE_PRIVATE)}")
                            if (!folder.exists()) {
                                folder.mkdir()
                            }
                            val reservedSafeUrl = safeMainPicUrl.toString().replace(Utils.RESERVED_CHARS.toRegex(), "_")
                            val file = File("${folder.absolutePath}/${reservedSafeUrl.substring(0, reservedSafeUrl.indexOf("_alt_media_token_"))}.jpg")
                            if (!file.exists()) {
                                file.createNewFile()
                                val requestOptions = RequestOptions()
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                val bitmap = Glide.with(this@TempSupportBookmarkService)
                                        .asBitmap()
                                        .load(safeMainPicUrl)
                                        .apply(requestOptions)
                                        .submit()
                                        .get()
                                val out = FileOutputStream(file)
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                out.flush()
                                out.close()
                            }
                            Log.i("NYA", "Main pic ${file.absolutePath} saved")
                            return@async file.absolutePath
                        } catch (e: Exception) {
                            Log.i("NYA", "Error saving main pic $safeMainPicUrl")
                            val reservedSafeUrl = safeMainPicUrl.toString().replace(Utils.RESERVED_CHARS.toRegex(), "_")
                            Log.i("NYA", "Tried to save ${reservedSafeUrl.substring(0, reservedSafeUrl.indexOf("_alt_media_token_"))}.jpg")
                            e.printStackTrace()
                            return@async null
                        }
                    }
                }

                val currentStarCount = p0.child(Utils.CHILD_RECIPE_STAR_COUNT).value as Long?
                if (currentStarCount != null) {
                    currentRecipeRef.child(Utils.CHILD_RECIPE_STAR_COUNT).setValue(currentStarCount + 1)
                } else {
                    currentRecipeRef.child(Utils.CHILD_RECIPE_STAR_COUNT).setValue(1)
                }
            }
        })

        GlobalScope.launch {
            val mainPicUriToSave = mainPicDeferred?.await()
            val stepsToSave = stepsDeferred?.await()
            val bookmark = BookmarkData(key = key, name = nameToSave, authorUID = authorUIDtoSave, author = authorToSave,
                    description = descriptionToSave, mainPicUri = mainPicUriToSave, commentsAllowed = commentsAllowed,
                    ingredientsList = ingredientsToSave, stepsList = stepsToSave)
            Log.i("NYA", "Bookmark to save: $bookmark")
            val db = CookingDatabase.getInstance(MainApplication.applicationContext())
            val bookmarkParcelable = PersistableBookmark<BookmarkData>(db!!)
            bookmarkParcelable.save(key, bookmark)
        }

    }
}