package com.mishenka.cookingstuff.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mishenka.cookingstuff.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BookmarkFragment.BookmarkFragmentListener] interface
 * to handle interaction events.
 * Use the [BookmarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BookmarkFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: BookmarkFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BookmarkFragmentListener) {
            listener = context
        } else {
            //throw RuntimeException(context.toString() + " must implement HomeFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface BookmarkFragmentListener {
        fun onFragmentInteraction()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                /*BookmarkFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }*/
                BookmarkFragment()
    }
}
