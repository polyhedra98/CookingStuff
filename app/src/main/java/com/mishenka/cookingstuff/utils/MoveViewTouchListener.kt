package com.mishenka.cookingstuff.utils

import android.view.MotionEvent
import android.view.GestureDetector
import android.view.View

class MoveViewTouchListener(private val mView: View) : View.OnTouchListener {
    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        private var mMotionDownX: Float = 0f
        private var mMotionDownY: Float = 0f

        override fun onDown(e: MotionEvent?): Boolean {
            e?.let { motionEvent ->
                mMotionDownX = motionEvent.rawX - mView.translationX
                mMotionDownY = motionEvent.rawY - mView.translationY
                return true
            }
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            e2?.let { motionEvent ->
                mView.translationX = motionEvent.rawX - mMotionDownX
                mView.translationY = motionEvent.rawY - mMotionDownY
                return true
            }
            return false
        }
    }
    private val mGestureDetector = GestureDetector(mView.context, mGestureListener)

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }
}