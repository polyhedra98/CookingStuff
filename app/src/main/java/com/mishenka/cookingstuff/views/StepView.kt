package com.mishenka.cookingstuff.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Step
import com.mishenka.cookingstuff.interfaces.StepListener
import com.mishenka.cookingstuff.utils.listeners.OnStepAddButtonClickListener

class StepView : LinearLayout {
    private val mStep: Step
    private val mStepListener: StepListener?

    constructor(step: Step, context: Context?) : super(context) {
        mStep = step
        mStepListener = if (context is StepListener) context else null
    }
    constructor(step: Step, context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mStep = step
        mStepListener = if (context is StepListener) context else null
    }
    constructor(step: Step, context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
            defStyleAttr) {
        mStep = step
        mStepListener = if (context is StepListener) context else null
    }

    init {
        LayoutInflater.from(this.context).inflate(R.layout.item_step, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val etStepDescription = findViewById<EditText>(R.id.et_description)
        mStep.stepDescription?.let {
            etStepDescription.setText(it, TextView.BufferType.EDITABLE)
        }
        etStepDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mStep.stepDescription = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        val bFirstStep = findViewById<Button>(R.id.b_first_step)
        bFirstStep.setOnClickListener(OnStepAddButtonClickListener(mStepListener, this, mStep))
        bFirstStep.visibility = if (mStep.firstPicUri != null) View.INVISIBLE else View.VISIBLE
        val ivFirstStep = findViewById<ImageView>(R.id.iv_step_first)
        ivFirstStep.visibility = bFirstStep.visibility.xor(View.INVISIBLE)
        if (ivFirstStep.visibility == View.VISIBLE) {
            Glide.with(ivFirstStep.context)
                    .load(mStep.firstPicUri)
                    .into(ivFirstStep)
        }

        val bSecondStep = findViewById<Button>(R.id.b_second_step)
        bSecondStep.setOnClickListener(OnStepAddButtonClickListener(mStepListener, this, mStep))
        bSecondStep.visibility = if (mStep.secondPicUri != null) View.INVISIBLE else View.VISIBLE
        val ivSecondStep = findViewById<ImageView>(R.id.iv_step_second)
        ivSecondStep.visibility = bSecondStep.visibility.xor(View.INVISIBLE)
        if (ivSecondStep.visibility == View.VISIBLE) {
            Glide.with(ivSecondStep.context)
                    .load(mStep.secondPicUri)
                    .into(ivSecondStep)
        }

        val bThirdStep = findViewById<Button>(R.id.b_third_step)
        bThirdStep.setOnClickListener(OnStepAddButtonClickListener(mStepListener, this, mStep))
        bThirdStep.visibility = if (mStep.thirdPicUri != null) View.INVISIBLE else View.VISIBLE
        val ivThirdStep = findViewById<ImageView>(R.id.iv_step_third)
        ivThirdStep.visibility = bThirdStep.visibility.xor(View.INVISIBLE)
        if (ivThirdStep.visibility == View.VISIBLE) {
            Glide.with(ivThirdStep.context)
                    .load(mStep.thirdPicUri)
                    .into(ivThirdStep)
        }

        val bClear = findViewById<ImageButton>(R.id.b_step_clear)
        bClear.setOnClickListener {
            mStepListener?.onStepClearButtonClicked(this)
        }
    }
}
