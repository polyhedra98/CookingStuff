package com.mishenka.cookingstuff.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.NonParcelableStep

class NonInteractiveStepView : LinearLayout {
    private val mStep: NonParcelableStep

    constructor(step : NonParcelableStep, context: Context?) : super(context) { mStep = step }
    constructor(step: NonParcelableStep, context: Context?, attrs: AttributeSet?) : super(context, attrs) { mStep = step }
    constructor(step: NonParcelableStep, context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
            defStyleAttr) { mStep = step }

    init {
        LayoutInflater.from(this.context).inflate(R.layout.item_non_interactive_step, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        mStep.stepDescription?.let {
            val tvStepDescription = findViewById<TextView>(R.id.tv_detail_step_desc)
            tvStepDescription.text = it
        }

        val picUriList = ArrayList<String>()
        val picList = arrayListOf<ImageView>(
                findViewById(R.id.iv_detail_first_step),
                findViewById(R.id.iv_detail_second_step),
                findViewById(R.id.iv_detail_third_step))

        mStep.firstPicUri?.let {
            if (!it.contains("content")) {
                picUriList.add(it)
            }
        }
        mStep.secondPicUri?.let {
            if (!it.contains("content")) {
                picUriList.add(it)
            }
        }
        mStep.thirdPicUri?.let {
            if (!it.contains("content")) {
                picUriList.add(it)
            }
        }

        var counter = -1
        while (++counter < picUriList.size) {
            Glide.with(picList[counter].context)
                    .load(picUriList[counter])
                    .into(picList[counter])
        }
        counter--
        while (++counter < picList.size) {
            picList[counter].visibility = View.GONE
        }
    }
}
