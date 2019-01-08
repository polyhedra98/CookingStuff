package com.mishenka.cookingstuff.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mishenka.cookingstuff.R
import com.mishenka.cookingstuff.data.Recipe

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var messagesDBRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        messagesDBRef = FirebaseDatabase.getInstance().reference.child("recipes")

        val addRecipeButton = findViewById<Button>(R.id.b_add_recipe)
        addRecipeButton.setOnClickListener {
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
}
