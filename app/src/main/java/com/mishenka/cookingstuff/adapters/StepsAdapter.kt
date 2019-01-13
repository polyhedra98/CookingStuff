package com.mishenka.cookingstuff.adapters

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
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
        bFirstStep.visibility = if (step.firstPicUri != null) View.INVISIBLE else View.VISIBLE
        val ivFirstStep = returnView.findViewById<ImageView>(R.id.iv_step_first)
        ivFirstStep.visibility = bFirstStep.visibility.xor(View.INVISIBLE)
        if (ivFirstStep.visibility == View.VISIBLE) {
            Glide.with(ivFirstStep.context)
                    .load(step.firstPicUri)
                    .into(ivFirstStep)
        }

        val bSecondStep = returnView.findViewById<Button>(R.id.b_second_step)
        bSecondStep.setOnClickListener(OnAddButtonClickListener(mStepListener, returnView, step))
        bSecondStep.visibility = if (step.secondPicUri != null) View.INVISIBLE else View.VISIBLE
        val ivSecondStep = returnView.findViewById<ImageView>(R.id.iv_step_second)
        ivSecondStep.visibility = bSecondStep.visibility.xor(View.INVISIBLE)
        if (ivSecondStep.visibility == View.VISIBLE) {
            Glide.with(ivSecondStep.context)
                    .load(step.secondPicUri)
                    .into(ivSecondStep)
        }

        val bThirdStep = returnView.findViewById<Button>(R.id.b_third_step)
        bThirdStep.setOnClickListener(OnAddButtonClickListener(mStepListener, returnView, step))
        bThirdStep.visibility = if (step.ivThirdPic != null) View.INVISIBLE else View.VISIBLE
        val ivThirdStep = returnView.findViewById<ImageView>(R.id.iv_step_third)
        ivThirdStep.visibility = bThirdStep.visibility.xor(View.INVISIBLE)
        if (ivThirdStep.visibility == View.VISIBLE) {
            Glide.with(ivThirdStep.context)
                    .load(step.ivThirdPic)
                    .into(ivThirdStep)
        }

        val etDescription = returnView.findViewById<TextView>(R.id.et_description)
        etDescription.text = step.stepDescription
        etDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                step.stepDescription = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        return returnView
    }

    class OnAddButtonClickListener(listener : StepListener?, parentView : View?, step : Step) : View.OnClickListener {
        private val mListener = listener
        private val mParentView = parentView
        private val mStep = step

        override fun onClick(v: View?) {
            mListener?.onStepPicButtonClicked(v, mParentView, mStep)
        }
    }

    interface StepListener {
        fun onStepPicButtonClicked(v : View?, pv : View?, s : Step)
    }
}