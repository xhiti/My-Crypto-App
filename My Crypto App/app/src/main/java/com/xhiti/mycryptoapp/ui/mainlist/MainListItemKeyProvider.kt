package com.baruckis.kriptofolio.ui.mainlist

import androidx.recyclerview.selection.ItemKeyProvider
import com.baruckis.kriptofolio.db.MyCryptocurrency

class MainListItemKeyProvider(
        private var myCryptocurrencyList: List<MyCryptocurrency>,
        scope: Int = ItemKeyProvider.SCOPE_CACHED
) : ItemKeyProvider<String>(scope) {

    private lateinit var keyToPosition: MutableMap<String, Int>

    init {
        updataData(myCryptocurrencyList)
    }

    fun updataData(newCryptocurrencyList: List<MyCryptocurrency>) {
        myCryptocurrencyList = newCryptocurrencyList
        keyToPosition = HashMap(myCryptocurrencyList.size)

        for ((i, cryptocurrency) in myCryptocurrencyList.withIndex()) {
            keyToPosition[cryptocurrency.myId.toString()] = i
        }
    }


    override fun getKey(position: Int): String? {
        return myCryptocurrencyList[position].myId.toString()
    }

    override fun getPosition(key: String): Int {
        return keyToPosition.get(key) ?: -1
    }
}