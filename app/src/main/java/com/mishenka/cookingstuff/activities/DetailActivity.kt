package com.mishenka.cookingstuff.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.NonParcelableIngredient
import com.mishenka.cookingstuff.data.NonParcelableStep
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.views.NonInteractiveIngredientView
import com.mishenka.cookingstuff.views.NonInteractiveStepView
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
                val recipeName = p0.child(Utils.WHOLE_RECIPE_NAME_CHILD).value
                recipeName?.let {
                    val tvRecipeName = findViewById<TextView>(R.id.tv_detail_recipe_name)
                    tvRecipeName.text = it.toString()
                }

                val mainPicDownloadUrl = p0.child(Utils.WHOLE_RECIPE_MAIN_PIC_CHILD).value
                mainPicDownloadUrl?.let {
                    val ivMainPic = findViewById<ImageView>(R.id.iv_detail_main_pic)
                    Glide.with(ivMainPic.context)
                            .load(it)
                            .into(ivMainPic)
                }

                val params = LinearLayout.LayoutParams (
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                try {
                //TODO("Doesn't work with D1 null D3. Cannot convert null.")
                    val stepsListNullable = p0.child(Utils.WHOLE_RECIPE_STEPS_LIST_CHILD).value
                    stepsListNullable?.let { stepsList ->
                        val mapper = Klaxon()
                        val stepsDict = mapper.parseArray<NonParcelableStep?>(mapper.toJsonString(stepsList))
                        stepsDict?.let { dict ->
                            val vgSteps = findViewById<ViewGroup>(R.id.detail_steps)
                            for (step in dict) {
                                step?.let {
                                    vgSteps.addView(NonInteractiveStepView(step, this@DetailActivity), params)
                                }
                            }
                        }
                    }
                } catch (e : Exception) {
                    throw e
                }
                try {
                    val ingredientsListNullable = p0.child(Utils.WHOLE_RECIPE_INGREDIENTS_LIST_CHILD).value
                    ingredientsListNullable?.let { ingredientsList ->
                        val mapper = Klaxon()
                        val ingredientsDict = mapper.parseArray<NonParcelableIngredient?>(mapper.toJsonString(ingredientsList))
                        ingredientsDict?.let { dict ->
                            val vgIngredients = findViewById<ViewGroup>(R.id.detail_ingredients)
                            for (ingredient in dict) {
                                ingredient?.let {
                                    vgIngredients.addView(NonInteractiveIngredientView(ingredient, this@DetailActivity), params)
                                }
                            }
                        }
                    }

                } catch (e : Exception) {
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
                val currentReadCount = p0.child(Utils.CHILD_RECIPE_READ_COUNT).value as Long?
                currentReadCount?.let {
                    currentRecipeRef.child(Utils.CHILD_RECIPE_READ_COUNT).setValue(it + 1)
                }
            }
        })
    }
}
