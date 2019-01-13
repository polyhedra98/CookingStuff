package com.mishenka.cookingstuff.activities

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.adapters.StepsAdapter
import com.mishenka.cookingstuff.data.Step
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.mishenka.cookingstuff.data.Recipe
import com.mishenka.cookingstuff.data.WholeRecipe
import com.mishenka.cookingstuff.utils.Utils
import java.util.concurrent.Callable
import java.util.concurrent.Executors


class AddRecipeActivity : AppCompatActivity(), StepsAdapter.StepListener {
    private val mStepsList : ArrayList<Step> = arrayListOf(Step(), Step(), Step())

    private lateinit var mDBRef : DatabaseReference
    private lateinit var mStepsSRef : StorageReference
    private lateinit var mStepsAdapter : StepsAdapter

    private lateinit var mSubmitButton : Button

    private var mMainPicUri : Uri? = null

    private var mCurrentStep : Step? = null
    private var mCurrentButton : View? = null
    private var mCurrentParentView : View? = null

    private val MAIN_GALLERY = 1
    private val MAIN_CAMERA = 2
    private val STEP_GALLERY = 3
    private val STEP_CAMERA = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        mDBRef = FirebaseDatabase.getInstance().reference
        mStepsSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_STEPS_PHOTOS)
        mStepsAdapter = StepsAdapter(this, R.layout.item_step, mStepsList)

        val bMainPic = findViewById<Button>(R.id.b_main_picture)
        bMainPic.setOnClickListener {
            if (userVerification()) {
                showPictureDialog(MAIN_GALLERY, MAIN_CAMERA)
            }
        }

        //TODO("UI seems buggy, list gets cut off")
        val stepsList = findViewById<ListView>(R.id.lv_steps)
        stepsList.adapter = mStepsAdapter

        //TODO("Make it a nice little '+' on the right side")
        val addStepButton = findViewById<Button>(R.id.b_add_step)
        addStepButton.setOnClickListener {
            mStepsList.add(Step())
            mStepsAdapter.notifyDataSetChanged()
        }

        mSubmitButton = findViewById(R.id.b_add_recipe)
        mSubmitButton.setOnClickListener { b_submit ->
            val recipeName = findViewById<TextView>(R.id.et_recipe_name).text.toString()

            if (!recipeName.isEmpty()) {
                Tasks.call {
                    val user = FirebaseAuth.getInstance().currentUser!!
                    val username = user.displayName
                    val mainPicTask = Tasks.call(sExecutor, MainPicCallable(mMainPicUri))
                    val stepPicsTask = Tasks.call(sExecutor, StepsCallable(mStepsList))
                    Tasks.whenAll(mainPicTask, stepPicsTask).continueWithTask { task ->
                        val key = mDBRef.child(Utils.CHILD_RECIPE).push().key!!
                        val mainPicUri = mainPicTask.result
                        val stepPicUris = stepPicsTask.result
                        if (mStepsList.size == stepPicUris?.size) {
                            var counter = 0
                            var innerCounter = 0
                            while (counter < mStepsList.size) {
                                mStepsList[counter].firstPicUri = if (mStepsList[counter].firstPicUri != null) stepPicUris[innerCounter++] else null
                                mStepsList[counter].secondPicUri = if (mStepsList[counter].secondPicUri != null) stepPicUris[innerCounter++] else null
                                mStepsList[counter].firstPicUri = if (mStepsList[counter].firstPicUri != null) stepPicUris[innerCounter++] else null
                                counter++
                            }
                        } else {
                            var counter = 0
                            while (counter < mStepsList.size) {
                                mStepsList[counter].firstPicUri = null
                                mStepsList[counter].secondPicUri = null
                                mStepsList[counter].ivThirdPic = null
                                counter++
                            }
                        }
                        mDBRef.child(Utils.CHILD_RECIPE).child(key).setValue(Recipe(key = key, name = recipeName, author = username,
                                authorUID = user.uid, mainPicUri = mainPicUri)).addOnFailureListener {
                            throw it
                        }
                        mDBRef.child(Utils.CHILD_WHOLE_RECIPE).child(key).setValue(WholeRecipe(key = key, name = recipeName, author = username,
                                authorUID = user.uid, mainPicUri = mainPicUri, stepsList = mStepsList)).addOnFailureListener {
                            throw it
                        }
                    }
                }
                finish()
            }
        }
    }

    override fun onStepPicButtonClicked(v: View?, pv : View?, s : Step) {
        if (!userVerification()) return
        mCurrentButton = v
        mCurrentParentView = pv
        mCurrentStep = s
        showPictureDialog(STEP_GALLERY, STEP_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        when(requestCode) {
            MAIN_GALLERY -> data?.let {intent ->
                mMainPicUri = intent.data
                val ivMainPic = findViewById<ImageView>(R.id.iv_main_pic)
                Glide.with(ivMainPic.context)
                        .load(mMainPicUri)
                        .into(ivMainPic)
            }
            STEP_GALLERY -> data?.let { intent ->
                val currentStepPicUri : Uri? = intent.data
                currentStepPicUri?.let {
                    mCurrentButton?.visibility = View.INVISIBLE
                    when (mCurrentButton?.id) {
                        R.id.b_first_step -> {
                            mCurrentStep?.firstPicUri = currentStepPicUri.toString()
                            val firstPic = mCurrentParentView?.findViewById<ImageView>(R.id.iv_step_first)
                            firstPic?.visibility = View.VISIBLE
                            Glide.with(firstPic?.context)
                                    .load(currentStepPicUri)
                                    .into(firstPic)
                        }
                        R.id.b_second_step -> {
                            mCurrentStep?.secondPicUri = currentStepPicUri.toString()
                            val secondPic = mCurrentParentView?.findViewById<ImageView>(R.id.iv_step_second)
                            secondPic?.visibility = View.VISIBLE
                            Glide.with(secondPic?.context)
                                    .load(currentStepPicUri)
                                    .into(secondPic)
                        }
                        R.id.b_third_step -> {
                            mCurrentStep?.ivThirdPic = currentStepPicUri.toString()
                            val thirdPic = mCurrentParentView?.findViewById<ImageView>(R.id.iv_step_third)
                            thirdPic?.visibility = View.VISIBLE
                            Glide.with(thirdPic?.context)
                                    .load(currentStepPicUri)
                                    .into(thirdPic)
                        }
                        else -> {
                        }
                    }
                    mCurrentButton = null
                    mCurrentParentView = null
                    mCurrentStep = null
                }
            }
        }
    }



    private fun userVerification() : Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun showPictureDialog(RC_CODE_GALLERY : Int, RC_CODE_CAMERA: Int) {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Gallery")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery(RC_CODE_GALLERY)
                //1 -> takePhotoFromCamera(RC_CODE_CAMERA)
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery(RC_CODE : Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, RC_CODE)
    }

    private fun takePhotoFromCamera(RC_CODE : Int) {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, RC_CODE)
    }

    private class MainPicCallable(mainPicUri : Uri?) : Callable<String?> {
        val mMainPicUri = mainPicUri

        override fun call(): String? {
            if (mMainPicUri == null) return null
            val user = FirebaseAuth.getInstance().currentUser!!
            val stepsSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_STEPS_PHOTOS)
            val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
            val photoRef = stepsSRef.child("${user.uid}/${mMainPicUri.lastPathSegment!!}")
            val uploadTask = photoRef.putFile(mMainPicUri, metadata)
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
            await(uriTask)
            Log.i("NYA", "MainPicResult!! : ${uriTask.result}")
            return uriTask.result.toString()
        }
    }

    private class StepsCallable(stepsList : List<Step>) : Callable<List<String>?> {
        val mStepsList = stepsList

        override fun call(): List<String>? {
            val stepPicUris = ArrayList<String>()
            for (step in mStepsList) {
                step.firstPicUri?.let {
                    stepPicUris.add(it)
                }
                step.secondPicUri?.let {
                    stepPicUris.add(it)
                }
                step.ivThirdPic?.let {
                    stepPicUris.add(it)
                }
            }
            val size = stepPicUris.size
            if (size == 0) return null
            val uploadTasks = ArrayList<Task<String?>>(size)
            for (stepPicUri in stepPicUris) {
                uploadTasks.add(Tasks.call(sExecutor, SingleStepPicCallable(Uri.parse(stepPicUri))))
            }
            await(Tasks.whenAll(uploadTasks))
            val downloadUrls = ArrayList<String>()
            for (task in uploadTasks) {
                task.result?.let {
                    downloadUrls.add(it)
                }
            }
            Log.i("NYA", "StepsResult!! : $downloadUrls")
            return downloadUrls
        }
    }

    private class SingleStepPicCallable(stepPicUri : Uri) : Callable<String?> {
        val mStepPicUri = stepPicUri

        override fun call(): String? {
            val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
            val user = FirebaseAuth.getInstance().currentUser!!
            val stepsSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_STEPS_PHOTOS)
            val photoRef = stepsSRef.child("${user.uid}/${mStepPicUri.lastPathSegment!!}")
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
            await(uriTask)
            Log.i("Nya", "Single step result!! : ${uriTask.result}")
            return uriTask.result.toString()
        }
    }

    companion object {
        private const val AMOUNT_OF_THREADS = 3
        private val sExecutor = Executors.newFixedThreadPool(AMOUNT_OF_THREADS)
    }
}
