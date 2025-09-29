package com.ichi2.anim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View

object AnimatorUtil {
    const val ANIM_DURATION = 200L
    const val ANIM_TARGET_SCALE = 0.95f
    const val ANIM_TARGET_ALPHA = 0.5f

    /**
     * Scales and fades a view.
     *
     * @param view The view to animate.
     * @param targetScale The target scale for the view.
     * @param targetAlpha The target alpha for the view.
     * @param duration The duration of the animation.
     */
    fun scaleAndFade(view: View, targetScale: Float, targetAlpha: Float, duration: Long) {
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .alpha(targetAlpha)
            .setDuration(duration)
            .start()
    }
}