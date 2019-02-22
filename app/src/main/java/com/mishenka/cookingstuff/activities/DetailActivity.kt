package com.mishenka.cookingstuff.activities

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.NonParcelableIngredient
import com.mishenka.cookingstuff.data.NonParcelableStep
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.views.NonInteractiveIngredientView
import com.mishenka.cookingstuff.views.NonInteractiveStepView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class DetailActivity : AppCompatActivity() {
    private var mRecipeKey : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        mRecipeKey = intent.getStringExtra(Utils.RECIPE_ID_KEY)
        val wholeRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_WHOLE_RECIPE).child(mRecipeKey!!)
        val wholeStepListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val params = LinearLayout.LayoutParams (
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                try {
                    val stepsListNullable = p0.child(Utils.WHOLE_RECIPE_STEPS_LIST_CHILD).value
                    if (stepsListNullable != null) {
                        val mapper = Klaxon()
                        val stepsDict = mapper.parseArray<NonParcelableStep?>(mapper.toJsonString(stepsListNullable))
                        stepsDict?.let { dict ->
                            val vgSteps = findViewById<ViewGroup>(R.id.detail_steps)
                            for (step in dict) {
                                step?.let { nonParcelableStep ->
                                    vgSteps.addView(NonInteractiveStepView(nonParcelableStep, this@DetailActivity), params)
                                }
                            }
                        }
                    } else {
                        val vgSteps = findViewById<LinearLayout>(R.id.detail_outer_steps)
                        vgSteps.visibility = View.GONE
                    }
                } catch (e : Exception) {
                    val vgSteps = findViewById<LinearLayout>(R.id.detail_outer_steps)
                    vgSteps.visibility = View.GONE
                    throw e
                }
                try {
                    val ingredientsListNullable = p0.child(Utils.WHOLE_RECIPE_INGREDIENTS_LIST_CHILD).value
                    if (ingredientsListNullable != null) {
                        val mapper = Klaxon()
                        val ingredientsDict = mapper.parseArray<NonParcelableIngredient?>(mapper.toJsonString(ingredientsListNullable))
                        ingredientsDict?.let { dict ->
                            val vgIngredients = findViewById<ViewGroup>(R.id.detail_ingredients)
                            for (ingredient in dict) {
                                ingredient?.let { nonParcelableIngredient ->
                                    vgIngredients.addView(NonInteractiveIngredientView(nonParcelableIngredient, this@DetailActivity), params)
                                }
                            }
                        }
                    } else {
                        val vgIngredients = findViewById<LinearLayout>(R.id.detail_outer_ingredients)
                        vgIngredients.visibility = View.GONE
                    }

                } catch (e : Exception) {
                    val vgIngredients = findViewById<LinearLayout>(R.id.detail_outer_ingredients)
                    vgIngredients.visibility = View.GONE
                    throw e
                }
            }
        }
        wholeRecipeRef.addListenerForSingleValueEvent(wholeStepListener)

        val currentRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(mRecipeKey!!)
        currentRecipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val recipeName = p0.child(Utils.CHILD_RECIPE_NAME).value
                recipeName?.let { name ->
                    val tvRecipeName = findViewById<TextView>(R.id.tv_detail_recipe_name)
                    tvRecipeName.text = name.toString()
                }

                val recipeAuthor = p0.child(Utils.CHILD_RECIPE_AUTHOR).value
                recipeAuthor?.let { author ->
                    val tvAuthorName = findViewById<TextView>(R.id.tv_detail_author)
                    val spannableStr = SpannableStringBuilder("post by $author")
                    val authorLength = author.toString().length
                    val totalLength = spannableStr.length
                    spannableStr.setSpan(StyleSpan(Typeface.BOLD_ITALIC), totalLength - authorLength, totalLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableStr.setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)), totalLength - authorLength, totalLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    tvAuthorName.text = spannableStr
                }

                val recipeDescription = p0.child(Utils.CHILD_RECIPE_DESCRIPTION).value
                val tvRecipeDescription = findViewById<TextView>(R.id.tv_detail_description)
                if (recipeDescription != null) {
                    tvRecipeDescription.text = recipeDescription.toString()
                } else {
                    tvRecipeDescription.visibility = View.GONE
                }

                val mainPicUrl = p0.child(Utils.CHILD_RECIPE_MAIN_PIC_URL).value
                val ivMainPic = findViewById<ImageView>(R.id.iv_detail_main_pic)
                if (mainPicUrl != null) {
                    Glide.with(ivMainPic.context)
                            .load(mainPicUrl)
                            .apply(RequestOptions().centerCrop())
                            .into(ivMainPic)
                } else {
                    ivMainPic.visibility = View.GONE
                }

                val currentReadCount = p0.child(Utils.CHILD_RECIPE_READ_COUNT).value as Long?
                currentReadCount?.let {
                    currentRecipeRef.child(Utils.CHILD_RECIPE_READ_COUNT).setValue(it + 1)
                }
            }
        })
    }
}
