package com.mishenka.cookingstuff.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Recipe
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.views.UpperRecipeView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeFragment.HomeFragmentListener] interface
 * to handle interaction events.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: HomeFragmentListener? = null

    private lateinit var mFirebaseRecipeAdapter: FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>
    private lateinit var mQuery: Query
    private lateinit var mrvRecipes: RecyclerView
    private lateinit var mContext: Context
    private lateinit var mdbRecipesReference: DatabaseReference

    private var mChildEventListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val returnView = inflater.inflate(R.layout.fragment_home, container, false)
        mrvRecipes = returnView.findViewById(R.id.rv_recipes)
        mrvRecipes.adapter = mFirebaseRecipeAdapter
        return returnView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is HomeFragmentListener) {
            listener = context
        } else {
            //throw RuntimeException(context.toString() + " must implement HomeFragmentListener")
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
        }
        mdbRecipesReference = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE)
        mQuery = if (param1 == Utils.BOOKMARK_FRAGMENT_OPTION) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val dbStarredPostsReference = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_USER).child(user.uid).child(Utils.CHILD_STARRED_POSTS)
                //TODO("Finish")

                mdbRecipesReference
            } else {
                mdbRecipesReference
            }
        } else {
            mdbRecipesReference
        }
        val options = FirebaseRecyclerOptions.Builder<Recipe>().setQuery(mQuery, Recipe::class.java).build()
        //TODO("For some reason shows prev picture if no other is provided")
        mFirebaseRecipeAdapter = object : FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecipeViewHolder {
                val view = LayoutInflater.from(p0.context)
                        .inflate(R.layout.item_recipe, p0, false)
                return RecipeViewHolder(view)/*.listen { pos, type ->
                    listener?.onRecyclerItemClicked(getItem(pos).key)
                }*/
            }

            override fun onBindViewHolder(holder: RecipeViewHolder, position: Int, model: Recipe) {
                holder.tvRecipeName.text = model.name
                holder.tvAuthorName.text = model.author
                holder.tvWatchCount.text = "${model.readCount}"
                if (model.mainPicUrl != null && model.mainPicUrl != "") {
                    Glide.with(holder.ivMainPicture.context)
                            .load(model.mainPicUrl)
                            .apply(RequestOptions().centerCrop())
                            .into(holder.ivMainPicture)
                }
                holder.upperRecipe.setOnClickListener {
                    listener?.onRecyclerItemClicked(getItem(position).key)
                }
                val user = FirebaseAuth.getInstance().currentUser
                var isStarred : Boolean? = false
                user?.let { fbUser ->
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
                    listener?.onStarButtonClicked(getItem(position).key, it as ImageButton)
                }
            }
        }
        mFirebaseRecipeAdapter
        attachDatabaseListener()
    }

    override fun onDetach() {
        super.onDetach()
        detachDatabaseListener()
        listener = null
    }

    override fun onStart() {
        super.onStart()
        mFirebaseRecipeAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseRecipeAdapter.stopListening()
    }

    /* Reimplemented recipe, no need for this method anymore. It looks cool though, so I won't delete it
    private fun <T : RecyclerView.ViewHolder> T.listen(event : (position : Int, type : Int) -> Unit) : T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
    */

    //This one was needed earlier on, kinda useless now. Might need in the future
    private fun attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }
        mdbRecipesReference.addChildEventListener(mChildEventListener!!)
    }

    private fun detachDatabaseListener() {
        mChildEventListener?.let {
            mdbRecipesReference.removeEventListener(mChildEventListener!!)
            mChildEventListener = null
        }
    }

    class RecipeViewHolder(recipeView : View) : RecyclerView.ViewHolder(recipeView) {
        val upperRecipe = recipeView.findViewById<UpperRecipeView>(R.id.ur_recipe)
        val tvRecipeName = upperRecipe.findViewById<TextView>(R.id.tv_upper_recipe_name)
        val tvAuthorName = upperRecipe.findViewById<TextView>(R.id.tv_upper_author_name)
        val ivMainPicture = upperRecipe.findViewById<ImageView>(R.id.iv_upper_recipe_main)

        val tvWatchCount = recipeView.findViewById<TextView>(R.id.tv_watch_count)
        val bStar = recipeView.findViewById<ImageButton>(R.id.b_star)
    }

    interface HomeFragmentListener {
        fun onRecyclerItemClicked(recipeKey : String?)
        fun onStarButtonClicked(recipeKey : String?, view : ImageButton)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String? = null) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        //putString(ARG_PARAM2, param2)
                    }
                }
    }
}
