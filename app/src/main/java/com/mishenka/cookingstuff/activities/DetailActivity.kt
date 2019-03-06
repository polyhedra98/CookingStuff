package com.mishenka.cookingstuff.activities

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.*
import com.mishenka.cookingstuff.interfaces.CommentListener
import com.mishenka.cookingstuff.utils.MainApplication
import com.mishenka.cookingstuff.utils.Utils
import com.mishenka.cookingstuff.utils.database.CookingDatabase
import com.mishenka.cookingstuff.utils.database.PersistableBookmark
import com.mishenka.cookingstuff.views.CommentView
import com.mishenka.cookingstuff.views.NonInteractiveCommentView
import com.mishenka.cookingstuff.views.NonInteractiveIngredientView
import com.mishenka.cookingstuff.views.NonInteractiveStepView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class DetailActivity : AppCompatActivity(), CommentListener {
    private var mRecipeKey: String? = null
    private var mIsBookmarked: Boolean? = null
    private var mComment: Comment? = null

    private val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        mRecipeKey = intent.getStringExtra(Utils.RECIPE_ID_KEY)
        mIsBookmarked = intent.getBooleanExtra(Utils.IS_BOOKMARKED_KEY, false)
        if (mIsBookmarked != null && mIsBookmarked!!) {
            val db = CookingDatabase.getInstance(MainApplication.applicationContext())
            val persistableBookmark = PersistableBookmark<BookmarkData>(db!!)
            var bookmark: BookmarkData? = null
            GlobalScope.launch(Dispatchers.Main) {
                bookmark = GlobalScope.async { persistableBookmark.loadBookmark(mRecipeKey!!, BookmarkData.CREATOR) }.await()
                if (bookmark != null) {
                    val recipeName = bookmark?.name
                    recipeName?.let { safeName ->
                        updateUIName(safeName)
                    }

                    val recipeAuthor = bookmark?.author
                    recipeAuthor?.let { safeAuthor ->
                        updateUIAuthor(safeAuthor)
                    }

                    val recipeDescription = bookmark?.description
                    updateUIDescription(recipeDescription)

                    val commentsAllowed = bookmark?.commentsAllowed
                    updateUIComments(commentsAllowed)

                    val mainPicUri = bookmark?.mainPicUri
                    Log.i("NYA", "Bookmark main pic uri: $mainPicUri")
                    updateUIMainPic(mainPicUri)

                    val stepsList = bookmark?.stepsList
                    Log.i("NYA", "Bookmark steps: $stepsList")
                    updateUISteps(stepsList)

                    val ingredientsList = bookmark?.ingredientsList
                    Log.i("NYA", "Bookmark ingredients: $ingredientsList")
                    updateUIIngredients(ingredientsList)

                    val currentRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(mRecipeKey!!)
                    currentRecipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            throw p0.toException()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val currentReadCount = p0.child(Utils.CHILD_RECIPE_READ_COUNT).value as Long?
                            currentReadCount?.let { readCount ->
                                currentRecipeRef.child(Utils.CHILD_RECIPE_READ_COUNT).setValue(readCount + 1)
                            }
                        }
                    })
                }
            }
        } else {
            val wholeRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_WHOLE_RECIPE).child(mRecipeKey!!)
            val wholeStepListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw p0.toException()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val stepsSnapshot = p0.child(Utils.WHOLE_RECIPE_STEPS_LIST_CHILD)
                    updateUIFirebaseSteps(stepsSnapshot)

                    val ingredientsSnapshot = p0.child(Utils.WHOLE_RECIPE_INGREDIENTS_LIST_CHILD)
                    updateUIFirebaseIngredients(ingredientsSnapshot)
                }
            }
            wholeRecipeRef.addListenerForSingleValueEvent(wholeStepListener)

            val currentRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_RECIPE).child(mRecipeKey!!)
            currentRecipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw p0.toException()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val recipeName = p0.child(Utils.CHILD_RECIPE_NAME).value
                    recipeName?.let { safeName ->
                        updateUIName(safeName.toString())
                    }

                    val recipeAuthor = p0.child(Utils.CHILD_RECIPE_AUTHOR).value
                    recipeAuthor?.let { safeAuthor ->
                        updateUIAuthor(safeAuthor.toString())
                    }

                    val recipeDescription = p0.child(Utils.CHILD_RECIPE_DESCRIPTION).value
                    updateUIDescription(recipeDescription?.toString())

                    val commentsAllowed = p0.child(Utils.CHILD_RECIPE_COMMENTS).value as Boolean?
                    updateUIComments(commentsAllowed)

                    val mainPicUrl = p0.child(Utils.CHILD_RECIPE_MAIN_PIC_URL).value
                    updateUIMainPic(mainPicUrl?.toString())

                    val currentReadCount = p0.child(Utils.CHILD_RECIPE_READ_COUNT).value as Long?
                    currentReadCount?.let {
                        currentRecipeRef.child(Utils.CHILD_RECIPE_READ_COUNT).setValue(it + 1)
                    }
                }
            })
        }
    }

    private fun updateUIName(name: String) {
        val tvRecipeName = findViewById<TextView>(R.id.tv_detail_recipe_name)
        tvRecipeName.text = name
    }

    private fun updateUIAuthor(author: String?) {
        val tvAuthorName = findViewById<TextView>(R.id.tv_detail_author)
        val spannableStr = SpannableStringBuilder("post by $author")
        val authorLength = author.toString().length
        val totalLength = spannableStr.length
        spannableStr.setSpan(StyleSpan(Typeface.BOLD_ITALIC), totalLength - authorLength, totalLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStr.setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)), totalLength - authorLength, totalLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvAuthorName.text = spannableStr
    }

    private fun updateUIDescription(description: String?) {
        val tvRecipeDescription = findViewById<TextView>(R.id.tv_detail_description)
        if (description != null) {
            tvRecipeDescription.text = description
        } else {
            tvRecipeDescription.visibility = View.GONE
        }
    }

    private fun updateUIComments(commentsAllowed: Boolean?) {
        //TODO("Finish working on comments already!")
        if (commentsAllowed != null && commentsAllowed) {
            val vgOuterComments = findViewById<ViewGroup>(R.id.detail_outer_comments)
            vgOuterComments.visibility = View.VISIBLE
            val vgComments = findViewById<ViewGroup>(R.id.detail_comments)
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val user = FirebaseAuth.getInstance().currentUser
            mComment = Comment(userAvatarUrl = user?.photoUrl?.toString())
            vgComments.addView(CommentView(mComment!!, this@DetailActivity), params)

            val localCommentsRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_WHOLE_RECIPE).child(mRecipeKey!!).child(Utils.WHOLE_RECIPE_COMMENTS)
            localCommentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw p0.toException()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChildren()) {
                        val iterator = p0.children.iterator()
                        while (iterator.hasNext()) {
                            val snapshot = iterator.next()
                            val innerSnapshot = snapshot.children
                            val innerIterator = innerSnapshot.iterator()
                            val commentToShow = Comment()
                            while (innerIterator.hasNext()) {
                                val commentSnapshot = innerIterator.next()
                                when (commentSnapshot.key) {
                                    Utils.COMMENT_SNAPSHOT_TEXT -> commentToShow.text = commentSnapshot.value?.toString()
                                    Utils.COMMENT_SNAPSHOT_AUTHOR -> commentToShow.user = commentSnapshot.value?.toString()
                                    Utils.COMMENT_SNAPSHOT_AVATAR_URL -> commentToShow.userAvatarUrl = commentSnapshot.value?.toString()
                                    Utils.COMMENT_SNAPSHOT_LIKE_COUNT -> commentToShow.likeCount = commentSnapshot.value as Long?
                                }
                            }
                            vgComments.addView(NonInteractiveCommentView(commentToShow, this@DetailActivity), 1, params)
                        }
                    }
                }
            })
        } else {
            val vgOuterComments = findViewById<ViewGroup>(R.id.detail_outer_comments)
            vgOuterComments.visibility = View.GONE
        }
    }

    private fun updateUIMainPic(uri: String?) {
        val ivMainPic = findViewById<ImageView>(R.id.iv_detail_main_pic)
        if (uri != null) {
            Glide.with(ivMainPic.context)
                    .load(uri)
                    .apply(RequestOptions().centerCrop())
                    .into(ivMainPic)
        } else {
            ivMainPic.visibility = View.GONE
        }
    }

    private fun updateUIFirebaseSteps(p0: DataSnapshot) {
        try {
            if (p0.hasChildren()) {
                val iterator = p0.children.iterator()
                val vgOuterSteps = findViewById<ViewGroup>(R.id.detail_outer_steps)
                vgOuterSteps.visibility = View.VISIBLE
                while (iterator.hasNext()) {
                    val vgSteps = findViewById<ViewGroup>(R.id.detail_steps)
                    val stepToShow = NonParcelableStep()
                    val snapshot = iterator.next()
                    val snapshotChildren = snapshot.children
                    val innerIterator = snapshotChildren.iterator()
                    while(innerIterator.hasNext()) {
                        val innerSnapshot = innerIterator.next()
                        when (innerSnapshot.key) {
                            Utils.STEP_SNAPSHOT_DESCRIPTION -> stepToShow.stepDescription = innerSnapshot.value?.toString()
                            Utils.STEP_SNAPSHOT_PIC_URLS -> {
                                val picsSnapshot = innerSnapshot.children
                                val picsIterator = picsSnapshot.iterator()
                                val picUrlsList = ArrayList<String>()
                                while (picsIterator.hasNext()) {
                                    picUrlsList.add(picsIterator.next().value.toString())
                                }
                                stepToShow.picUrls = picUrlsList
                            }
                        }
                    }
                    vgSteps.addView(NonInteractiveStepView(stepToShow, this@DetailActivity), params)
                }
            } else {
                val vgSteps = findViewById<LinearLayout>(R.id.detail_outer_steps)
                vgSteps.visibility = View.GONE
            }
        } catch (e: Exception) {
            val vgSteps = findViewById<LinearLayout>(R.id.detail_outer_steps)
            vgSteps.visibility = View.GONE
            throw e
        }
    }

    private fun updateUISteps(stepsList: List<Step>?) {
        if (stepsList != null) {
            val vgOuterSteps = findViewById<ViewGroup>(R.id.detail_outer_steps)
            vgOuterSteps.visibility = View.VISIBLE
            val vgSteps = findViewById<ViewGroup>(R.id.detail_steps)
            for (step in stepsList) {
                val listOfUris = listOfNotNull(step.firstPicUri, step.secondPicUri, step.thirdPicUri)
                val nonParcelableStep = NonParcelableStep(stepDescription = step.stepDescription, picUrls = listOfUris)
                vgSteps.addView(NonInteractiveStepView(nonParcelableStep, this@DetailActivity), params)
            }
        } else {
            val vgSteps = findViewById<LinearLayout>(R.id.detail_outer_steps)
            vgSteps.visibility = View.GONE
        }
    }

    private fun updateUIFirebaseIngredients(p0: DataSnapshot) {
        try {
            if (p0.hasChildren()) {
                val iterator = p0.children.iterator()
                val vgOuterIngredients = findViewById<ViewGroup>(R.id.detail_outer_ingredients)
                vgOuterIngredients.visibility = View.VISIBLE
                while(iterator.hasNext()) {
                    val vgIngredients = findViewById<ViewGroup>(R.id.detail_ingredients)
                    val ingredientToShow = NonParcelableIngredient()
                    val snapshot = iterator.next()
                    val snapshotChildren = snapshot.children
                    val innerIterator = snapshotChildren.iterator()
                    while(innerIterator.hasNext()) {
                       val innerSnapshot = innerIterator.next()
                        when (innerSnapshot.key) {
                            Utils.INGREDIENT_SNAPSHOT_SEPARATOR -> ingredientToShow.isSeparator = innerSnapshot.value as Boolean?
                            Utils.INGREDIENT_SNAPSHOT_TEXT -> ingredientToShow.text = innerSnapshot.value?.toString()
                        }
                    }
                    vgIngredients.addView(NonInteractiveIngredientView(ingredientToShow, this@DetailActivity), params)
                }
            } else {
                val vgIngredients = findViewById<LinearLayout>(R.id.detail_outer_ingredients)
                vgIngredients.visibility = View.GONE
            }

        } catch (e: Exception) {
            val vgIngredients = findViewById<LinearLayout>(R.id.detail_outer_ingredients)
            vgIngredients.visibility = View.GONE
            throw e
        }
    }

    private fun updateUIIngredients(ingredientsList: List<Ingredient>?) {
        if (ingredientsList != null) {
            val vgOuterIngredients = findViewById<ViewGroup>(R.id.detail_outer_ingredients)
            vgOuterIngredients.visibility = View.VISIBLE
            val vgIngredients = findViewById<ViewGroup>(R.id.detail_ingredients)
            for (ingredient in ingredientsList) {
                vgIngredients.addView(NonInteractiveIngredientView(NonParcelableIngredient(ingredient.isSeparator, ingredient.text), this@DetailActivity), params)
            }
        } else {
            val vgIngredients = findViewById<LinearLayout>(R.id.detail_outer_ingredients)
            vgIngredients.visibility = View.GONE
        }
    }

    override fun onCommentSubmitButtonClicked(v: View) {
        mComment?.text?.let {
            mComment?.user = FirebaseAuth.getInstance().currentUser?.displayName
            mComment?.likeCount = 0.toLong()

            val currentRecipeRef = FirebaseDatabase.getInstance().reference.child(Utils.CHILD_WHOLE_RECIPE).child(mRecipeKey!!)
            currentRecipeRef.child(Utils.WHOLE_RECIPE_COMMENTS).push().setValue(mComment)

            val etCommentText = (v as CommentView).findViewById<EditText>(R.id.comment_text)
            val savedText = etCommentText.text.toString()
            etCommentText.text = null
            etCommentText.clearFocus()
            mComment?.text = savedText

            val vgComments = findViewById<ViewGroup>(R.id.detail_comments)
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            vgComments.addView(NonInteractiveCommentView(mComment!!, this@DetailActivity), 1, params)
        }
    }

    override fun onCommentLikeButtonClicked(v: View) {
        //TODO("Implement likes logic")
    }
}
