package com.mishenka.cookingstuff.activities

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mishenka.cookingstuff.R
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mishenka.cookingstuff.data.*
import com.mishenka.cookingstuff.interfaces.IngredientListener
import com.mishenka.cookingstuff.interfaces.StepListener
import com.mishenka.cookingstuff.services.ImprovedUploadService
import com.mishenka.cookingstuff.services.TempSupportUploadService
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.MoveViewTouchListener
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.utils.database.CookingDatabase
import com.mishenka.cookingstuff.utils.database.DbWorkerThread
import com.mishenka.cookingstuff.utils.database.PersistableParcelable
import com.mishenka.cookingstuff.views.IngredientView
import com.mishenka.cookingstuff.views.StepView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class AddRecipeActivity : AppCompatActivity(), StepListener, IngredientListener {
    private val mIngredientsList: ArrayList<Ingredient> = arrayListOf(Ingredient(false), Ingredient(false), Ingredient(false))
    private val mStepsList: ArrayList<Step> = arrayListOf(Step(), Step(), Step())

    private lateinit var mDBRef: DatabaseReference
    private lateinit var mStepsSRef: StorageReference
    private lateinit var mSubmitButton : Button
    private lateinit var mDbWorkerThread: DbWorkerThread

    private var mLocalDb: CookingDatabase? = null
    private var mMainPicUri: Uri? = null
    private var mCurrentStep: Step? = null
    private var mCurrentButton: View? = null
    private var mCurrentParentView: View? = null

    private val MAIN_GALLERY = 1
    private val MAIN_CAMERA = 2
    private val STEP_GALLERY = 3
    private val STEP_CAMERA = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        mDBRef = FirebaseDatabase.getInstance().reference
        mStepsSRef = FirebaseStorage.getInstance().reference.child(Utils.CHILD_COOKING_PHOTOS)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()

        val bMainPic = findViewById<Button>(R.id.b_main_picture)
        bMainPic.setOnClickListener {
            if (userVerification()) {
                showPictureDialog(MAIN_GALLERY, MAIN_CAMERA)
            }
        }

        val ivMainPic = findViewById<ImageView>(R.id.iv_main_pic)
        ivMainPic.visibility = View.GONE

        val params = LinearLayout.LayoutParams (
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val vgIngredients = findViewById<ViewGroup>(R.id.insert_ingredients)
        for (ingredient in mIngredientsList) {
            val iv = IngredientView(ingredient, this)
            iv.setOnTouchListener(MoveViewTouchListener(iv))
            vgIngredients.addView(iv, params)
        }
        val vgSteps = findViewById<ViewGroup>(R.id.insert_steps)
        for (step in mStepsList) {
            vgSteps.addView(StepView(step, this), params)
        }

        val addStepButton = findViewById<Button>(R.id.b_add_step)
        addStepButton.setOnClickListener {
            mStepsList.add(Step())
            vgSteps.addView(StepView(mStepsList.last(), this), params)
        }

        val bAddSection = findViewById<Button>(R.id.b_add_section)
        bAddSection.setOnClickListener {
            mIngredientsList.add(Ingredient(true))
            vgIngredients.addView(IngredientView(mIngredientsList.last(), this), params)
        }

        val bAddIngredient = findViewById<Button>(R.id.b_add_ingredient)
        bAddIngredient.setOnClickListener {
            mIngredientsList.add(Ingredient(false))
            val iv = IngredientView(mIngredientsList.last(), this)
            iv.setOnTouchListener(MoveViewTouchListener(iv))
            vgIngredients.addView(iv, params)
        }

        mSubmitButton = findViewById(R.id.b_add_recipe)
        mSubmitButton.setOnClickListener { b_submit ->
            val recipeName = findViewById<TextView>(R.id.et_recipe_name).text.toString()

            if (!recipeName.isEmpty()) {
                uploadToServer(recipeName)
                finish()
            }
        }
    }

    private fun uploadToServer(recipeName: String) {
        val user = FirebaseAuth.getInstance().currentUser!!
        val username = user.displayName
        val userID = user.uid
        val recDesc = findViewById<EditText>(R.id.et_recipe_description).text?.toString()
        val commentsAllowed = findViewById<Switch>(R.id.sw_allow_comments).isChecked
        mLocalDb = CookingDatabase.getInstance(MainApplication.applicationContext())
        val persistableParcelable = PersistableParcelable<UploadData>(mLocalDb!!)
        val uploadData = UploadData(name = recipeName, authorUID = userID, author = username, description = recDesc,
                mainPicUri = mMainPicUri?.toString(), commentsAllowed = commentsAllowed,ingredientsList = mIngredientsList, stepsList = mStepsList)
        val dataId = recipeName + System.currentTimeMillis()
        GlobalScope.async {
            persistableParcelable.save(dataId, uploadData)
            trySchedulingJob(dataId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleUploadJob(dataId: String) {
        Log.i("NYA_serv", "SDK VER ge than Lollipop")
        val startServiceIntent = Intent(this, ImprovedUploadService::class.java)
        startServiceIntent.putExtra(Utils.UPLOAD_DATA_KEY, dataId)
        startService(startServiceIntent)
        val componentName = ComponentName(this, ImprovedUploadService::class.java)
        val jobInfo = JobInfo.Builder(Utils.UPLOAD_SERVICE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build()
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = jobScheduler.schedule(jobInfo)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.i("NYA_serv", "Job scheduled!")
        } else {
            Log.i("NYA_serv", "Job not scheduled..")
        }
        stopService()
        mDbWorkerThread.quit()
        CookingDatabase.destroyInstance()
    }

    private fun supportScheduleUploadJob(dataId: String) {
        Log.i("NYA_serv", "SDK VER l than Lollipop")
        val intent = Intent(this, TempSupportUploadService::class.java)
        intent.putExtra(Utils.UPLOAD_DATA_KEY, dataId)
        startService(intent)
    }

    private fun trySchedulingJob(dataId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleUploadJob(dataId)
        } else {
            supportScheduleUploadJob(dataId)
        }
    }

    private fun stopService() {
        stopService(Intent(this, ImprovedUploadService::class.java))
    }

    override fun onStepPicButtonClicked(v: View?, pv: View?, s: Step) {
        if (!userVerification()) return
        mCurrentButton = v
        mCurrentParentView = pv
        mCurrentStep = s
        showPictureDialog(STEP_GALLERY, STEP_CAMERA)
    }

    override fun onStepClearButtonClicked(v: View) {
        val parent = (v.parent as ViewGroup)
        val pos = parent.indexOfChild(v)
        parent.removeViewAt(pos)
        mStepsList.removeAt(pos)
    }

    override fun onIngredientClearButtonClicked(v: View) {
        val parent = (v.parent as ViewGroup)
        val pos = parent.indexOfChild(v)
        parent.removeViewAt(pos)
        mIngredientsList.removeAt(pos)
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
                ivMainPic.visibility = View.VISIBLE
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
                            firstPic?.let { fp ->
                                fp.visibility = View.VISIBLE
                                Glide.with(fp.context)
                                        .load(currentStepPicUri)
                                        .apply(RequestOptions().centerCrop())
                                        .into(fp)
                            }
                        }
                        R.id.b_second_step -> {
                            mCurrentStep?.secondPicUri = currentStepPicUri.toString()
                            val secondPic = mCurrentParentView?.findViewById<ImageView>(R.id.iv_step_second)
                            secondPic?.let { sp ->
                                sp.visibility = View.VISIBLE
                                Glide.with(sp.context)
                                        .load(currentStepPicUri)
                                        .apply(RequestOptions().centerCrop())
                                        .into(sp)
                            }
                        }
                        R.id.b_third_step -> {
                            mCurrentStep?.thirdPicUri = currentStepPicUri.toString()
                            val thirdPic = mCurrentParentView?.findViewById<ImageView>(R.id.iv_step_third)
                            thirdPic?.let { tp ->
                                tp.visibility = View.VISIBLE
                                Glide.with(tp.context)
                                        .load(currentStepPicUri)
                                        .apply(RequestOptions().centerCrop())
                                        .into(tp)
                            }
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

    private fun showPictureDialog(RC_CODE_GALLERY: Int, RC_CODE_CAMERA: Int) {
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

    private fun choosePhotoFromGallery(RC_CODE: Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, RC_CODE)
    }

    private fun takePhotoFromCamera(RC_CODE: Int) {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, RC_CODE)
    }
}
