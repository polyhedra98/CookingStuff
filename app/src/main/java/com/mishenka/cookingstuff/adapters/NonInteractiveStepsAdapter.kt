package com.mishenka.cookingstuff.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Step

class NonInteractiveStepsAdapter(context : Context, resource : Int, objects : List<Step>)
    : ArrayAdapter<Step>(context, resource, objects) {
    private val mResource = resource

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val returnView = convertView ?: (context as Activity).layoutInflater.inflate(mResource, parent, false)
        val step = getItem(position)!!

        step.stepDescription?.let {
            val tvStepDescription = returnView.findViewById<TextView>(R.id.tv_detail_step_desc)
            tvStepDescription.text = it
        }

        step.firstPicUri?.let {
            val ivFirstPic = returnView.findViewById<ImageView>(R.id.iv_detail_first_step)
            Glide.with(ivFirstPic.context)
                    .load(it)
                    .into(ivFirstPic)
        }

        step.secondPicUri?.let {
            val ivSecondPic = returnView.findViewById<ImageView>(R.id.iv_detail_second_step)
            Glide.with(ivSecondPic.context)
                    .load(it)
                    .into(ivSecondPic)
        }

        step.thirdPicUri?.let {
            val ivThirdPic = returnView.findViewById<ImageView>(R.id.iv_detail_third_step)
            Glide.with(ivThirdPic.context)
                    .load(it)
                    .into(ivThirdPic)
        }

        return returnView
    }
}