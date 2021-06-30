package com.baruckis.kriptofolio.utilities

import android.os.Build
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import com.baruckis.kriptofolio.R

class PrimaryActionModeController : ActionMode.Callback {

    private lateinit var activity: AppCompatActivity
    private var statusBarColor: Int = 0

    interface PrimaryActionModeListener {
        fun onEnterActionMode()
        fun onLeaveActionMode()
        fun onActionItemClick(item: MenuItem)
    }

    private var primaryActionModeListener: PrimaryActionModeListener? = null
    private var mode: ActionMode? = null

    @MenuRes
    private var menuResId: Int = 0
    private var title: String? = null
    private var subtitle: String? = null

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        primaryActionModeListener?.onEnterActionMode()

        mode?.let {
            mode.menuInflater.inflate(menuResId, menu)
            mode.title = title
            mode.subtitle = subtitle
            this.mode = it

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBarColor = activity.window.statusBarColor
                activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.colorForActionModeStatusBar)
            }
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        primaryActionModeListener?.onLeaveActionMode()
        this.mode = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = statusBarColor
        }
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        item?.let {
            primaryActionModeListener?.onActionItemClick(item)
        }
        return true
    }

    fun startActionMode(activity: AppCompatActivity,
                        primaryActionModeListener: PrimaryActionModeListener,
                        @MenuRes menuResId: Int,
                        title: String? = null,
                        subtitle: String? = null
    ) {
        this.menuResId = menuResId
        this.title = title
        this.subtitle = subtitle
        this.activity = activity
        this.primaryActionModeListener = primaryActionModeListener

        activity.startSupportActionMode(this)
    }

    fun finishActionMode() {
        mode?.finish()
    }

    fun isInMode(): Boolean {
        return mode != null
    }

    fun setTitle(text: String) {
        mode?.title = text
    }

}