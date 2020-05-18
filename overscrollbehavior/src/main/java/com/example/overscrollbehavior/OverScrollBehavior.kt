package com.example.overscrollbehavior

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import com.google.android.material.appbar.AppBarLayout

class OverScrollBehavior(context: Context?, attributeSet: AttributeSet?) :
    AppBarLayout.ScrollingViewBehavior(context, attributeSet),
    AppBarLayout.OnOffsetChangedListener {
    companion object {
        private const val OVER_SCROLL_AREA = 4
    }

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
            is ProgressBar -> progressBar = child
        }
        Log.d("COORD", "PONLAY: ${child.javaClass.name}")
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        Log.d("COORD", "Depends: ${child.javaClass.name}")
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
        Log.d("COORD", "VIEW: ${child.javaClass}")
        if (dyUnconsumed == 0) {
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
        if (verticalOffset == 0) {
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
        // Smooth animate to 0 when the user stops scrolling
        moveToDefPosition(target)
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        // Scroll view by inertia when current position equals to 0
        if (overScrollY == 0) {
            return false
        }
        // Smooth animate to 0 when user fling view
        moveToDefPosition(target)
        return true
    }



    private fun moveToDefPosition(target: View) {
        progressBar?.visibility = View.INVISIBLE
        target.translationY = 0f
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        this.verticalOffset = -verticalOffset
    }


}
