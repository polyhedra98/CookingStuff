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
import com.mishenka.cookingstuff.fragments.ChatFragment
import com.mishenka.cookingstuff.fragments.HomeFragment
import com.mishenka.cookingstuff.fragments.MeFragment
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils

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
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, HomeFragment.newInstance(), HOME_TAG).commit()
        }

        findViewById<Button>(R.id.tab_button_home).setOnClickListener {
            val currentHomeFragment = supportFragmentManager.findFragmentByTag(HOME_TAG)
            if (currentHomeFragment == null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment.newInstance(), HOME_TAG).commit()
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
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment.newInstance(), HOME_TAG).commit()
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
                            val currentStarRef = currentUserRef.child(Utils.CHILD_STARRED_POSTS).child(key)
                            currentStarRef.setValue(true)

                            val currentRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(key)
                            currentRecipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    throw p0.toException()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val currentStarCount = p0.child(Utils.CHILD_RECIPE_STAR_COUNT).value as Long?
                                    if (currentStarCount != null) {
                                        currentRecipeRef.child(Utils.CHILD_RECIPE_STAR_COUNT).setValue(currentStarCount + 1)
                                    } else {
                                        currentRecipeRef.child(Utils.CHILD_RECIPE_STAR_COUNT).setValue(1)
                                    }
                                }
                            })
                            view.setImageDrawable(ContextCompat.getDrawable(MainApplication.applicationContext(), R.drawable.star_checked))
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
