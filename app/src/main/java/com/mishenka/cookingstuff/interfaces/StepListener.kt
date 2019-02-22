package com.mishenka.cookingstuff.interfaces

import android.view.View
import com.mishenka.cookingstuff.data.Step

interface StepListener {
    fun onStepPicButtonClicked(v: View?, pv: View?, s: Step)
    fun onStepClearButtonClicked(v: View)
}