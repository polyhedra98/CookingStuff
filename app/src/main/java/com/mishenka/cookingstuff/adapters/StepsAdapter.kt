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
    private val mStepListener : StepListener? = if (context is StepListener) context else null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val returnView = convertView ?: (context as Activity).layoutInflater.inflate(mResource, parent, false)

        val step = getItem(position)

        val bFirstStep = returnView.findViewById<Button>(R.id.b_first_step)
        bFirstStep.setOnClickListener(OnAddButtonClickListener(mStepListener, returnView, step))
        bFirstStep.visibility = if (step.firstButtonClicked) View.INVISIBLE else View.VISIBLE
        val ivFirstStep = returnView.findViewById<ImageView>(R.id.iv_step_first)
        ivFirstStep.visibility = bFirstStep.visibility.xor(View.INVISIBLE)

        val bSecondStep = returnView.findViewById<Button>(R.id.b_second_step)
        bSecondStep.setOnClickListener(OnAddButtonClickListener(mStepListener, returnView, step))
        bSecondStep.visibility = if (step.secondButtonClicked) View.INVISIBLE else View.VISIBLE
        val ivSecondStep = returnView.findViewById<ImageView>(R.id.iv_step_second)
        ivSecondStep.visibility = bSecondStep.visibility.xor(View.INVISIBLE)

        val bThirdStep = returnView.findViewById<Button>(R.id.b_third_step)
        bThirdStep.setOnClickListener(OnAddButtonClickListener(mStepListener, returnView, step))
        bThirdStep.visibility = if (step.thirdButtonClicked) View.INVISIBLE else View.VISIBLE
        val ivThirdStep = returnView.findViewById<ImageView>(R.id.iv_step_third)
        ivThirdStep.visibility = bThirdStep.visibility.xor(View.INVISIBLE)

        return returnView
    }

    class OnAddButtonClickListener(listener : StepListener?, parentView : View?, step : Step?) : View.OnClickListener {
        private val mListener = listener
        private val mParentView = parentView
        private val mStep = step

        override fun onClick(v: View?) {
            mListener?.onStepPicButtonClicked(v, mParentView, mStep)
        }
    }

    interface StepListener {
        fun onStepPicButtonClicked(v : View?, pv : View?, s : Step?)
    }
}