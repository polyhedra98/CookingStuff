package com.mishenka.cookingstuff.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import com.mishenka.cookingstuff.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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
class MeFragment : Fragment(), MainActivity.MainActivityListener {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: MeFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MeFragmentListener) {
            listener = context
        } else {
            //throw RuntimeException(context.toString() + " must implement MeFragmentListener")
        }
    }

    override fun onResume() {
        super.onResume()

        val auth = FirebaseAuth.getInstance()
        updateUI(auth.currentUser)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun userSignedOut() {
        updateUI(null)
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        val signInButton = view?.findViewById<Button>(R.id.b_me_sign_in)
        val outerGroup = view?.findViewById<ViewGroup>(R.id.me_outer_relative_layout)
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
        } else {
            outerGroup?.visibility = View.GONE
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
