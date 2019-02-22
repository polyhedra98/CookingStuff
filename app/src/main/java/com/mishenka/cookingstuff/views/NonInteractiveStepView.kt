package com.mishenka.cookingstuff.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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

        val tvStepDescription = findViewById<TextView>(R.id.tv_detail_step_desc)
        if (mStep.stepDescription != null) {
            tvStepDescription.text = mStep.stepDescription
        } else {
            tvStepDescription.visibility = View.GONE
        }

        val picUriList = ArrayList<String>()
        val picList = arrayListOf<ImageView>(
                findViewById(R.id.iv_detail_first_step),
                findViewById(R.id.iv_detail_second_step),
                findViewById(R.id.iv_detail_third_step))

        mStep.picUrls?.let { downloadedUrlsList ->
            for (url in downloadedUrlsList) {
                url?.let { singleUrl ->
                    picUriList.add(singleUrl)
                }
            }
        }
        if (picUriList.size == 0) {
            val llIvContainer = findViewById<LinearLayout>(R.id.ll_detail_image_container)
            llIvContainer.visibility = View.GONE
        } else {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 8)
            tvStepDescription.layoutParams = params
            var counter = 0
            while (counter < picUriList.size) {
                Glide.with(picList[counter].context)
                        .load(picUriList[counter])
                        .apply(RequestOptions().centerCrop())
                        .into(picList[counter])
                counter++
            }
            while (counter < picList.size) {
                picList[counter].visibility = View.GONE
                counter++
            }
        }
    }
}
