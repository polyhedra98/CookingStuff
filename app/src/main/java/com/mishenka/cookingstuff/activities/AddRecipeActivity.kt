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
import com.mishenka.cookingstuff.utils.Utils
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import java.io.IOException


class AddRecipeActivity : AppCompatActivity(), StepsAdapter.StepListener {
    private val mStepsList = arrayListOf(Step(), Step(), Step())

    private lateinit var messagesDBRef : DatabaseReference
    private lateinit var mStepsAdapter : StepsAdapter

    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        messagesDBRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE)
        mStepsAdapter = StepsAdapter(this, R.layout.item_step, mStepsList)

        //TODO("Reimplement as a RecycleView")
        val stepsList = findViewById<ListView>(R.id.lv_steps)
        stepsList.adapter = mStepsAdapter

        //TODO("Make it a nice little '+' on the right side")
        val addStepButton = findViewById<Button>(R.id.b_add_step)
        addStepButton.setOnClickListener {
            mStepsList.add(Step())
            mStepsAdapter.notifyDataSetChanged()
        }

        val submitButton = findViewById<Button>(R.id.b_add_recipe)
        submitButton.setOnClickListener {
            val recipeName = findViewById<TextView>(R.id.et_recipe_name).text.toString()

            if (!recipeName.isEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                val username = if (user != null) user.displayName else "anonymous"
                val recipe = Recipe(recipeName, username)
                messagesDBRef.push().setValue(recipe)
                finish()
            }
        }
    }

    override fun onStepPicButtonClicked(v: View?, pv : View?, s : Step?) {
        showPictureDialog()
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
        if (requestCode == GALLERY) {
            data?.let {
                val contentURI = it.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    //TODO("Process bitmap")
                    Log.i("Nya", "Got it! ${bitmap.byteCount}")
                } catch (e : IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMERA) {
            val bitmap = data?.extras?.get("data") as Bitmap
            //TODO("Process bitmap")
            Log.i("Nya", "Got it! ${bitmap.byteCount}")
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Gallery", "Camera")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA)
    }

}
