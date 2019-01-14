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

        val picUriList = ArrayList<String>()
        val picList = arrayListOf<ImageView>(
                returnView.findViewById(R.id.iv_detail_first_step),
                returnView.findViewById(R.id.iv_detail_second_step),
                returnView.findViewById(R.id.iv_detail_third_step))

        step.firstPicUri?.let {
            /*val ivFirstPic = returnView.findViewById<ImageView>(R.id.iv_detail_first_step)
            Glide.with(ivFirstPic.context)
                    .load(it)
                    .into(ivFirstPic)*/
            picUriList.add(it)
        }

        step.secondPicUri?.let {
            picUriList.add(it)
        }

        step.thirdPicUri?.let {
            picUriList.add(it)
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

        return returnView
    }
}