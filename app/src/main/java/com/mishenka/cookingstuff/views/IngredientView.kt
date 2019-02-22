package com.mishenka.cookingstuff.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Ingredient
import com.mishenka.cookingstuff.interfaces.IngredientListener

class IngredientView : LinearLayout {
    private val mIngredient: Ingredient
    private val mListener: IngredientListener?

    constructor(ingredient: Ingredient, context: Context?) : super(context) {
        mIngredient = ingredient
        mListener = if (context is IngredientListener) {
            context
        } else {
            null
        }
    }
    constructor(ingredient: Ingredient, context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mIngredient = ingredient
        mListener = if (context is IngredientListener) {
            context
        } else {
            null
        }
    }
    constructor(ingredient: Ingredient, context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
            defStyleAttr) {
        mIngredient = ingredient
        mListener = if (context is IngredientListener) {
            context
        } else {
            null
        }
    }

    init {
        LayoutInflater.from(this.context).inflate(R.layout.item_ingredient, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIngredient.isSeparator?.let { separator ->
            if (separator) {
                val etName = findViewById<EditText>(R.id.et_ingredient_name)
                etName.visibility = View.GONE
                val etSeparator = findViewById<EditText>(R.id.et_ingredient_separator)
                mIngredient.text?.let {
                    etSeparator.setText(it, TextView.BufferType.EDITABLE)
                }
                etSeparator.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        mIngredient.text = s.toString()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
            } else {
                val etSeparator = findViewById<EditText>(R.id.et_ingredient_separator)
                etSeparator.visibility = View.GONE
                val etName = findViewById<EditText>(R.id.et_ingredient_name)
                mIngredient.text?.let {
                    etName.setText(it, TextView.BufferType.EDITABLE)
                }
                etName.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        mIngredient.text = s.toString()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
            }
            val bClear = findViewById<ImageButton>(R.id.b_ingredient_clear)
            bClear.setOnClickListener {
                mListener?.onIngredientClearButtonClicked(this)
            }
        }
    }
}
