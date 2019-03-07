package com.mishenka.cookingstuff.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mishenka.cookingstuff.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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
            GlobalScope.launch(Dispatchers.Main) {
                val ivMainPic = findViewById<ImageView>(R.id.iv_upper_recipe_main)
                val drawable = GlobalScope.async {
                    Glide.with(ivMainPic.context)
                            .load(it)
                            .submit()
                            .get()
                }.await()
                Glide.with(ivMainPic.context)
                        .load(drawable)
                        .apply(RequestOptions().centerCrop())
                        .into(ivMainPic)
            }
        }
    }
}