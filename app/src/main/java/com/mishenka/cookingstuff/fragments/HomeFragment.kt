package com.mishenka.cookingstuff.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.firebase.database.*

import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.adapters.RecipeAdapter
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

    private lateinit var mRecipeAdapter : RecipeAdapter
    //TODO("Reimplement this as a RecycleView")
    private lateinit var mlvRecipes : ListView
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
        mlvRecipes = returnView.findViewById(R.id.lv_recipes)
        mlvRecipes.adapter = mRecipeAdapter
        return  returnView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeFragmentListener) {
            listener = context
        } else {
            //throw RuntimeException(context.toString() + " must implement HomeFragmentListener")
        }
        mdbRecipesReference = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE)
        attachDatabaseListener()
        mRecipeAdapter = RecipeAdapter(context, R.layout.item_recipe, ArrayList())
    }

    override fun onDetach() {
        super.onDetach()
        detachDatabaseListener()
        mRecipeAdapter.clear()
        listener = null
    }

    private fun attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val recipe = p0.getValue(Recipe::class.java)
                    mRecipeAdapter.add(recipe)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    interface HomeFragmentListener {
        fun onFragmentInteraction()
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
