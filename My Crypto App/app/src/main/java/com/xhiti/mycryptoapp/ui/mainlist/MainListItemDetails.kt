package com.baruckis.kriptofolio.ui.mainlist

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup

class MainListItemDetails(private val identifier: String, private val adapterPosition: Int) : ItemDetailsLookup.ItemDetails<String>() {

    override fun getSelectionKey(): String? {
        return identifier
    }

    override fun getPosition(): Int {
        return adapterPosition
    }

    override fun inSelectionHotspot(e: MotionEvent): Boolean {
        return false
    }

    override fun inDragRegion(e: MotionEvent): Boolean {
        return false
    }
}