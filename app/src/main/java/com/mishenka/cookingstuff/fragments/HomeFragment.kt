package com.mishenka.cookingstuff.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*

import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Recipe
import com.mishenka.cookingstuff.utils.Utils

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

    private lateinit var mFirebaseRecipeAdapter : FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>
    private lateinit var mQuery : Query
    private lateinit var mrvRecipes : RecyclerView
    private lateinit var mContext : Context
    private lateinit var mdbRecipesReference: DatabaseReference

    private var mChildEventListener : ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val returnView = inflater.inflate(R.layout.fragment_home, container, false)
        mrvRecipes = returnView.findViewById(R.id.rv_recipes)
        mrvRecipes.adapter = mFirebaseRecipeAdapter
        mrvRecipes.addOnItemTouchListener(RecyclerOnItemClickListener(mContext, mrvRecipes, object : RecyclerOnItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                listener?.onRecyclerItemClicked(mFirebaseRecipeAdapter.getItem(position).key)
            }

            override fun onItemLongClick(view: View, position: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }))
        return  returnView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is HomeFragmentListener) {
            listener = context
        } else {
            //throw RuntimeException(context.toString() + " must implement HomeFragmentListener")
        }
        mdbRecipesReference = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE)
        mQuery = mdbRecipesReference
        val options = FirebaseRecyclerOptions.Builder<Recipe>().setQuery(mQuery, Recipe::class.java).build()
        mFirebaseRecipeAdapter = object : FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecipeViewHolder {
                val view = LayoutInflater.from(p0.context)
                        .inflate(R.layout.item_recipe, p0, false)
                return RecipeViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecipeViewHolder, position: Int, model: Recipe) {
                holder.tvRecipeName.text = model.name
                holder.tvAuthorName.text = model.author
                if (model.mainPicUri != "" && model.mainPicUri != null) {
                    Glide.with(holder.ivMainPicture.context)
                            .load(model.mainPicUri)
                            .into(holder.ivMainPicture)
                }
            }
        }
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

    //This one was needed earlier on, kinda useless now. Might need in the future
    private fun attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildRemoved(p0: DataSnapshot) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private class RecipeViewHolder(recipeView : View) : RecyclerView.ViewHolder(recipeView) {
        val tvRecipeName = recipeView.findViewById<TextView>(R.id.tv_recipe_name)
        val tvAuthorName = recipeView.findViewById<TextView>(R.id.tv_author_name)
        val ivMainPicture = recipeView.findViewById<ImageView>(R.id.iv_recipe_main)
    }

    private class RecyclerOnItemClickListener(context : Context, recyclerView: RecyclerView, onItemClickListener : OnItemClickListener) : RecyclerView.OnItemTouchListener {
        private val mListener = onItemClickListener
        private val mContext = context
        private val mRecyclerView = recyclerView
        private val mGestureDetector = GestureDetector(mContext, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return super.onSingleTapUp(e)
            }

            override fun onLongPress(e: MotionEvent) {
                val child = mRecyclerView.findChildViewUnder(e.x, e.y)
                child?.let {
                    mListener.onItemLongClick(it, mRecyclerView.getChildAdapterPosition(it))
                }
            }
        })

        interface OnItemClickListener {
            fun onItemClick(view : View, position : Int)
            fun onItemLongClick(view : View, position: Int)
        }

        override fun onTouchEvent(p0: RecyclerView, p1: MotionEvent) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onInterceptTouchEvent(p0: RecyclerView, p1: MotionEvent): Boolean {
            val child = p0.findChildViewUnder(p1.x, p1.y)
            if (child != null && mGestureDetector.onTouchEvent(p1)) {
                mListener.onItemClick(child, p0.getChildAdapterPosition(child))
                return true
            }
            return false
        }

        override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    interface HomeFragmentListener {
        fun onRecyclerItemClicked(recipeKey : String?)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                /*HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }*/
                HomeFragment()
    }
}
