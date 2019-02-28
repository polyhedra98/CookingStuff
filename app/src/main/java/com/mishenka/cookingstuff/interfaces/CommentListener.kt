package com.mishenka.cookingstuff.interfaces

import android.view.View

interface CommentListener {
    fun onCommentSubmitButtonClicked(v: View)
    fun onCommentLikeButtonClicked(v: View)
}