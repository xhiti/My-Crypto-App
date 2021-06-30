package com.baruckis.kriptofolio.ui.addsearchlist

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.databinding.ActivityAddSearchListItemBinding
import com.baruckis.kriptofolio.db.Cryptocurrency
import com.baruckis.kriptofolio.dependencyinjection.GlideApp
import com.baruckis.kriptofolio.utilities.*
import com.baruckis.kriptofolio.utilities.glide.WhiteBackground
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import kotlinx.android.synthetic.main.flipview_front_custom.view.*

class AddSearchListAdapter(val context: Context, private val cryptocurrencyClickCallback: ((Cryptocurrency) -> Unit)?) : BaseAdapter() {

    private var dataList: List<Cryptocurrency> = ArrayList()
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun setData(newDataList: List<Cryptocurrency>) {
        dataList = newDataList
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val itemBinding: ActivityAddSearchListItemBinding

        if (view == null) {

            view = inflater.inflate(R.layout.activity_add_search_list_item, parent, false)
            itemBinding = DataBindingUtil.bind(view)!!

            itemBinding.root.setOnClickListener {
                itemBinding.cryptocurrency?.let {
                    cryptocurrencyClickCallback?.invoke(it)
                }
            }

            view.tag = itemBinding

        } else {

            itemBinding = view.tag as ActivityAddSearchListItemBinding
        }

        val cryptocurrency = getItem(position) as Cryptocurrency
        itemBinding.cryptocurrency = cryptocurrency
        itemBinding.itemRanking.text = String.format("${cryptocurrency.rank}")

        itemBinding.itemImageIcon.textview_front.text = getTextFirstChars(cryptocurrency.symbol, FLIPVIEW_CHARACTER_LIMIT)

        val imageUri = Uri.parse(CRYPTOCURRENCY_IMAGE_URL).buildUpon()
                .appendPath(CRYPTOCURRENCY_IMAGE_SIZE_PX)
                .appendPath(cryptocurrency.id.toString() + CRYPTOCURRENCY_IMAGE_FILE)
                .build()

        GlideApp
                .with(itemBinding.root)
                .load(imageUri)
                .transform(MultiTransformation(WhiteBackground(), CircleCrop()))
                .into(itemBinding.itemImageIcon.imageview_front)

        itemBinding.itemName.text = cryptocurrency.name
        itemBinding.itemSymbol.text = cryptocurrency.symbol

        return itemBinding.root
    }


    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataList.size
    }

}