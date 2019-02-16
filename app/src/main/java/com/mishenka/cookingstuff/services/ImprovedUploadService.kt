package com.mishenka.cookingstuff.services

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.mishenka.cookingstuff.data.*
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.utils.database.CookingDatabase
import com.mishenka.cookingstuff.utils.database.PersistableParcelable
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ImprovedUploadService : JobService() {
    private lateinit var mDatabaseJob: Job
    private lateinit var mName: String
    private lateinit var mAuthorUID: String
    private var mAuthor: String? = null
    private var mMainPicUri: String? = null
    private var mIngredientsList: List<Ingredient>? = null
    private var mStepsList: List<Step>? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("NYA_serv", "Improved upload service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("NYA_serv", "Improved upload service destroyed")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val uploadDataId = intent.getStringExtra(Utils.UPLOAD_DATA_KEY)
        val db = CookingDatabase.getInstance(MainApplication.applicationContext())
        val persistableParcelable = PersistableParcelable<UploadData>(db!!)
        mDatabaseJob = GlobalScope.launch {
            val uploadData = persistableParcelable.load(uploadDataId, UploadData.CREATOR)!!
            mName = uploadData.name
            mAuthorUID = uploadData.authorUID
            mAuthor = uploadData.author
            mMainPicUri = uploadData.mainPicUri
            mIngredientsList = uploadData.ingredientsList
            mStepsList = uploadData.stepsList
        }
        return Service.START_REDELIVER_INTENT
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i("NYA_serv", "Started uploading")
        GlobalScope.launch {
            mDatabaseJob.join()
            var mainPicStorageRef: String? = null
            val mainPicJob = GlobalScope.launch {
                mMainPicUri?.let { mainPickUriString ->
                    val mainPicUri = Uri.parse(mainPickUriString)
                    val cookingSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_COOKING_PHOTOS)
                    val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
                    val photoRef = cookingSRef.child("$mAuthorUID/${mainPicUri.lastPathSegment!!}")
                    val uploadTask = photoRef.putFile(mainPicUri, metadata)
                    uploadTask.addOnSuccessListener {

                    }
                    Tasks.await(uploadTask)
                }
            }
            var stepPicsStorageRefs: List<String?>? = null
            val stepPicsJob = GlobalScope.launch {
                mStepsList?.let { nullSafeStepsList ->
                    val stepPicsLocalUris = ArrayList<String>()
                    for (step in nullSafeStepsList) {
                        step.firstPicUri?.let {
                            stepPicsLocalUris.add(it)
                        }
                        step.secondPicUri?.let {
                            stepPicsLocalUris.add(it)
                        }
                        step.thirdPicUri?.let {
                            stepPicsLocalUris.add(it)
                        }
                    }
                    val localStepPicsStorageRefs = ArrayList<String?>(stepPicsLocalUris.size)
                    val cookingSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_COOKING_PHOTOS)
                    val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
                    val uploadTasks = ArrayList<UploadTask>()
                    for ((counter, localStepUri) in stepPicsLocalUris.withIndex()) {
                        val uriRepresentation = Uri.parse(localStepUri)
                        val photoRef = cookingSRef.child("$mAuthorUID/${uriRepresentation.lastPathSegment}")
                        val uploadTask = photoRef.putFile(uriRepresentation, metadata)
                        uploadTasks.add(uploadTask)
                        uploadTask.addOnSuccessListener {
                            localStepPicsStorageRefs[counter] = photoRef.toString()
                        }
                    }
                    Tasks.await(Tasks.whenAll(uploadTasks))
                    stepPicsStorageRefs = localStepPicsStorageRefs
                }
            }
            mainPicJob.join()
            stepPicsJob.join()

            val dbRef = FirebaseDatabase.getInstance().reference
            val key = dbRef.child(Utils.CHILD_RECIPE).push().key!!
            val firebaseStepsList = ArrayList<FirebaseStep>()
            if (mStepsList != null && stepPicsStorageRefs != null) {
                val localStepPicsRefs = stepPicsStorageRefs
                var innerCounter = 0
                for (step in mStepsList!!) {
                    val firebaseStep = FirebaseStep()
                    step.stepDescription?.let { description ->
                        firebaseStep.stepDescription = description
                    }
                    val listOfRefs = ArrayList<String>()
                    step.firstPicUri?.let {
                        localStepPicsRefs!![innerCounter++]?.let { ref ->
                            listOfRefs.add(ref)
                        }
                    }
                    step.secondPicUri?.let {
                        localStepPicsRefs!![innerCounter++]?.let { ref ->
                            listOfRefs.add(ref)
                        }
                    }
                    step.thirdPicUri?.let {
                        localStepPicsRefs!![innerCounter++]?.let { ref ->
                            listOfRefs.add(ref)
                        }
                    }
                    if (!listOfRefs.isEmpty()) {
                        firebaseStep.picRefs = listOfRefs
                    }
                    firebaseStepsList.add(firebaseStep)
                }
            }
            var ingredientsList: List<Ingredient>? = null
            if (mIngredientsList != null) {
                ingredientsList = mIngredientsList!!.filter { ingredient -> !ingredient.text.isNullOrEmpty() }
            }
            dbRef.child(Utils.CHILD_RECIPE).child(key).setValue(Recipe(key = key, name = mName,
                    author = mAuthor, authorUID = mAuthorUID, mainPicRef = mainPicStorageRef))
            dbRef.child(Utils.CHILD_WHOLE_RECIPE).child(key).setValue(WholeRecipe(key = key,
                    ingredientsList = ingredientsList, stepsList = firebaseStepsList))

        }.invokeOnCompletion {
            jobFinished(params, false)
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.i("NYA_serv", "Job stopped: ${params?.jobId}")
        //Restart?
        return true
    }
}