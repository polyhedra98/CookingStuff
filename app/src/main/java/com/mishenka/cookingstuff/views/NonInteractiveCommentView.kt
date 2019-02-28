package com.mishenka.cookingstuff.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Comment
import com.mishenka.cookingstuff.interfaces.CommentListener

class NonInteractiveCommentView : RelativeLayout {
    private val mCommentListener: CommentListener?
    private val mComment: Comment?

    constructor(comment: Comment, context: Context?) : super(context) {
        mCommentListener = if (context is CommentListener) context else null
        mComment = comment
    }

    constructor(comment: Comment, context: Context?, attrs: AttributeSet?) :
            super(context, attrs) {
        mCommentListener = if (context is CommentListener) context else null
        mComment = comment
    }

    constructor(comment: Comment, context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        mCommentListener = if (context is CommentListener) context else null
        mComment = comment
    }

    init {
        LayoutInflater.from(this.context).inflate(R.layout.item_non_interactive_comment, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val tvCommentText = findViewById<TextView>(R.id.non_comment_text)
        mComment?.text?.let { commentText->
            tvCommentText.text = commentText
        }

        val ivAvatar = findViewById<ImageView>(R.id.non_comment_avatar)
        mComment?.userAvatarUrl?.let { url ->
            Glide.with(ivAvatar.context)
                    .load(url)
                    .apply(RequestOptions().centerCrop())
                    .into(ivAvatar)
        }

        val bLike = findViewById<ImageButton>(R.id.non_comment_like)
        bLike.setOnClickListener {
            mCommentListener?.onCommentLikeButtonClicked(this)
        }

        val tvLikeCount = findViewById<TextView>(R.id.non_comment_like_count)
        mComment?.likeCount?.let { likeCount ->
            tvLikeCount.text = likeCount.toString()
        }
    }
}