package com.mishenka.cookingstuff.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.activities.MainActivity
import com.mishenka.cookingstuff.data.Recipe
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MeFragment.MeFragmentListener] interface
 * to handle interaction events.
 * Use the [MeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
//TODO("Implement statistics")
class MeFragment : Fragment(), MainActivity.MainActivityListener {
    private var param1: String? = null
    private var param2: String? = null
    private var mListener: HomeFragment.HomeFragmentListener? = null
    private var mRecipeAdapter: RecyclerView.Adapter<HomeFragment.RecipeViewHolder>? = null
    private lateinit var mrvRecipes: RecyclerView

    private val posts = ArrayList<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val returnView = inflater.inflate(R.layout.fragment_me, container, false)
        mrvRecipes = returnView.findViewById(R.id.rv_me_created_posts)
        mrvRecipes.adapter = mRecipeAdapter
        return returnView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeFragment.HomeFragmentListener) {
            mListener = context
        } else {
            //throw RuntimeException(context.toString() + " must implement MeFragmentListener")
        }
        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            mRecipeAdapter = object : RecyclerView.Adapter<HomeFragment.RecipeViewHolder>() {
                override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HomeFragment.RecipeViewHolder {
                    val view = LayoutInflater.from(p0.context)
                            .inflate(R.layout.item_recipe, p0, false)
                    return HomeFragment.RecipeViewHolder(view)
                }

                override fun getItemCount(): Int = posts.size

                override fun onBindViewHolder(holder: HomeFragment.RecipeViewHolder, position: Int) {
                    val model = posts[position]
                    holder.tvRecipeName.text = model.name
                    holder.tvAuthorName.text = model.author
                    holder.tvRecipeDescription.text = model.description
                    if (model.mainPicUrl != null && model.mainPicUrl != "") {
                        holder.ivMainPicture.visibility = View.VISIBLE
                        GlobalScope.launch(Dispatchers.Main) {
                            val drawable = GlobalScope.async {
                                Glide.with(this@MeFragment)
                                        .load(model.mainPicUrl)
                                        .apply(RequestOptions().centerCrop())
                                        .submit()
                                        .get()
                            }.await()
                            holder.ivMainPicture.setImageDrawable(drawable)
                        }
                    } else {
                        holder.ivMainPicture.visibility = View.GONE
                    }
                    //TODO("Calling this for each post seems far from being ok")
                    model.key?.let { safeKey ->
                        val recipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(safeKey).child(Utils.CHILD_RECIPE_READ_COUNT)
                        recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                throw p0.toException()
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                holder.tvWatchCount.text = p0.value?.toString()
                            }
                        })
                        var isStarred: Boolean? = false
                        firebaseUser.let { fbUser ->
                            val starRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_USER).child(fbUser.uid).child(Utils.CHILD_STARRED_POSTS).child(model.key!!)
                            starRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    throw p0.toException()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    isStarred = p0.value as Boolean?
                                    if (isStarred != null && isStarred!!) {
                                        holder.bStar.setImageDrawable(ContextCompat.getDrawable(MainApplication.applicationContext(), R.drawable.star_checked))
                                    } else {
                                        holder.bStar.setImageDrawable(ContextCompat.getDrawable(MainApplication.applicationContext(), R.drawable.star_unchecked))
                                    }
                                }
                            })
                        }
                        holder.bStar.setOnClickListener {
                            mListener?.onStarButtonClicked(model.key, it as ImageButton)
                        }
                        holder.upperRecipe.setOnClickListener {
                            mListener?.onRecyclerItemClicked(posts[position].key)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val auth = FirebaseAuth.getInstance()
        updateUI(auth.currentUser)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun userSignedOut() {
        updateUI(null)
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        val signInButton = view?.findViewById<Button>(R.id.b_me_sign_in)
        val outerGroup = view?.findViewById<ViewGroup>(R.id.me_outer_linear_layout)
        val rvPosts = view?.findViewById<RecyclerView>(R.id.rv_me_created_posts)
        if (firebaseUser != null) {
            signInButton?.visibility = View.GONE
            val greetingTextView = view?.findViewById<TextView>(R.id.tv_me_name)
            greetingTextView?.text = "Greetings, ${firebaseUser.displayName}"
            val ivAvatar = view?.findViewById<ImageView>(R.id.iv_me_avatar)
            if (ivAvatar != null && firebaseUser.photoUrl != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    val drawable = GlobalScope.async {
                        Glide.with(this@MeFragment)
                                .load(firebaseUser.photoUrl)
                                .apply(RequestOptions().centerCrop())
                                .submit()
                                .get()
                    }.await()
                    ivAvatar.setImageDrawable(drawable)
                }
            }
            outerGroup?.visibility = View.VISIBLE
            rvPosts?.visibility = View.VISIBLE

            val databaseRef = FirebaseDatabase.getInstance().reference
            val currentUserRef = databaseRef.child(Utils.CHILD_USER).child(firebaseUser.uid)
            currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw p0.toException()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChildren()) {
                        val iterator = p0.children.iterator()
                        while (iterator.hasNext()) {
                            val snapshot = iterator.next()
                            when (snapshot.key) {
                                Utils.CHILD_USER_TOTAL_POSTS_COUNT -> {
                                    val tvTotalPosts = view?.findViewById<TextView>(R.id.tv_me_total_posts)
                                    tvTotalPosts?.text = "Total posts: ${snapshot.value?.toString()}"
                                }
                                Utils.CHILD_USER_TOTAL_STAR_COUNT -> {
                                    val tvTotalStars = view?.findViewById<TextView>(R.id.tv_me_total_stars)
                                    tvTotalStars?.text = "Total stars: ${snapshot.value?.toString()}"
                                }
                                Utils.CHILD_USER_TOTAL_READ_COUNT -> {
                                    val tvTotalReads = view?.findViewById<TextView>(R.id.tv_me_total_views)
                                    tvTotalReads?.text = "Total views: ${snapshot.value?.toString()}"
                                }
                            }
                        }
                    }
                }
            })
            val currentUserPostsRef = databaseRef.child(Utils.CHILD_USER).child(firebaseUser.uid).child(Utils.CHILD_USER_CREATED_POSTS)
            currentUserPostsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw p0.toException()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChildren()) {
                        GlobalScope.launch {
                            GlobalScope.async {
                                val listToReturn = ArrayList<String>()
                                p0.children.forEach { singleChildPost ->
                                    singleChildPost.key?.let { safeKey ->
                                        listToReturn.add(safeKey)
                                    }
                                }
                                return@async if (listToReturn.isEmpty()) {
                                    null
                                } else {
                                    listToReturn
                                }
                            }.await()?.let { safeCreatedPosts ->
                                val recipeRef = databaseRef.child(Utils.CHILD_RECIPE)
                                recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        throw p0.toException()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.hasChildren()) {
                                            p0.children.forEach { singleRecipe ->
                                                singleRecipe.key?.let { safeKey ->
                                                    if (safeCreatedPosts.contains(safeKey)) {
                                                        val recipeToShow = Recipe()
                                                        val iterator = singleRecipe.children.iterator()
                                                        while (iterator.hasNext()) {
                                                            val currentRecipeElement = iterator.next()
                                                            when (currentRecipeElement.key) {
                                                                Utils.CHILD_RECIPE_KEY -> recipeToShow.key = currentRecipeElement.value?.toString()
                                                                Utils.CHILD_RECIPE_NAME -> recipeToShow.name = currentRecipeElement.value?.toString()
                                                                Utils.CHILD_RECIPE_AUTHOR -> recipeToShow.author = currentRecipeElement.value?.toString()
                                                                Utils.CHILD_RECIPE_AUTHOR_UID -> recipeToShow.authorUID = currentRecipeElement.value?.toString()
                                                                Utils.CHILD_RECIPE_MAIN_PIC_URL -> recipeToShow.mainPicUrl = currentRecipeElement.value?.toString()
                                                                Utils.CHILD_RECIPE_COMMENTS -> recipeToShow.commentsAllowed = currentRecipeElement.value as Boolean?
                                                                Utils.CHILD_RECIPE_DESCRIPTION -> recipeToShow.description = currentRecipeElement.value?.toString()
                                                                Utils.CHILD_RECIPE_READ_COUNT -> recipeToShow.readCount = currentRecipeElement.value as Long?
                                                                Utils.CHILD_RECIPE_STAR_COUNT -> recipeToShow.starCount = currentRecipeElement.value as Long?
                                                            }
                                                        }
                                                        posts.add(recipeToShow)
                                                        //TODO("Smartly notify data set")
                                                        mRecipeAdapter?.notifyDataSetChanged()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })

                            }
                        }
                    }
                }
            })
        } else {
            outerGroup?.visibility = View.GONE
            rvPosts?.visibility = View.GONE
            signInButton?.setOnClickListener {
                val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                )

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        MainActivity.RC_SIGN_IN)
            }
            signInButton?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val authUser = FirebaseAuth.getInstance().currentUser
                val userRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_USER).child(authUser!!.uid)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        throw p0.toException()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (!p0.exists()) {
                            userRef.setValue(com.mishenka.cookingstuff.data.User())
                        }
                    }
                })
                updateUI(authUser)
            } else {
                Log.i("NYA", response?.error?.errorCode.toString())
            }
        }
    }

    interface MeFragmentListener {
        fun onFragmentInteraction()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                /*MeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }*/
                MeFragment()
    }
}
