package com.mishenka.cookingstuff.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.*
import com.mishenka.cookingstuff.fragments.HomeFragment
import com.mishenka.cookingstuff.fragments.MeFragment
import com.mishenka.cookingstuff.services.TempSupportBookmarkService
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.utils.database.CookingDatabase
import com.mishenka.cookingstuff.utils.database.PersistableBookmark
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity(), HomeFragment.HomeFragmentListener {
    private lateinit var mAuth : FirebaseAuth
    private var mUsername = "anonymous"

    private val HOME_TAG = "HOME_TAG"
    private val BOOKMARK_TAG = "BOOKMARK_TAG"
    private val ME_TAG = "ME_TAG"

    //TODO("Fix recycler sloppiness")
    //TODO("Load and render every image async")
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

    override fun onRecyclerItemClicked(recipeKey: String?, isBookmarked: Boolean?) {
        recipeKey?.let {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(Utils.RECIPE_ID_KEY, it)
            intent.putExtra(Utils.IS_BOOKMARKED_KEY, isBookmarked)
            startActivity(intent)
        }
    }

    //TODO("Finish bookmarks saving and deleting")
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
                            //TODO("Saving is still buggy..")
                            trySchedulingBookmarkJob(key, user.uid)
                            val recipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(key)
                            recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    throw p0.toException()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val authorUID = p0.child(Utils.CHILD_RECIPE_AUTHOR_UID).value?.toString()
                                    authorUID?.let { safeUID ->
                                        val authorRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_USER).child(safeUID)
                                        authorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {
                                                throw p0.toException()
                                            }

                                            override fun onDataChange(p0: DataSnapshot) {
                                                p0.child(Utils.CHILD_USER_TOTAL_STAR_COUNT).value?.let { safeStarValue ->
                                                    authorRef.child(Utils.CHILD_USER_TOTAL_STAR_COUNT).setValue(safeStarValue as Long + 1)
                                                }
                                            }
                                        })
                                    }
                                }
                            })
                            view.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.star_checked))
                        } else {
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
                            deleteBookmarkData(key)
                            val recipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(key)
                            recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    throw p0.toException()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val authorUID = p0.child(Utils.CHILD_RECIPE_AUTHOR_UID).value?.toString()
                                    authorUID?.let { safeUID ->
                                        val authorRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_USER).child(safeUID)
                                        authorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {
                                                throw p0.toException()
                                            }

                                            override fun onDataChange(p0: DataSnapshot) {
                                                p0.child(Utils.CHILD_USER_TOTAL_STAR_COUNT).value?.let { safeStarValue ->
                                                    authorRef.child(Utils.CHILD_USER_TOTAL_STAR_COUNT).setValue(safeStarValue as Long - 1)
                                                }
                                            }
                                        })
                                    }
                                }
                            })
                            view.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.star_unchecked))
                        }
                    }
                })
            }
        }
    }

    private fun trySchedulingBookmarkJob(key: String, uid: String) {
        //TODO("Add non-support version")
        supportScheduleBookmarkJob(key, uid)
    }

    private fun supportScheduleBookmarkJob(key: String, uid: String) {
        val intent = Intent(this, TempSupportBookmarkService::class.java)
        intent.putExtra(Utils.BOOKMARK_DATA_KEY, key)
        intent.putExtra(Utils.BOOKMARK_UID_KEY, uid)
        startService(intent)
    }

    private fun deleteBookmarkData(key: String) {
        GlobalScope.launch {
            val db = CookingDatabase.getInstance(MainApplication.applicationContext())
            val persistableBookmark = PersistableBookmark<BookmarkData>(db!!)
            val deletedBookmark = GlobalScope.async {
                persistableBookmark.deleteBookmark(key, BookmarkData.CREATOR)
            }.await()
            Log.i("NYA", "Deleted bookmark: $deletedBookmark")
            deletedBookmark?.mainPicUri?.let { safeMainPicUri ->
                val file = File(safeMainPicUri)
                if (file.exists()) {
                    file.delete()
                }
            }
            deletedBookmark?.stepsList?.let { safeStepsList ->
                for (step in safeStepsList) {
                    step.firstPicUri?.let { safeUri ->
                        val file = File(safeUri)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                    step.secondPicUri?.let { safeUri ->
                        val file = File(safeUri)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                    step.thirdPicUri?.let { safeUri ->
                        val file = File(safeUri)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
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