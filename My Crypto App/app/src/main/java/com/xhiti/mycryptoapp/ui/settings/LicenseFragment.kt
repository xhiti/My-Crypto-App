package com.baruckis.kriptofolio.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.baruckis.kriptofolio.R
import kotlinx.android.synthetic.main.fragment_license.view.*

class LicenseFragment : Fragment() {

    companion object {
        private const val SUBTITLE_ARGUMENT = "subtitle"
        private const val LICENSE_ARGUMENT = "license"

        fun createArguments(subtitle: String, license: String): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(SUBTITLE_ARGUMENT, subtitle)
            bundle.putSerializable(LICENSE_ARGUMENT, license)
            return bundle
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let { activity ->
            activity.title = getString(R.string.fragment_license_title)
            if (activity is AppCompatActivity) activity.supportActionBar?.subtitle =
                    arguments?.getString(SUBTITLE_ARGUMENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_license, container, false)
        val textViewLicense = view.license
        textViewLicense.text = arguments?.getString(LICENSE_ARGUMENT)

        return view
    }
}
