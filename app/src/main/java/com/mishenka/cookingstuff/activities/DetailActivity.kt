package com.mishenka.cookingstuff.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.database.*
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Step
import com.mishenka.cookingstuff.utils.Utils

class DetailActivity : AppCompatActivity() {
    private var mRecipeKey : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        mRecipeKey = intent.getStringExtra(Utils.RECIPE_ID_KEY)
        val wholeRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_WHOLE_RECIPE).child(mRecipeKey!!)
        val options = FirebaseListOptions.Builder<Step>().setQuery(wholeRecipeRef, Step::class.java).setLayout(R.layout.item_non_interactive_step).build()
        val adapter = object : FirebaseListAdapter<Step>(options) {
            override fun populateView(v: View?, model: Step?, position: Int) {
                model?.stepDescription?.let {
                    val tvStepDesc = v?.findViewById<TextView>(R.id.tv_detail_name)
                    tvStepDesc?.text = it
                }
                model?.firstPicUri?.let {
                    val ivFirstPic = v?.findViewById<ImageView>(R.id.iv_detail_first_step)
                    Glide.with(ivFirstPic?.context)
                            .load(it)
                            .into(ivFirstPic)
                }
                model?.secondPicUri?.let {
                    val ivSecondPic = v?.findViewById<ImageView>(R.id.iv_detail_second_step)
                    Glide.with(ivSecondPic?.context)
                            .load(it)
                            .into(ivSecondPic)
                }
                model?.ivThirdPic?.let {
                    val thirdPic = v?.findViewById<ImageView>(R.id.iv_detail_third_step)
                    Glide.with(thirdPic?.context)
                            .load(it)
                            .into(thirdPic)
                }
            }
        }

        val wholeStepListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val tvRecipeName = findViewById<TextView>(R.id.tv_detail_recipe_name)
                tvRecipeName.text = p0.child(Utils.WHOLE_RECIPE_NAME_CHILD).value.toString()

                val ivMainPic = findViewById<ImageView>(R.id.iv_detail_main_pic)
                Glide.with(ivMainPic.context)
                        .load(p0.child(Utils.WHOLE_RECIPE_MAIN_PIC_CHILD).value)
                        .into(ivMainPic)
            }
        }
        wholeRecipeRef.addListenerForSingleValueEvent(wholeStepListener)

        val lvSteps = findViewById<ListView>(R.id.lv_detail_steps)
        lvSteps.adapter = adapter
    }
}
