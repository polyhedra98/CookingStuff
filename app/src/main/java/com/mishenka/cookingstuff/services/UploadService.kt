package com.mishenka.cookingstuff.services

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class UploadService : JobService() {
    private lateinit var mDeferred: Deferred<Unit>
    private lateinit var mName: String
    private lateinit var mAuthorUID: String
    private var mAuthor: String? = null
    private var mMainPicUri: String? = null
    private var mIngredientsList: List<Ingredient>? = null
    private var mStepsList: List<Step>? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("NYA_serv", "Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("NYA_serv", "Service destroyed")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val uploadDataId = intent.getStringExtra(Utils.UPLOAD_DATA_KEY)
        val db = CookingDatabase.getInstance(MainApplication.applicationContext())
        val persistableParcelable = PersistableParcelable<UploadData>(db!!)
        mDeferred = GlobalScope.async {
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
        GlobalScope.async {
            mDeferred.join()
            val mainPicTask = Tasks.call(sExecutor, MainPicCallable())
            val stepPicTask = Tasks.call(sExecutor, StepsCallable())
            Tasks.whenAll(mainPicTask, stepPicTask).continueWith {
                val dbRef = FirebaseDatabase.getInstance().reference
                val key = dbRef.child(Utils.CHILD_RECIPE).push().key!!
                val mainPicUri = mainPicTask.result
                val stepPicUris = stepPicTask.result

                if (mStepsList != null && stepPicUris != null) {
                    var counter = 0
                    var innerCounter = 0
                    while (counter < mStepsList!!.size) {
                        mStepsList!![counter].firstPicUri = if (mStepsList!![counter].firstPicUri != null) stepPicUris[innerCounter++] else null
                        mStepsList!![counter].secondPicUri = if (mStepsList!![counter].secondPicUri != null) stepPicUris[innerCounter++] else null
                        mStepsList!![counter].thirdPicUri = if (mStepsList!![counter].thirdPicUri != null) stepPicUris[innerCounter++] else null
                        counter++
                    }
                }
                var ingredientsList : List<Ingredient>? = null
                if (mIngredientsList != null) {
                    ingredientsList = mIngredientsList!!.filter { ingredient -> !ingredient.text.isNullOrEmpty() }
                }
                Log.i("NYA_serv", "Writing")
                Log.i("NYA_serv", "Writing key = $key")
                Log.i("NYA_serv", "Writing name = $mName")
                Log.i("NYA_serv", "Writing author = $mAuthor")
                Log.i("NYA_serv", "Writing authorUID = $mAuthorUID")
                Log.i("NYA_serv", "Writing mainPicUri = $mainPicUri")
                Log.i("NYA_serv", "Writing key = $key, name = $mName, author = $mAuthor, authorUID = $mAuthorUID, mainPicUri = $mainPicUri")
                dbRef.child(Utils.CHILD_RECIPE).child(key).setValue(Recipe(key = key, name = mName, author = mAuthor,
                        authorUID = mAuthorUID, mainPicUri = mainPicUri)).addOnFailureListener { e ->
                    Log.i("NYA_serv", "Error writing to recipes")
                    throw e
                }
                Log.i("NYA_serv", "Finished writing recipe")
                Log.i("NYA_serv", "Writing key = $key, name = $mName, author = $mAuthor, authorUID = $mAuthorUID, mainPicUri = $mainPicUri, ingredientsList = $ingredientsList, stepList = $mStepsList")
                dbRef.child(Utils.CHILD_WHOLE_RECIPE).child(key).setValue(WholeRecipe(key = key, name = mName, authorUID = mAuthorUID,
                        author = mAuthor, mainPicUri = mainPicUri, ingredientsList = ingredientsList, stepsList = mStepsList)).addOnFailureListener { e ->
                    Log.i("NYA_serv", "Error writing to whole recipes")
                    throw e
                }
                Log.i("NYA_serv", "Finished writing whole recipe")
            }.addOnCompleteListener { task ->
                CookingDatabase.destroyInstance()
                if (task.isSuccessful) {
                    Log.i("NYA_serv", "Uploading finished successfully")
                    jobFinished(params, false)
                } else {
                    Log.i("NYA_serv", "Uploading finished with an error")
                    jobFinished(params, true)
                }
            }
        }
        //Keep alive?
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.i("NYA_serv", "Job stopped: ${params?.jobId}")
        //Restart?
        return true
    }

    private inner class MainPicCallable() : Callable<String?> {
        override fun call(): String? {
            if (mMainPicUri == null) return null
            val mainPicUri = Uri.parse(mMainPicUri)
            val stepsSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_STEPS_PHOTOS)
            val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
            val photoRef = stepsSRef.child("$mAuthorUID/${mainPicUri.lastPathSegment!!}")
            val uploadTask = photoRef.putFile(mainPicUri, metadata)
            val uriTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation photoRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                } else {
                    task.exception?.let {
                        throw it
                    }
                }
            }
            Tasks.await(uriTask)
            Log.i("NYA_serv", "MainPicResult!! : ${uriTask.result}")
            return uriTask.result.toString()
        }
    }

    private inner class StepsCallable() : Callable<List<String>?> {
        override fun call(): List<String>? {
            if (mStepsList == null) return null
            val stepPicUris = ArrayList<String>()
            for (step in mStepsList!!) {
                step.firstPicUri?.let {
                    stepPicUris.add(it)
                }
                step.secondPicUri?.let {
                    stepPicUris.add(it)
                }
                step.thirdPicUri?.let {
                    stepPicUris.add(it)
                }
            }
            val size = stepPicUris.size
            if (size == 0) return null
            val uploadTasks = ArrayList<Task<String?>>(size)
            for (stepPicUri in stepPicUris) {
                uploadTasks.add(Tasks.call(sExecutor, SingleStepPicCallable(Uri.parse(stepPicUri))))
            }
            Tasks.await(Tasks.whenAll(uploadTasks))
            val downloadUrls = ArrayList<String>()
            for (task in uploadTasks) {
                task.result?.let {
                    downloadUrls.add(it)
                }
            }
            Log.i("NYA_serv", "StepsResult!! : $downloadUrls")
            return downloadUrls
        }
    }

    private inner class SingleStepPicCallable(stepPicUri : Uri) : Callable<String?> {
        val mStepPicUri = stepPicUri

        override fun call(): String? {
            val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
            val stepsSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_STEPS_PHOTOS)
            val photoRef = stepsSRef.child("$mAuthorUID/${mStepPicUri.lastPathSegment!!}")
            val uploadTask = photoRef.putFile(mStepPicUri, metadata)
            val uriTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation photoRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                } else {
                    task.exception?.let {
                        throw it
                    }
                }
            }
            Tasks.await(uriTask)
            Log.i("Nya_serv", "Single step result!! : ${uriTask.result}")
            return uriTask.result.toString()
        }
    }

    companion object {
        private const val AMOUNT_OF_THREADS = 2
        private val sExecutor = Executors.newFixedThreadPool(AMOUNT_OF_THREADS)
    }

}