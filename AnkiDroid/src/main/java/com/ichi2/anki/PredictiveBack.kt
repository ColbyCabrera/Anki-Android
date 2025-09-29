package com.ichi2.anki

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.CheckResult
import com.ichi2.anim.AnimatorUtil

/**
 * A helper for creating a predictive back callback that animates the root view of an activity.
 *
 * @param activity The activity to which the callback is attached.
 * @param onHandleBackPressed A lambda to be executed when the back gesture is completed.
 * @return An [OnBackPressedCallback] that handles predictive back animations.
 */
@CheckResult
fun predictiveBackCallback(activity: Activity, onHandleBackPressed: () -> Unit): OnBackPressedCallback {
    val root = (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)

    val goBack =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onHandleBackPressed()
            }

            override fun handleOnBackStarted(backEvent: androidx.activity.BackEvent) {
                super.handleOnBackStarted(backEvent)
                AnimatorUtil.scaleAndFade(root, AnimatorUtil.ANIM_TARGET_SCALE, AnimatorUtil.ANIM_TARGET_ALPHA, AnimatorUtil.ANIM_DURATION)
            }

            override fun handleOnBackProgressed(backEvent: androidx.activity.BackEvent) {
                super.handleOnBackProgressed(backEvent)
                AnimatorUtil.scaleAndFade(
                    root,
                    AnimatorUtil.ANIM_TARGET_SCALE + (1 - AnimatorUtil.ANIM_TARGET_SCALE) * backEvent.progress,
                    AnimatorUtil.ANIM_TARGET_ALPHA + (1 - AnimatorUtil.ANIM_TARGET_ALPHA) * backEvent.progress,
                    0
                )
            }

            override fun handleOnBackCancelled() {
                super.handleOnBackCancelled()
                AnimatorUtil.scaleAndFade(root, 1f, 1f, AnimatorUtil.ANIM_DURATION)
            }
        }
    return goBack
}