package com.example.overscrollbehavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

class OverScrollBehavior(context: Context?, attributeSet: AttributeSet?) :
    AppBarLayout.ScrollingViewBehavior(context, attributeSet),
    AppBarLayout.OnOffsetChangedListener {
    companion object {
        private const val OVER_SCROLL_AREA = 4
    }

    var overScrollListener: OverScrollListener? = null
    var verticalOffset = 0
    var appBarLayout: AppBarLayout? = null
    var progressBar: ProgressBar? = null
    private var overScrollY = 0

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        when (child) {
            is AppBarLayout -> appBarLayout = child
        }
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        for (a in parent.children) {
            if (a is AppBarLayout) {
                appBarLayout = a
                appBarLayout?.addOnOffsetChangedListener(this)
                break
            }
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        overScrollY = 0
        super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        if (target is RecyclerView && target.layoutManager?.childCount == 0) return false
        return true
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
//        Log.d("SCROLLP", "SCROLL: ${child.javaClass}")
        if (dyUnconsumed >= 0) {
            super.onNestedScroll(
                coordinatorLayout,
                child,
                target,
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                type,
                consumed
            )
            return
        }
        if (verticalOffset == 0 && target is RecyclerView) {
            overScrollListener?.onOverScrollStart()
            progressBar?.visibility = View.VISIBLE
            overScrollY -= (dyUnconsumed / OVER_SCROLL_AREA)
            progressBar?.translationY = -overScrollY.toFloat()
            target.translationY = overScrollY.toFloat()
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        if (target.translationY != 0f && verticalOffset == 0) {
            if (target is RecyclerView && target.translationY < -10) overScrollListener?.onOverScrollCompleted()
            moveToDefPosition(target)
        }
        // Smooth animate to 0 when the user stops scrolling
//        Log.d("SCROLLP", "STOP ${target.javaClass.name}")
    }

    private fun moveToDefPosition(target: View) {
        progressBar?.visibility = View.INVISIBLE
        target.animate().translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        this.verticalOffset = -verticalOffset
    }

    interface OverScrollListener {
        fun onOverScrollCompleted()
        fun onOverScrollStart()
    }
}
