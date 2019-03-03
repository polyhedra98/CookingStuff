package com.mishenka.cookingstuff.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.*
import com.mishenka.cookingstuff.fragments.ChatFragment
import com.mishenka.cookingstuff.fragments.HomeFragment
import com.mishenka.cookingstuff.fragments.MeFragment
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.utils.database.Bookmark
import com.mishenka.cookingstuff.utils.database.CookingDatabase
import com.mishenka.cookingstuff.utils.database.PersistableBookmark
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), HomeFragment.HomeFragmentListener {
    private lateinit var mAuth : FirebaseAuth
    private var mUsername = "anonymous"

    private val HOME_TAG = "HOME_TAG"
    private val BOOKMARK_TAG = "BOOKMARK_TAG"
    private val CHAT_TAG = "CHAT_TAG"
    private val ME_TAG = "ME_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        val instantiatedHomeFragment = supportFragmentManager.findFragmentByTag(HOME_TAG)
        if (instantiatedHomeFragment == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, HomeFragment.newInstance(Utils.HOME_FRAGMENT_OPTION), HOME_TAG).commit()
        }

        findViewById<Button>(R.id.tab_button_home).setOnClickListener {
            val currentHomeFragment = supportFragmentManager.findFragmentByTag(HOME_TAG)
            if (currentHomeFragment == null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment.newInstance(Utils.HOME_FRAGMENT_OPTION), HOME_TAG).commit()
            }
        }

        findViewById<Button>(R.id.tab_button_bookmark).setOnClickListener {
            val currentBookmarkFragment = supportFragmentManager.findFragmentByTag(BOOKMARK_TAG)
            if (currentBookmarkFragment == null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment.newInstance(Utils.BOOKMARK_FRAGMENT_OPTION), BOOKMARK_TAG).commit()
            }
        }

        findViewById<Button>(R.id.tab_button_add_recipe).setOnClickListener {
            if (mAuth.currentUser != null) {
                val intent = Intent(this, AddRecipeActivity::class.java)
                startActivity(intent)
            } else {
                val currentHomeFragment = supportFragmentManager.findFragmentByTag(HOME_TAG)
                if (currentHomeFragment == null) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment.newInstance(Utils.HOME_FRAGMENT_OPTION), HOME_TAG).commit()
                }
            }
        }

        findViewById<Button>(R.id.tab_button_chat).setOnClickListener {
            val currentChatFragment = supportFragmentManager.findFragmentByTag(CHAT_TAG)
            if (currentChatFragment == null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ChatFragment.newInstance(), CHAT_TAG).commit()
            }
        }

        findViewById<Button>(R.id.tab_button_me).setOnClickListener {
            val currentMeFragment = supportFragmentManager.findFragmentByTag(ME_TAG)
            if (currentMeFragment == null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MeFragment.newInstance(), ME_TAG).commit()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val user = mAuth.currentUser
        updateUI(user)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            //TODO("Get rid of Sign Out for non-Signed In users")
            R.id.action_sign_out -> {
                if (mAuth.currentUser != null) {
                    AuthUI.getInstance().signOut(this).addOnCompleteListener {
                        updateUI(null)
                        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                        if (currentFragment is MainActivityListener) {
                            currentFragment.userSignedOut()
                        }
                    }
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRecyclerItemClicked(recipeKey : String?) {
        recipeKey?.let {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(Utils.RECIPE_ID_KEY, it)
            startActivity(intent)
        }
    }

    //TODO("Finish bookmarks saving and deleting")
    //TODO("Skips frames while saving, even though coroutine starts immediately")
    override fun onStarButtonClicked(recipeKey: String?, view : ImageButton) {
        recipeKey?.let { key ->
            mAuth.currentUser?.let { user ->
                var alreadyStarred = false
                val currentUserRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_USER).child(user.uid)
                currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        throw p0.toException()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val currentPost = p0.child(Utils.CHILD_STARRED_POSTS).child(key).value as Boolean?
                        if (currentPost != null && currentPost) {
                            alreadyStarred = true
                        }
                        if (!alreadyStarred) {
                            //TODO("Redo saving, cut url so the same files won't save multiple times")
                            GlobalScope.launch {
                                val currentStarRef = currentUserRef.child(Utils.CHILD_STARRED_POSTS).child(key)
                                currentStarRef.setValue(true)
                                val stepsToSave = ArrayList<Step>()
                                val ingredientsToSave = ArrayList<Ingredient>()
                                var stepsToDownloadCounter = 0

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
                                                for (step in dict) {
                                                    GlobalScope.launch {
                                                        stepsToDownloadCounter++
                                                        step?.picUrls?.let { safePicUrls ->
                                                            for (url in safePicUrls) {
                                                                val requestOptions = RequestOptions().override(100)
                                                                        .skipMemoryCache(true)
                                                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                val bitmap = Glide.with(MainApplication.applicationContext())
                                                                        .asBitmap()
                                                                        .load(url)
                                                                        .apply(requestOptions)
                                                                        .submit()
                                                                        .get()
                                                                try {
                                                                    val mFolder = File("${MainApplication.applicationContext().getDir(Utils.IMAGES_DIR, Context.MODE_PRIVATE)}")
                                                                    if (!mFolder.exists()) {
                                                                        mFolder.mkdir()
                                                                    }
                                                                    val imgFile = File(mFolder.absolutePath + "/${url.replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg")
                                                                    if (!imgFile.exists()) {
                                                                        imgFile.createNewFile()
                                                                    }
                                                                    val out = FileOutputStream(imgFile)
                                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                                                    out.flush()
                                                                    out.close()
                                                                    Log.i("NYA", "$step saved")
                                                                } catch (e: Exception) {
                                                                    Log.i("NYA", "Error saving $step")
                                                                    Log.i("NYA", "Tried to save ${url.replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg")
                                                                    e.printStackTrace()
                                                                    throw e
                                                                }
                                                            }
                                                        }
                                                    }.invokeOnCompletion {
                                                        step?.let { safeStep ->
                                                            val stepToSave = Step(safeStep.stepDescription)
                                                            safeStep.picUrls?.let { safePicUrls ->
                                                                when {
                                                                    safePicUrls.size == 1 -> stepToSave.firstPicUri = "${safePicUrls[0].replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg"
                                                                    safePicUrls.size == 2 -> {
                                                                        stepToSave.firstPicUri = "${safePicUrls[0].replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg"
                                                                        stepToSave.secondPicUri = "${safePicUrls[1].replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg"
                                                                    }
                                                                    safePicUrls.size == 3 -> {
                                                                        stepToSave.firstPicUri = "${safePicUrls[0].replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg"
                                                                        stepToSave.secondPicUri = "${safePicUrls[1].replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg"
                                                                        stepToSave.thirdPicUri = "${safePicUrls[2].replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg"
                                                                    }
                                                                }
                                                            }
                                                            stepsToSave.add(stepToSave)
                                                        }
                                                        stepsToDownloadCounter--
                                                    }
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
                                var mainPicUriToSave: String? = null
                                var commentsAllowed = false
                                var mainPicToDownloadCounter = 0
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
                                            GlobalScope.launch {
                                                mainPicToDownloadCounter++
                                                val requestOptions = RequestOptions().override(100)
                                                        .skipMemoryCache(true)
                                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                val bitmap = Glide.with(MainApplication.applicationContext())
                                                        .asBitmap()
                                                        .load(safeMainPicUrl)
                                                        .apply(requestOptions)
                                                        .submit()
                                                        .get()
                                                try {
                                                    val mFolder = File("${MainApplication.applicationContext().getDir(Utils.IMAGES_DIR, Context.MODE_PRIVATE)}")
                                                    if (!mFolder.exists()) {
                                                        mFolder.mkdir()
                                                    }
                                                    val imgFile = File(mFolder.absolutePath + "/${safeMainPicUrl.toString().replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg")
                                                    if (!imgFile.exists()) {
                                                        imgFile.createNewFile()
                                                    }
                                                    val out = FileOutputStream(imgFile)
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                                    out.flush()
                                                    out.close()
                                                    Log.i("NYA", "Main pic $safeMainPicUrl saved")
                                                } catch (e: Exception) {
                                                    Log.i("NYA", "Error saving main pic $safeMainPicUrl")
                                                    Log.i("NYA", "Tried to save ${safeMainPicUrl.toString().replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg")
                                                    e.printStackTrace()
                                                    throw e
                                                }
                                            }.invokeOnCompletion {
                                                mainPicUriToSave = "${safeMainPicUrl.toString().replace(Utils.RESERVED_CHARS.toRegex(), "_")}.jpg"
                                                mainPicToDownloadCounter--
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
                                    while(stepsToDownloadCounter != 0 && mainPicToDownloadCounter != 0) {}
                                    val bookmark = BookmarkData(key = key, name = nameToSave, authorUID = authorUIDtoSave, author = authorToSave,
                                            description = descriptionToSave, mainPicUri = mainPicUriToSave, commentsAllowed = commentsAllowed,
                                            ingredientsList = ingredientsToSave, stepsList = stepsToSave)
                                    val db = CookingDatabase.getInstance(MainApplication.applicationContext())
                                    val bookmarkParcelable = PersistableBookmark<BookmarkData>(db!!)
                                    bookmarkParcelable.save(key, bookmark)
                                }
                            }
                            view.setImageDrawable(ContextCompat.getDrawable(MainApplication.applicationContext(), R.drawable.star_checked))
                        } else {
                            //TODO("Delete bookmark")
                            val currentStarRef = currentUserRef.child(Utils.CHILD_STARRED_POSTS).child(key)
                            currentStarRef.setValue(null)

                            val currentRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(key)
                            currentRecipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    throw p0.toException()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val currentStarCount = p0.child(Utils.CHILD_RECIPE_STAR_COUNT).value as Long?
                                    currentStarCount?.let {
                                        currentRecipeRef.child(Utils.CHILD_RECIPE_STAR_COUNT).setValue(it - 1)
                                    }
                                }
                            })
                            view.setImageDrawable(ContextCompat.getDrawable(MainApplication.applicationContext(), R.drawable.star_unchecked))
                        }
                    }
                })
            }
        }
    }

    private fun updateUI(user : FirebaseUser?) {

    }

    interface MainActivityListener {
        fun userSignedOut()
    }

    companion object {
        public const val RC_SIGN_IN = 1
    }
}
