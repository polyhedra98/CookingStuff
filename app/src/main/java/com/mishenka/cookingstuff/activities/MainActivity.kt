package com.mishenka.cookingstuff.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Recipe
import com.mishenka.cookingstuff.fragments.BookmarkFragment
import com.mishenka.cookingstuff.fragments.ChatFragment
import com.mishenka.cookingstuff.fragments.HomeFragment
import com.mishenka.cookingstuff.fragments.MeFragment
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
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, BookmarkFragment.newInstance(), BOOKMARK_TAG).commit()
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
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(Utils.RECIPE_ID_KEY, recipeKey)
        startActivity(intent)
    }

    private fun updateUI(user : FirebaseUser?) {
        if (user != null) {
            mUsername = user.displayName.toString()
        }
        Log.i("NYA", "$mUsername is logged in")
    }

    interface MainActivityListener {
        fun userSignedOut()
    }

    companion object {
        public const val RC_SIGN_IN = 1
    }
}
