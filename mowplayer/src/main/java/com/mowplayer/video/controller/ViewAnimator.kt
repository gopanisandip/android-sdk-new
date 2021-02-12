package com.mowplayer.video.controller

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.ViewPropertyAnimatorUpdateListener
import com.mowplayer.video.controller.ViewAnimator.Listeners.Cancel

/**
 * Created by Bruce Too
 * On 7/12/16.
 * At 15:44
 */
class ViewAnimator(var view: View?) {
    /**
     * change the view to execute animator
     *
     * @param view view
     * @return ViewAnimator
     */
    fun andPutOn(view: View?): ViewAnimator {
        this.view = view
        return this
    }

    /**
     * Wait system to finish inflate view,and get its size
     *
     * @param sizeListener callback when finished
     */
    fun waitForSize(sizeListener: Listeners.Size?) {
        view!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (view != null) {
                    view!!.viewTreeObserver.removeOnPreDrawListener(this)
                    sizeListener?.onSize(this@ViewAnimator)
                }
                return false
            }
        })
    }

    /**
     * get view's top value in global screen
     *
     * @return top value
     */
    val y: Float
        get() {
            val rect = Rect()
            view!!.getGlobalVisibleRect(rect)
            return rect.top.toFloat()
        }

    /**
     * get view's x value = translationX + getLeft
     *
     * @return x value
     */
    val x: Float
        get() = ViewCompat.getX(view)

    fun alpha(alpha: Float): ViewAnimator {
        if (view != null) {
            view!!.alpha = alpha
        }
        return this
    }

    fun scaleX(scale: Float): ViewAnimator {
        if (view != null) {
            view!!.scaleX = scale
        }
        return this
    }

    fun scaleY(scale: Float): ViewAnimator {
        if (view != null) {
            view!!.scaleY = scale
        }
        return this
    }

    fun scale(scale: Float): ViewAnimator {
        if (view != null) {
            view!!.scaleX = scale
            view!!.scaleY = scale
        }
        return this
    }

    fun translationX(translation: Float): ViewAnimator {
        if (view != null) {
            view!!.translationX = translation
        }
        return this
    }

    fun translationY(translation: Float): ViewAnimator {
        if (view != null) {
            view!!.translationY = translation
        }
        return this
    }

    fun translation(translationX: Float, translationY: Float): ViewAnimator {
        if (view != null) {
            view!!.translationX = translationX
            view!!.translationY = translationY
        }
        return this
    }

    fun pivotX(percent: Float): ViewAnimator {
        if (view != null) {
            view!!.pivotX = view!!.width * percent
        }
        return this
    }

    fun pivotY(percent: Float): ViewAnimator {
        if (view != null) {
            view!!.pivotY = view!!.height * percent
        }
        return this
    }

    fun visible(): ViewAnimator {
        if (view != null) {
            view!!.visibility = View.VISIBLE
        }
        return this
    }

    fun invisible(): ViewAnimator {
        if (view != null) {
            view!!.visibility = View.INVISIBLE
        }
        return this
    }

    fun gone(): ViewAnimator {
        if (view != null) {
            view!!.visibility = View.GONE
        }
        return this
    }

    /**
     * 属性变化的动画效果
     *
     * @return
     */
    fun animate(): AnimatorExecutor {
        return AnimatorExecutor(this)
    }

    internal class AnimatorListener(var animatorExecutor: AnimatorExecutor) : ViewPropertyAnimatorListener {
        override fun onAnimationStart(view: View) {
            val animatorExecutor = animatorExecutor
            if (animatorExecutor != null && animatorExecutor.startListener != null) {
                val startListener = animatorExecutor.startListener
                startListener!!.onStart()
            }
        }

        override fun onAnimationEnd(view: View) {
            val animatorExecutor = animatorExecutor
            if (animatorExecutor != null && animatorExecutor.endListener != null) {
                val endListener = animatorExecutor.endListener
                endListener!!.onEnd()
            }
        }

        override fun onAnimationCancel(view: View) {
            val animatorExecutor = animatorExecutor
            if (animatorExecutor != null && animatorExecutor.cancelListener != null) {
                val cancelListener = animatorExecutor.cancelListener
                cancelListener!!.onCancel()
            }
        }
    }

    internal class AnimatorUpdate(var animatorExecutor: AnimatorExecutor?) : ViewPropertyAnimatorUpdateListener {
        override fun onAnimationUpdate(view: View) {
            if (animatorExecutor != null && animatorExecutor!!.updateListener != null) {
                val updateListener = animatorExecutor!!.updateListener
                updateListener!!.update()
            }
        }
    }

    /**
     * this inner class used to executor animator
     */
    class AnimatorExecutor internal constructor(viewAnimator: ViewAnimator) {
        //but < 14 there are nothing...
        val animator: ViewPropertyAnimatorCompat
        val viewAnimator: ViewAnimator
        var startListener: Listeners.Start? = null
        var endListener: Listeners.End? = null
        var updateListener: Listeners.Update? = null
        var cancelListener: Cancel? = null
        fun alpha(alpha: Float): AnimatorExecutor {
            animator.alpha(alpha)
            return this
        }

        fun alpha(from: Float, to: Float): AnimatorExecutor {
            viewAnimator.alpha(from)
            return alpha(to)
        }

        fun scaleX(scale: Float): AnimatorExecutor {
            animator.scaleX(scale)
            return this
        }

        fun scaleX(from: Float, to: Float): AnimatorExecutor {
            viewAnimator.scaleX(from)
            return scaleX(to)
        }

        fun scaleY(scale: Float): AnimatorExecutor {
            animator.scaleY(scale)
            return this
        }

        fun scaleY(from: Float, to: Float): AnimatorExecutor {
            viewAnimator.scaleY(from)
            return scaleY(to)
        }

        fun scale(scale: Float): AnimatorExecutor {
            animator.scaleX(scale)
            animator.scaleY(scale)
            return this
        }

        fun scale(from: Float, to: Float): AnimatorExecutor {
            viewAnimator.scale(from)
            return scale(to)
        }

        fun translationX(translation: Float): AnimatorExecutor {
            animator.translationX(translation)
            return this
        }

        fun translationX(from: Float, to: Float): AnimatorExecutor {
            viewAnimator.translationX(from)
            return translationX(to)
        }

        fun translationY(translation: Float): AnimatorExecutor {
            animator.translationY(translation)
            return this
        }

        fun translationY(from: Float, to: Float): AnimatorExecutor {
            viewAnimator.translationY(from)
            return translationY(to)
        }

        fun translation(translationX: Float, translationY: Float): AnimatorExecutor {
            animator.translationX(translationX)
            animator.translationY(translationY)
            return this
        }

        fun rotation(rotation: Float): AnimatorExecutor {
            animator.rotation(rotation)
            return this
        }

        fun duration(duration: Long): AnimatorExecutor {
            animator.duration = duration
            return this
        }

        fun startDelay(duration: Long): AnimatorExecutor {
            animator.startDelay = duration
            return this
        }

        fun interpolator(interpolator: Interpolator?): AnimatorExecutor {
            animator.interpolator = interpolator
            return this
        }

        fun end(listener: Listeners.End?): AnimatorExecutor {
            endListener = listener
            return this
        }

        fun update(listener: Listeners.Update?): AnimatorExecutor {
            updateListener = listener
            animator.setUpdateListener(AnimatorUpdate(this))
            return this
        }

        fun start(listener: Listeners.Start?): AnimatorExecutor {
            startListener = listener
            return this
        }

        fun cancel(listener: Cancel?): AnimatorExecutor {
            cancelListener = listener
            return this
        }

        /**
         * get [ViewAnimator] from [AnimatorExecutor]
         *
         * @return ViewAnimator
         */
        fun pullOut(): ViewAnimator {
            return viewAnimator
        }

        /**
         * execute view animate subsequently
         *
         * @param view view to be animated
         * @return AnimatorExecutor
         */
        fun thenAnimate(view: View?): AnimatorExecutor {
            val viewAnimator = ViewAnimator(view)
            val animatorExecutor = viewAnimator.animate()
            animatorExecutor.startDelay(animator.startDelay + animator.duration)
            return animatorExecutor
        }

        /**
         * execute view animate together
         *
         * @param view view to be animated
         * @return AnimatorExecutor
         */
        fun andAnimate(view: View?): AnimatorExecutor {
            val viewAnimator = ViewAnimator(view)
            val animatorExecutor = viewAnimator.animate()
            animatorExecutor.startDelay(animator.startDelay)
            return viewAnimator.animate()
        }

        /**
         * Constructor of AnimatorExecutor with [ViewAnimator]
         *
         * @param viewAnimator ViewAnimator to execute
         */
        init {
            animator = ViewCompat.animate(viewAnimator.view!!)
            this.viewAnimator = viewAnimator
            //set listener
            animator.setListener(AnimatorListener(this))
        }
    }

    class Listeners {
        interface End {
            fun onEnd()
        }

        interface Start {
            fun onStart()
        }

        interface Size {
            fun onSize(viewAnimator: ViewAnimator?)
        }

        interface Update {
            fun update()
        }

        interface Cancel {
            fun onCancel()
        }
    }

    companion object {
        /**
         * add a view to execute animator
         *
         * @param view view
         * @return ViewAnimator
         */
        @JvmStatic
        fun putOn(view: View?): ViewAnimator {
            return ViewAnimator(view)
        }
    }
}