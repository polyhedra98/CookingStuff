package com.mishenka.cookingstuff.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.mishenka.cookingstuff.R

class UpperRecipeView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
            defStyleAttr)

    var mRecipeName : String? = null
    var mAuthorName : String? = null
    var mMainPicUri : String? = null

    init {
        LayoutInflater.from(this.context).inflate(R.layout.item_upper_recipe, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mRecipeName?.let {
            val tvRecipeName = findViewById<TextView>(R.id.tv_upper_recipe_name)
            tvRecipeName.text = it
        }
        mAuthorName?.let {
            val tvAuthorName = findViewById<TextView>(R.id.tv_upper_author_name)
            tvAuthorName.text = it
        }
        mMainPicUri?.let {
            val ivMainPic = findViewById<ImageView>(R.id.iv_upper_recipe_main)
            Glide.with(ivMainPic.context)
                    .load(it)
                    .into(ivMainPic)
        }
    }
}