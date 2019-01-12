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
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.mishenka.cookingstuff.data.Recipe
import com.mishenka.cookingstuff.data.WholeRecipe
import com.mishenka.cookingstuff.utils.Utils
import kotlinx.coroutines.*


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
            val ivMainPic = findViewById<ImageView>(R.id.iv_main_pic)
            Glide.with(ivMainPic.context)
                    .load(mMainPicUri)
                    .into(ivMainPic)
        }

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
                GlobalScope.launch {
                    val user = FirebaseAuth.getInstance().currentUser
                    val mainPicDownloadUrl = uploadMainPic(user!!)
                    val username = user.displayName
                    val key = mDBRef.child(Utils.CHILD_RECIPE).push().key!!
                    mDBRef.child(Utils.CHILD_RECIPE).child(key).setValue(Recipe(key = key, name = recipeName, author = username,
                            authorUID = user.uid, mainPicUri = mainPicDownloadUrl.toString()))
                    mDBRef.child(Utils.CHILD_WHOLE_RECIPE).child(key).setValue(WholeRecipe(key = key, name = recipeName, author = username,
                            authorUID = user.uid, mainPicUri = mainPicDownloadUrl.toString(), stepsList = mStepsList))
                }

                /*val user = FirebaseAuth.getInstance().currentUser
                val username = if (user != null) user.displayName else "anonymous"
                val key = mDBRef.child(Utils.CHILD_RECIPE).push().key!!
                mDBRef.child(Utils.CHILD_RECIPE).child(key).setValue(Recipe(key = key, name = recipeName, author = username,
                        authorUID = user?.uid, mainPicUri = if (mMainPicDownloadUrl == null) "" else mMainPicDownloadUrl.toString()))*/
                finish()
            }
        }
    }

    private suspend fun uploadMainPic(user : FirebaseUser) : Uri? {
        if (mMainPicUri == null) return null
        val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
        val photoRef = mStepsSRef.child("${user.uid}/${mMainPicUri!!.lastPathSegment!!}")
        val uploadTask = photoRef.putFile(mMainPicUri!!, metadata)
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
        //TODO("WHAT A JUNK LOL)
        //TODO("I couldn't join, so I didn't know what else to do")
        //TODO("Works for now, gonna change later")
        while (!uriTask.isComplete) {}
        return uriTask.result
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
                /*val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
                val photoRef = mStepsSRef.TODO("Additional node here")child(contentURI!!.lastPathSegment!!)
                mSubmitButton.isEnabled = false
                val uploadTask = photoRef.putFile(contentURI, metadata)
                val uriTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation photoRef.downloadUrl
                }).addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        mMainPicDownloadUrl = task.result
                        mSubmitButton.isEnabled = true
                    } else {
                        task.exception?.let {
                            throw it
                        }
                    }
                }*/
                /*val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    //TODO("Process bitmap")
                    Log.i("Nya", "Got it! ${bitmap.byteCount}")
                } catch (e : IOException) {
                    e.printStackTrace()
                }*/
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
                            mCurrentStep?.thirdPicUri = currentStepPicUri.toString()
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
}
