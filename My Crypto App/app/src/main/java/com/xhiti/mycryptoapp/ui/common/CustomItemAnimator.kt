package com.baruckis.kriptofolio.ui.common

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

open class CustomItemAnimator : DefaultItemAnimator(), RecyclerView.ItemAnimator.ItemAnimatorFinishedListener {

    interface OnItemAnimatorListener {
        fun getNumberOfItemsToRemove(): Int
        fun onAnimationsFinishedOnItemRemoved()
        fun getNumberOfItemsToAdd(): Int
        fun onAnimationsFinishedOnItemAdded()
    }

    enum class AnimationsFinishedType {
        REMOVE,
        ADD
    }

    private var onItemAnimatorListener: OnItemAnimatorListener? = null
    private var animationsFinishedType: AnimationsFinishedType? = null
    private var countAlreadyRemoved = 0
    private var countAlreadyAdded = 0

    override fun onAnimationsFinished() {
        if (onItemAnimatorListener != null) {
            when (animationsFinishedType) {
                AnimationsFinishedType.REMOVE -> {
                    countAlreadyRemoved++
                    if (countAlreadyRemoved == onItemAnimatorListener!!.getNumberOfItemsToRemove()) {
                        countAlreadyRemoved = 0
                        onItemAnimatorListener!!.onAnimationsFinishedOnItemRemoved()
                    }
                }
                AnimationsFinishedType.ADD -> {
                    countAlreadyAdded++
                    if (countAlreadyAdded == onItemAnimatorListener!!.getNumberOfItemsToAdd()) {
                        countAlreadyAdded = 0
                        onItemAnimatorListener!!.onAnimationsFinishedOnItemAdded()
                    }
                }
            }
        }
    }

    override fun onRemoveFinished(viewHolder: RecyclerView.ViewHolder?) {
        animationsFinishedType = AnimationsFinishedType.REMOVE
        isRunning(this)
    }

    override fun onAddFinished(item: RecyclerView.ViewHolder?) {
        animationsFinishedType = AnimationsFinishedType.ADD
        isRunning(this)
    }

    fun setOnItemAnimatorListener(onItemAnimatorListener: OnItemAnimatorListener) {
        this.onItemAnimatorListener = onItemAnimatorListener
    }

}