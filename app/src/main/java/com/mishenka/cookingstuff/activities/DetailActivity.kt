package com.mishenka.cookingstuff.activities

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.adapters.NonInteractiveStepsAdapter
import com.mishenka.cookingstuff.data.Step
import com.mishenka.cookingstuff.utils.Utils
import java.lang.ClassCastException

class DetailActivity : AppCompatActivity() {
    private var mRecipeKey : String? = null

    private val mStepsList = ArrayList<Step>()
    private val mContext = this

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

                val lvStepsList = findViewById<ListView>(R.id.lv_detail_steps)
                val stepListAdapter = NonInteractiveStepsAdapter(mContext, R.layout.item_non_interactive_step, mStepsList)
                lvStepsList.adapter = stepListAdapter
                try {
                    val stepsList = p0.child(Utils.WHOLE_RECIPE_STEPS_LIST_CHILD).value as List<HashMap<String, String>>
                    if (stepsList.isNotEmpty()) {
                        for (step in stepsList) {
                            val currentStep = Step()
                            if (step.containsKey(Utils.WHOLE_RECIPE_STEP_DESCRIPTION_CHILD)) {
                                currentStep.stepDescription = step[Utils.WHOLE_RECIPE_STEP_DESCRIPTION_CHILD]
                            }
                            if (step.containsKey(Utils.WHOLE_RECIPE_STEP_FIRST_URL_CHILD) && !step[Utils.WHOLE_RECIPE_STEP_FIRST_URL_CHILD]!!.contains("content")) {
                                currentStep.firstPicUri = step[Utils.WHOLE_RECIPE_STEP_FIRST_URL_CHILD]
                            }
                            if (step.containsKey(Utils.WHOLE_RECIPE_STEP_SECOND_URL_CHILD) && !step[Utils.WHOLE_RECIPE_STEP_SECOND_URL_CHILD]!!.contains("content")) {
                                currentStep.secondPicUri = step[Utils.WHOLE_RECIPE_STEP_SECOND_URL_CHILD]
                            }
                            if (step.containsKey(Utils.WHOLE_RECIPE_STEP_THIRD_URL_CHILD) && !step[Utils.WHOLE_RECIPE_STEP_THIRD_URL_CHILD]!!.contains("content")) {
                                currentStep.thirdPicUri = step[Utils.WHOLE_RECIPE_STEP_THIRD_URL_CHILD]
                            }
                            mStepsList.add(currentStep)
                        }
                        stepListAdapter.notifyDataSetChanged()
                    }
                } catch (e : ClassCastException) {
                    Log.i("NYA", e.toString())
                    throw e
                }
            }
        }
        wholeRecipeRef.addListenerForSingleValueEvent(wholeStepListener)
    }
}
