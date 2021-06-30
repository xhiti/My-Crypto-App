package com.baruckis.kriptofolio.ui.settings.thirdpartysoft

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.databinding.FragmentLibrariesLicensesListBinding
import com.baruckis.kriptofolio.dependencyinjection.Injectable
import com.baruckis.kriptofolio.ui.settings.LicenseFragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.fragment_libraries_licenses_list.*
import javax.inject.Inject

class LibrariesLicensesListFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: LibrariesLicensesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: LibrariesLicensesRecyclerViewAdapter
    lateinit var binding: FragmentLibrariesLicensesListBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let { activity ->

            viewModel = ViewModelProvider(activity, viewModelFactory)[LibrariesLicensesViewModel::class.java]

            activity.title = viewModel.stringsLocalization.getString(R.string.fragment_libraries_licenses_list_title)
            if (activity is AppCompatActivity) activity.supportActionBar?.subtitle = viewModel.stringsLocalization.getString(R.string.fragment_libraries_licenses_list_subtitle)

            recyclerView = recyclerview_fragment_libraries_licenses_list

            setupList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_libraries_licenses_list, container, false)
        val view: View = binding.root

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_libraries_licenses, menu)
        menu.findItem(R.id.action_more)?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_more -> showMoreLicenses()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showMoreLicenses(): Boolean {
        startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        OssLicensesMenuActivity.setActivityTitle(viewModel.stringsLocalization.getString(R.string.activity_oss_licenses_menu_title))
        return true
    }


    private fun setupList() {

        recyclerView.layoutManager = LinearLayoutManager(activity)

        val listener = object : LibrariesLicensesRecyclerViewAdapter.OnInteractionListener {

            override fun onProjectLinkButtonClick(link: String) {
                browseUrl(link)
            }

            override fun onReadLicenseButtonClick(library: String, license: String) {
                Navigation.findNavController(activity as FragmentActivity, R.id.nav_host_fragment)
                        .navigate(R.id.action_libraries_licenses_dest_to_license_dest,
                                LicenseFragment.createArguments(library, license))
            }
        }

        recyclerAdapter = LibrariesLicensesRecyclerViewAdapter(
                viewModel.librariesLicensesData, listener)

        recyclerView.adapter = recyclerAdapter
    }

    private fun browseUrl(uriString: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, viewModel.noBrowserFoundMessage, Toast.LENGTH_LONG).show()
        }
    }

}