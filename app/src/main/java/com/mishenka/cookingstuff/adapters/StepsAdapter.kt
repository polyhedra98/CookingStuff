package com.mishenka.cookingstuff.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Step

class StepsAdapter(context: Context, resource: Int, objects: List<Step>) : ArrayAdapter<Step>(context, resource, objects) {
    private val mResource = resource

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val returnView = convertView ?: (context as Activity).layoutInflater.inflate(mResource, parent, false)

        val step = getItem(position)

        val bFirstStep = returnView.findViewById<Button>(R.id.b_first_step)
        bFirstStep.setOnClickListener(OnAddButtonClickListener(returnView))
        bFirstStep.visibility = if (step.firstButtonClicked) View.INVISIBLE else View.VISIBLE

        val bSecondStep = returnView.findViewById<Button>(R.id.b_second_step)
        bSecondStep.setOnClickListener(OnAddButtonClickListener(returnView))
        bSecondStep.visibility = if (step.secondButtonClicked) View.INVISIBLE else View.VISIBLE

        val bThirdStep = returnView.findViewById<Button>(R.id.b_third_step)
        bThirdStep.setOnClickListener(OnAddButtonClickListener(returnView))
        bThirdStep.visibility = if (step.thirdButtonClicked) View.INVISIBLE else View.VISIBLE

        return returnView
    }

    class OnAddButtonClickListener(parentView : View?) : View.OnClickListener {
        private val mParentView = parentView

        override fun onClick(v: View?) {
            //TODO("Start camera / gallery intent")
            v?.visibility = View.INVISIBLE
            when (v?.id) {
                R.id.b_first_step -> {
                    mParentView?.findViewById<ImageView>(R.id.iv_step_first)?.visibility = View.VISIBLE
                }
                R.id.b_second_step -> {
                    mParentView?.findViewById<ImageView>(R.id.iv_step_second)?.visibility = View.VISIBLE
                }
                R.id.b_third_step -> {
                    mParentView?.findViewById<ImageView>(R.id.iv_step_third)?.visibility = View.VISIBLE
                }
            }
        }
    }
}