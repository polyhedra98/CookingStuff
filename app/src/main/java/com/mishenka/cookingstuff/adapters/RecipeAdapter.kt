package com.mishenka.cookingstuff.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mishenka.cookingstuff.data.Recipe
import android.app.Activity
import android.widget.TextView
import com.mishenka.cookingstuff.R


class RecipeAdapter(context: Context, resource: Int, objects: List<Recipe>) : ArrayAdapter<Recipe>(context, resource, objects) {
    private val mResource = resource

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val returnView = convertView ?: (context as Activity).layoutInflater.inflate(mResource, parent, false)

        val recipe = getItem(position)
        val tvRecipeName = returnView.findViewById<TextView>(R.id.tv_recipe_name)
        val tvAuthorName = returnView.findViewById<TextView>(R.id.tv_author_name)
        tvRecipeName.text = recipe?.name
        tvAuthorName.text = recipe?.author

        return returnView
    }

}