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
import com.mishenka.cookingstuff.data.Recipe
import com.mishenka.cookingstuff.data.Step
import android.content.Intent
import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.mishenka.cookingstuff.data.WholeRecipe
import com.mishenka.cookingstuff.utils.Utils


class AddRecipeActivity : AppCompatActivity(), StepsAdapter.StepListener {
    private val mStepsList = arrayListOf(Step(), Step(), Step())

    private lateinit var mDBRef : DatabaseReference
    private lateinit var mStepsSRef : StorageReference
    private lateinit var mStepsAdapter : StepsAdapter

    private lateinit var mSubmitButton : Button

    private val mWholeRecipe = WholeRecipe()
    private var mMainPicDownloadUrl : Uri? = null

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

        val stepsList = findViewById<ListView>(R.id.lv_steps)
        stepsList.adapter = mStepsAdapter

        //TODO("Make it a nice little '+' on the right side")
        val addStepButton = findViewById<Button>(R.id.b_add_step)
        addStepButton.setOnClickListener {
            mStepsList.add(Step())
            mStepsAdapter.notifyDataSetChanged()
        }

        mSubmitButton = findViewById(R.id.b_add_recipe)
        mSubmitButton.setOnClickListener {
            val recipeName = findViewById<TextView>(R.id.et_recipe_name).text.toString()

            if (!recipeName.isEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                val username = if (user != null) user.displayName else "anonymous"
                val key = mDBRef.child(Utils.CHILD_RECIPE).push().key!!
                mDBRef.child(Utils.CHILD_RECIPE).child(key).setValue(Recipe(name = recipeName, author = username,
                        mainPicUri = if (mMainPicDownloadUrl == null) "" else mMainPicDownloadUrl.toString()))

                //TODO("Start an Async task instead of downloading stuff before submit button is clicked")
                mWholeRecipe.name = recipeName
                mWholeRecipe.author = username
                mWholeRecipe.mainPicUri = if (mMainPicDownloadUrl == null) "" else mMainPicDownloadUrl.toString()
                mDBRef.child(Utils.CHILD_WHOLE_RECIPE).child(key).setValue(mWholeRecipe)
                finish()
            }
        }
    }

    override fun onStepPicButtonClicked(v: View?, pv : View?, s : Step?) {
        if (!userVerification()) return
        showPictureDialog(STEP_GALLERY, STEP_CAMERA)
        v?.visibility = View.INVISIBLE
        when (v?.id) {
            R.id.b_first_step -> {
                s?.firstButtonClicked = true
                pv?.findViewById<ImageView>(R.id.iv_step_first)?.visibility = View.VISIBLE
            }
            R.id.b_second_step -> {
                s?.secondButtonClicked = true
                pv?.findViewById<ImageView>(R.id.iv_step_second)?.visibility = View.VISIBLE
            }
            R.id.b_third_step -> {
                s?.thirdButtonClicked = true
                pv?.findViewById<ImageView>(R.id.iv_step_third)?.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == MAIN_GALLERY) {
            data?.let {intent ->
                val contentURI = intent.data
                val metadata = StorageMetadata.Builder().setContentType(Utils.IMAGE_CONTENT_TYPE).build()
                val photoRef = mStepsSRef.child(contentURI!!.lastPathSegment!!)
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
                }


                /*val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    //TODO("Process bitmap")
                    Log.i("Nya", "Got it! ${bitmap.byteCount}")
                } catch (e : IOException) {
                    e.printStackTrace()
                }*/
            }
        } else if (requestCode == MAIN_CAMERA) {
            TODO("Implement")
            /*val bitmap = data?.extras?.get("data") as Bitmap
            //TODO("Process bitmap")
            Log.i("Nya", "Got it! ${bitmap.byteCount}")*/

        }
    }

    private fun userVerification() : Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun showPictureDialog(RC_CODE_GALLERY : Int, RC_CODE_CAMERA: Int) {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Gallery", "Camera")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery(RC_CODE_GALLERY)
                1 -> takePhotoFromCamera(RC_CODE_CAMERA)
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
