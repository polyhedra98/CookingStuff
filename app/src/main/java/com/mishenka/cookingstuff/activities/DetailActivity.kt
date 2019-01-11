package com.mishenka.cookingstuff.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.utils.Utils

class DetailActivity : AppCompatActivity() {
    private var mRecipeKey : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        mRecipeKey = intent.getStringExtra(Utils.RECIPE_ID_KEY)
    }
}
