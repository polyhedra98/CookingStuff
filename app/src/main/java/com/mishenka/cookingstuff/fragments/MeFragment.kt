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
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.activities.MainActivity

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
        val greetingTextView = view?.findViewById<TextView>(R.id.me_text_view)
        val signInButton = view?.findViewById<Button>(R.id.me_sign_in_button)
        if (firebaseUser != null) {
            signInButton?.visibility = View.INVISIBLE
            greetingTextView?.text = "Greetings, ${firebaseUser.displayName}"
            greetingTextView?.visibility = View.VISIBLE
        } else {
            greetingTextView?.visibility = View.INVISIBLE
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
                val user = FirebaseAuth.getInstance().currentUser
                updateUI(user)
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
