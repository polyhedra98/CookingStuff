package com.mishenka.cookingstuff.services

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.mishenka.cookingstuff.data.Ingredient
import com.mishenka.cookingstuff.data.Step

class SupportUploadService : JobService() {
    private lateinit var mName : String
    private lateinit var mAuthorUID : String
    private var mAuthor : String? = null
    private var mMainPicUri : String? = null
    private var mIngredientsList : List<Ingredient>? = null
    private var mStepsList : List<Step>? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("NYA_serv", "Support service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("NYA_serv", "Support service destroyed")
    }

    override fun onStartJob(job: JobParameters?): Boolean {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        return false
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        Log.i("NYA_serv", "Job stopped: ${job?.tag}")
        //Should this job be retried?
        return true
    }
}