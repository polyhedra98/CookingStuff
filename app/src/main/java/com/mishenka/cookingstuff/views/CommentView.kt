package com.mishenka.cookingstuff.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Comment
import com.mishenka.cookingstuff.interfaces.CommentListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class CommentView : RelativeLayout {
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
        LayoutInflater.from(this.context).inflate(R.layout.item_comment, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val etCommentText = findViewById<EditText>(R.id.comment_text)
        mComment?.text?.let {
            etCommentText.setText(it, TextView.BufferType.EDITABLE)
        }

        etCommentText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                mComment?.text = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
               //("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        val ivAvatar = findViewById<ImageView>(R.id.comment_avatar)
        val avatarUrl = mComment?.userAvatarUrl
        avatarUrl?.let { url ->
            GlobalScope.launch(Dispatchers.Main) {
                val drawable = GlobalScope.async {
                    Glide.with(ivAvatar.context)
                            .load(url)
                            .submit()
                            .get()
                }.await()
                Glide.with(ivAvatar.context)
                        .load(drawable)
                        .apply(RequestOptions().centerCrop())
                        .into(ivAvatar)
            }
        }

        val bSubmit = findViewById<Button>(R.id.comment_submit)
        bSubmit.setOnClickListener {
            mCommentListener?.onCommentSubmitButtonClicked(this)
        }
        val bCancel = findViewById<Button>(R.id.comment_cancel)
        bCancel.setOnClickListener {
            etCommentText.text = null
            etCommentText.clearFocus()
        }
    }

}