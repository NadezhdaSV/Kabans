package com.example.kabans

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.ColorUtils
import com.example.kabans.databinding.ActivityMainBinding
import com.example.kabans.databinding.ActivityPopupStatisticsBinding

class popupStatistics : AppCompatActivity() {
    lateinit var bindClass : ActivityPopupStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        var popupTitle = ""
        var popupText = ""
        var popupButton = ""

        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        bindClass = ActivityPopupStatisticsBinding.inflate(layoutInflater)
        setContentView(bindClass.root)

        val bundle = intent.extras
        popupTitle = bundle?.getString("popuptitle", "Статистика") ?: ""
        popupText = bundle?.getString("popuptext", "Ваша статистика") ?: ""
        popupButton = bundle?.getString("popupbtn", "ОК") ?: ""

        bindClass.popupWindowTitle.text = popupTitle
        bindClass.popupWindowText.text = popupText
        bindClass.popupWindowButton.text = popupButton

        // Fade animation for the background of Popup Window
        val alpha = 100 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            bindClass.popupWindowBackground.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()

        // Fade animation for the Popup Window
        bindClass.popupWindowViewWithBorder.alpha = 0f
        bindClass.popupWindowViewWithBorder.animate().alpha(1f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // Close the Popup Window when you press the button
        bindClass.popupWindowButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        // Fade animation for the background of Popup Window when you press the back button
        val alpha = 100 // between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            bindClass.popupWindowBackground.setBackgroundColor(
                animator.animatedValue as Int
            )
        }

        // Fade animation for the Popup Window when you press the back button
        bindClass.popupWindowViewWithBorder.animate().alpha(0f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // After animation finish, close the Activity
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        colorAnimation.start()
    }
}