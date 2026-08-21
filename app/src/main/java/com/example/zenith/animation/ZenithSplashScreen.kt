package com.example.zenith.animation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen

fun setupSplashScreenExitAnimation(splashScreen: SplashScreen) {
    splashScreen.setOnExitAnimationListener { splashScreenView ->
        val iconView = splashScreenView.iconView

        // 1. Logo "Pulse" Scale Up
        val scaleX = ObjectAnimator.ofFloat(
            iconView,
            View.SCALE_X,
            1f, 1.2f
        )
        val scaleY = ObjectAnimator.ofFloat(
            iconView,
            View.SCALE_Y,
            1f, 1.2f
        )

        // 2. Fade out entire splash
        val alpha = ObjectAnimator.ofFloat(
            splashScreenView.view,
            View.ALPHA,
            1f, 0f
        )

        AnimatorSet().apply {
            interpolator = AccelerateInterpolator()
            duration = 500L
            playTogether(scaleX, scaleY, alpha)
            doOnEnd {
                splashScreenView.remove()
            }
            start()
        }
    }
}
