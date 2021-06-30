package com.baruckis.kriptofolio.ui.mainlist

import android.net.Uri
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.databinding.FragmentMainListItemBinding
import com.baruckis.kriptofolio.db.MyCryptocurrency
import com.baruckis.kriptofolio.dependencyinjection.GlideApp
import com.baruckis.kriptofolio.utilities.*
import com.baruckis.kriptofolio.utilities.glide.WhiteBackground
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.flipview_front_custom.view.*

class MainRecyclerViewAdapter : RecyclerView.Adapter<MainRecyclerViewAdapter.BindingViewHolder>() {

    private var dataList: List<MyCryptocurrency> = ArrayList()
    private lateinit var selectionTracker: SelectionTracker<String>
    private var selectedData: HashMap<Int, MyCryptocurrency> = HashMap()
    private var selectionSequencesToDelete = ArrayList<HashMap<Int, MyCryptocurrency>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentMainListItemBinding.inflate(inflater, parent, false)

        binding.itemImageIcon.setOnClickListener() { _ ->
            binding.myCryptocurrency?.let {
                selectionTracker.select(it.myId.toString())
            }
        }

        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val cryptocurrency = dataList[position]
        var isSelected = false

        if (this::selectionTracker.isInitialized) {
            if (selectionTracker.isSelected(cryptocurrency.myId.toString())) {
                selectedData[position] = cryptocurrency
                isSelected = true
            } else selectedData.remove(position)
        }

        holder.bind(cryptocurrency, isSelected, holder.itemView.isInLayout)
    }

    override fun getItemCount(): Int = dataList.size

    fun setData(newDataList: List<MyCryptocurrency>) {
        dataList = newDataList
        notifyDataSetChanged()
    }

    fun getData(): List<MyCryptocurrency> {
        return dataList
    }

    @Parcelize
    class SelectionSequencesToDelete(val maplist: ArrayList<HashMap<Int, MyCryptocurrency>>) : Parcelable

    fun setSelectionSequencesToDelete(newSelectionSequences: SelectionSequencesToDelete?) {
        selectionSequencesToDelete = newSelectionSequences?.maplist ?: ArrayList()
    }

    fun getSelectionSequencesToDelete(): SelectionSequencesToDelete {
        return SelectionSequencesToDelete(selectionSequencesToDelete)
    }

    fun setSelectionTracker(selectionTracker: SelectionTracker<String>) {
        this.selectionTracker = selectionTracker
    }

    fun deleteSelectedItems(): List<MyCryptocurrency> {

        val iterator = selectedData.iterator()
        selectionSequencesToDelete = ArrayList()
        var selectionSingleSequence: HashMap<Int, MyCryptocurrency> = HashMap()
        var current: MutableMap.MutableEntry<Int, MyCryptocurrency>? = null

        while (iterator.hasNext()) {
            val next = iterator.next()

            if (current != null && next.key != current.key + 1) {
                selectionSequencesToDelete.add(selectionSingleSequence)
                selectionSingleSequence = HashMap()
            }

            selectionSingleSequence.put(next.key, next.value)
            current = next
        }

        selectionSequencesToDelete.add(selectionSingleSequence)
        var alreadyRemoved = 0
        selectionSequencesToDelete.forEach { sequence ->
            if (sequence.size > 1) {
                notifyItemRangeRemoved(sequence.keys.first() - alreadyRemoved, sequence.size)
                alreadyRemoved += sequence.size
            } else {
                notifyItemRemoved(sequence.keys.first() - alreadyRemoved)
                alreadyRemoved += 1
            }
        }

        (dataList as ArrayList).removeAll(selectedData.values)

        return selectedData.values.toMutableList()
    }

    fun restoreDeletedItems() {

        selectionSequencesToDelete.forEach { sequence ->
            if (sequence.size > 1) {
                notifyItemRangeInserted(sequence.keys.first() , sequence.size)
                (dataList as ArrayList).addAll(sequence.keys.first(), sequence.values)
            } else {
                notifyItemInserted(sequence.keys.first() )
                (dataList as ArrayList).add(sequence.keys.first(), sequence.values.first())
            }
        }

        selectionSequencesToDelete.clear()
    }


    fun clearSelected() {
        selectedData.clear()
    }


    inner class BindingViewHolder(var binding: FragmentMainListItemBinding) : RecyclerView.ViewHolder(binding.root), ViewHolderWithDetails {

        fun bind(myCryptocurrency: MyCryptocurrency, isSelected: Boolean, flipViewAnimate: Boolean) {

            binding.myCryptocurrency = myCryptocurrency
            binding.root.isSelected = isSelected
            binding.itemRanking.text = String.format("${myCryptocurrency.cryptoData.rank}")
            binding.itemImageIcon.setFrontText(getTextFirstChars(myCryptocurrency.cryptoData.symbol, FLIPVIEW_CHARACTER_LIMIT))

            val imageUri = Uri.parse(CRYPTOCURRENCY_IMAGE_URL).buildUpon()
                    .appendPath(CRYPTOCURRENCY_IMAGE_SIZE_PX)
                    .appendPath(myCryptocurrency.myId.toString() + CRYPTOCURRENCY_IMAGE_FILE)
                    .build()

            GlideApp
                    .with(binding.root)
                    .load(imageUri)
                    .transform(MultiTransformation(WhiteBackground(), CircleCrop()))
                    .into(binding.itemImageIcon.imageview_front)


            if (flipViewAnimate) binding.itemImageIcon.flip(isSelected) else binding.itemImageIcon.flipSilently(isSelected)

            binding.itemAmountCode.text = String.format("${roundValue(myCryptocurrency.amount, ValueType.Crypto)} ${myCryptocurrency.cryptoData.symbol}")
            binding.itemPrice.text = String.format("${roundValue(myCryptocurrency.cryptoData.priceFiat, ValueType.Fiat)} ${myCryptocurrency.cryptoData.currencyFiat}")
            binding.itemAmountFiat.text = String.format("${roundValue(myCryptocurrency.amountFiat, ValueType.Fiat)} ${myCryptocurrency.cryptoData.currencyFiat}")
            binding.itemPricePercentChange1h7d.text = SpannableStringBuilder(getSpannableValueStyled(binding.root.context, myCryptocurrency.cryptoData.pricePercentChange1h, SpannableValueColorStyle.Foreground, ValueType.Percent, "", "%"))
                    .append(binding.root.context.getString(R.string.string_column_coin_separator_change)).append(getSpannableValueStyled(binding.root.context, myCryptocurrency.cryptoData.pricePercentChange7d, SpannableValueColorStyle.Foreground, ValueType.Fiat, "", "%"))
            binding.itemPricePercentChange24h.text = getSpannableValueStyled(binding.root.context, myCryptocurrency.cryptoData.pricePercentChange24h, SpannableValueColorStyle.Foreground, ValueType.Percent, "", "%")
            binding.itemAmountFiatChange24h.text = getSpannableValueStyled(binding.root.context, myCryptocurrency.amountFiatChange24h, SpannableValueColorStyle.Foreground, ValueType.Percent, "", " ${myCryptocurrency.cryptoData.currencyFiat}")

            binding.executePendingBindings()
        }

        override fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> {
            return MainListItemDetails(dataList[adapterPosition].myId.toString(), adapterPosition)
        }

    }
}