package com.mishenka.cookingstuff.views

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Ingredient
import com.mishenka.cookingstuff.data.NonParcelableIngredient

class NonInteractiveIngredientView : LinearLayout {
    private val mIngredient: NonParcelableIngredient

    constructor(ingredient: NonParcelableIngredient, context: Context?) : super(context) { mIngredient = ingredient }
    constructor(ingredient: NonParcelableIngredient, context: Context?, attrs: AttributeSet?) : super(context, attrs) { mIngredient = ingredient }
    constructor(ingredient: NonParcelableIngredient, context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
            defStyleAttr) { mIngredient = ingredient }

    init {
        LayoutInflater.from(this.context).inflate(R.layout.item_non_interactive_ingredient, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIngredient.isSeparator?.let { separator ->
            if (separator) {
                val tvIngredient = findViewById<TextView>(R.id.tv_non_int_ingredient)
                tvIngredient.visibility = View.GONE
                val tvSeparator = findViewById<TextView>(R.id.tv_non_int_separator)
                mIngredient.text?.let {
                    val spannableString = SpannableString(it)
                    spannableString.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableString.length, 0)
                    tvSeparator.text = spannableString
                }
            } else {
                val tvSeparator = findViewById<TextView>(R.id.tv_non_int_separator)
                tvSeparator.visibility = View.GONE
                val tvIngredient = findViewById<TextView>(R.id.tv_non_int_ingredient)
                mIngredient.text?.let {
                    val spannableString = SpannableString(it)
                    spannableString.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableString.length, 0)
                    tvIngredient.text = spannableString
                }
            }
        }
    }
}
