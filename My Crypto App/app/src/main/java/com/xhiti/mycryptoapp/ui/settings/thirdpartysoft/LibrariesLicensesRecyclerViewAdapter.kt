package com.baruckis.kriptofolio.ui.settings.thirdpartysoft

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baruckis.kriptofolio.databinding.FragmentLibrariesLicensesListItemBinding
import com.baruckis.kriptofolio.db.LibraryLicenseInfo

class LibrariesLicensesRecyclerViewAdapter(
        private val dataList: List<LibraryLicenseInfo>,
        private val listener: OnInteractionListener? = null
) : RecyclerView.Adapter<LibrariesLicensesRecyclerViewAdapter.BindingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibrariesLicensesRecyclerViewAdapter.BindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentLibrariesLicensesListItemBinding.inflate(inflater, parent, false)

        binding.buttonProjectLink.setOnClickListener {
            binding.libraryLicenseInfo?.let {
                listener?.onProjectLinkButtonClick(it.link)
            }
        }

        binding.buttonReadLicense.setOnClickListener() {
            binding.libraryLicenseInfo?.let {
                listener?.onReadLicenseButtonClick(it.library, it.license)
            }
        }

        return BindingViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: LibrariesLicensesRecyclerViewAdapter.BindingViewHolder, position: Int) {
        val libraryLicenseInfo = dataList[position]

        holder.bind(libraryLicenseInfo)
    }

    interface OnInteractionListener {

        fun onProjectLinkButtonClick(link: String)

        fun onReadLicenseButtonClick(library: String, license: String)
    }


    inner class BindingViewHolder(var binding: FragmentLibrariesLicensesListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(libraryLicenseInfo: LibraryLicenseInfo) {

            binding.libraryLicenseInfo = libraryLicenseInfo

            binding.executePendingBindings()
        }

    }

}