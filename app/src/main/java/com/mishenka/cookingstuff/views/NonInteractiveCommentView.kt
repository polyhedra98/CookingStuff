package com.mishenka.cookingstuff.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
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
    private val mLiked: Boolean?

    constructor(comment: Comment?, liked: Boolean?, context: Context?) : super(context) {
        mCommentListener = if (context is CommentListener) context else null
        mComment = comment
        mLiked = liked
    }

    constructor(comment: Comment?, liked: Boolean?, context: Context?, attrs: AttributeSet?) :
            super(context, attrs) {
        mCommentListener = if (context is CommentListener) context else null
        mComment = comment
        mLiked = liked
    }

    constructor(comment: Comment?, liked: Boolean?, context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        mCommentListener = if (context is CommentListener) context else null
        mComment = comment
        mLiked = liked
    }

    init {
        LayoutInflater.from(this.context).inflate(R.layout.item_non_interactive_comment, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val tvCommentAuthor = findViewById<TextView>(R.id.non_comment_author)
        mComment?.user?.let { safeUser ->
            tvCommentAuthor.text = safeUser
        }

        //TODO("Potentially add 'read more..'")
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
        if (mLiked != null && mLiked) {
            bLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_checked))
        }
        bLike.setOnClickListener {
            mComment?.key?.let { safeKey ->
                mCommentListener?.onCommentLikeButtonClicked(this, safeKey)
            }
        }

        val tvLikeCount = findViewById<TextView>(R.id.non_comment_like_count)
        mComment?.likeCount?.let { likeCount ->
            tvLikeCount.text = likeCount.toString()
        }
    }
}