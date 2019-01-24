package com.mishenka.cookingstuff.utils.listeners

import android.view.View
import com.mishenka.cookingstuff.data.Step
import com.mishenka.cookingstuff.interfaces.StepListener

class OnStepAddButtonClickListener(listener : StepListener?, parentView : View?, step : Step) : View.OnClickListener {
    private val mListener = listener
    private val mParentView = parentView
    private val mStep = step

    override fun onClick(v: View?) {
        mListener?.onStepPicButtonClicked(v, mParentView, mStep)
    }
}