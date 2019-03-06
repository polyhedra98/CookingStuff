package com.mishenka.cookingstuff.interfaces

import android.view.View
import android.widget.ImageButton

interface CommentListener {
    fun onCommentSubmitButtonClicked(v: View)
    fun onCommentLikeButtonClicked(v: View, key: String)
}