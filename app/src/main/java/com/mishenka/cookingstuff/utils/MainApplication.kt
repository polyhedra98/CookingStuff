package com.mishenka.cookingstuff.utils

import android.content.Context
import android.support.multidex.MultiDexApplication

class MainApplication : MultiDexApplication() {
    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}