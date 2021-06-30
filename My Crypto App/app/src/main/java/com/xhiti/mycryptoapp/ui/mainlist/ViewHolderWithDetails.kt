package com.baruckis.kriptofolio.ui.mainlist

import androidx.recyclerview.selection.ItemDetailsLookup

interface ViewHolderWithDetails {

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<String>
}