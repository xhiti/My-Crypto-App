package com.baruckis.kriptofolio.ui.mainlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.databinding.FragmentMainListBinding
import com.baruckis.kriptofolio.db.MyCryptocurrency
import com.baruckis.kriptofolio.dependencyinjection.Injectable
import com.baruckis.kriptofolio.ui.addsearchlist.AddSearchActivity
import com.baruckis.kriptofolio.ui.common.CustomItemAnimator
import com.baruckis.kriptofolio.ui.settings.SettingsActivity
import com.baruckis.kriptofolio.utilities.*
import com.baruckis.kriptofolio.vo.Status
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject

class MainListFragment : Fragment(), Injectable, PrimaryActionModeController.PrimaryActionModeListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyListView: View
    private lateinit var recyclerAdapter: MainRecyclerViewAdapter
    private lateinit var spinnerFiatCode: Spinner

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MainViewModel
    lateinit var binding: FragmentMainListBinding
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var snackbarUnableRefresh: Snackbar? = null
    private var snackbarUndoDelete: Snackbar? = null

    private val primaryActionModeController = PrimaryActionModeController()
    private lateinit var recyclerSelectionTracker: SelectionTracker<String>
    private lateinit var recyclerSelectionTrackerItemKeyProvider: MainListItemKeyProvider
    private var deletedItems: ArrayList<MyCryptocurrency>? = null
    private var numberOfItemsToRemoveOrAdd: Int = 0

    companion object {
        private const val SELECTION_TRACKER_ID = "selection_tracker"
        private const val DELETED_ITEMS_KEY = "deleted_items"
        private const val SELECTION_SEQUENCES_TO_DELETE_KEY = "selection_sequences_to_delete"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_list, container, false)
        val v: View = binding.root

        recyclerView = v.findViewById(R.id.recyclerview_fragment_main_list)
        emptyListView = v.findViewById(R.id.layout_fragment_main_list_empty)
        swipeRefreshLayout = v.findViewById(R.id.swiperefresh_fragment_main_list)

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorForSwipeRefreshProgress1,
                R.color.colorForSwipeRefreshProgress2,
                R.color.colorForSwipeRefreshProgress3)

        swipeRefreshLayout.setOnRefreshListener {
            snackbarUnableRefresh?.dismiss()
            spinnerFiatCode.isEnabled = false
            viewModel.retry()
        }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            setupList(it, savedInstanceState)
            subscribeUi(it)
            spinnerFiatCode = it.findViewById(R.id.spinner_fiat_code)
            spinnerFiatCode.setSelection(getFiatCurrencyPosition(viewModel.getCurrentFiatCurrencyCode()), false)
            spinnerFiatCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val spinnerSelectedFiatCurrencyCode = parent?.getItemAtPosition(position) as String

                    if (spinnerSelectedFiatCurrencyCode == viewModel.getSelectedFiatCurrencyCodeFromRep()) {
                        return
                    }

                    snackbarUnableRefresh?.dismiss()
                    snackbarUndoDelete?.dismiss()

                    if (viewModel.checkIfNewFiatCurrencyCodeSameToMyCryptocurrency(spinnerSelectedFiatCurrencyCode)) {
                        viewModel.setNewCurrentFiatCurrencyCode(spinnerSelectedFiatCurrencyCode)
                        viewModel.refreshMyCryptocurrencyResourceList()
                    } else {
                        spinnerFiatCode.isEnabled = false
                        viewModel.retry(spinnerSelectedFiatCurrencyCode)
                    }
                }
            }

            viewModel.liveDataCurrentFiatCurrencyCode.observe(viewLifecycleOwner, Observer<String> { data ->
                data?.let {
                    if (viewModel.getSelectedFiatCurrencyCodeFromRep() != data) {

                        snackbarUnableRefresh?.dismiss()
                        snackbarUndoDelete?.dismiss()
                        viewModel.setSelectedFiatCurrencyCodeFromRep(data)
                        spinnerFiatCode.setSelection(getFiatCurrencyPosition(data), false)

                        if (viewModel.checkIfNewFiatCurrencyCodeSameToMyCryptocurrency(data)) {
                            viewModel.refreshMyCryptocurrencyResourceList()
                        } else {
                            spinnerFiatCode.isEnabled = false
                            viewModel.retry()
                        }
                    }
                }
            })

            it.fab.setOnClickListener { _ ->
                snackbarUndoDelete?.let { snackbar ->
                    clean()
                    snackbar.dismiss()
                }
                val intent = Intent(it, AddSearchActivity::class.java)
                startActivityForResult(intent, ADD_TASK_REQUEST)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_TASK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val cryptocurrency: MyCryptocurrency? = data?.getParcelableExtra(AddSearchActivity.EXTRA_ADD_TASK_DESCRIPTION)
                cryptocurrency?.let {
                    viewModel.addCryptocurrency(cryptocurrency)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        recyclerSelectionTracker.onSaveInstanceState(outState)
        deletedItems?.let { outState.putParcelableArrayList(DELETED_ITEMS_KEY, deletedItems) }
        outState.putParcelable(SELECTION_SEQUENCES_TO_DELETE_KEY, recyclerAdapter.getSelectionSequencesToDelete())
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        recyclerSelectionTracker.onRestoreInstanceState(savedInstanceState)
        deletedItems = savedInstanceState?.getParcelableArrayList(DELETED_ITEMS_KEY)
        if (!deletedItems.isNullOrEmpty()) {
            showSnackbarUndoDelete()
        }
    }

    override fun onEnterActionMode() {
        swipeRefreshLayout.isEnabled = false
    }

    override fun onLeaveActionMode() {
        recyclerSelectionTracker.clearSelection()
        recyclerAdapter.clearSelected()
        swipeRefreshLayout.isEnabled = true
    }

    override fun onActionItemClick(item: MenuItem) {

        when (item.itemId) {
            R.id.action_select_all -> {
                val list = ArrayList<String>()
                recyclerAdapter.getData().forEach {
                    list.add(it.myId.toString())
                }
                recyclerSelectionTracker.setItemsSelected(list, true)
            }
            R.id.action_delete -> {
                deletedItems = recyclerAdapter.deleteSelectedItems() as ArrayList
                deletedItems?.let { deletedItems ->

                    numberOfItemsToRemoveOrAdd = deletedItems.size
                    recyclerSelectionTrackerItemKeyProvider.updataData(recyclerAdapter.getData())
                    primaryActionModeController.finishActionMode()

                    val isAllDeleted = recyclerAdapter.getData().isEmpty()

                    binding.emptyList = binding.listResource?.data != null && isAllDeleted
                    viewModel.deleteCryptocurrencyList(deletedItems)
                    showSnackbarUndoDelete()
                }

            }
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
            }
        }
    }

    private fun setupList(activity: FragmentActivity, savedInstanceState: Bundle?) {

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerAdapter = MainRecyclerViewAdapter()

        savedInstanceState?.let {
            recyclerAdapter.setSelectionSequencesToDelete(savedInstanceState.getParcelable(SELECTION_SEQUENCES_TO_DELETE_KEY))
        }

        recyclerView.adapter = recyclerAdapter
        recyclerSelectionTrackerItemKeyProvider = MainListItemKeyProvider(recyclerAdapter.getData())

        recyclerSelectionTracker = SelectionTracker.Builder<String>(SELECTION_TRACKER_ID,
                recyclerView, recyclerSelectionTrackerItemKeyProvider, MainListItemLookup(recyclerView),
                StorageStrategy.createStringStorage()).build()

        recyclerSelectionTracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {

            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (recyclerSelectionTracker.hasSelection() && !primaryActionModeController.isInMode() && activity is AppCompatActivity) {
                    primaryActionModeController.startActionMode(this@MainListFragment.activity as AppCompatActivity, this@MainListFragment,
                            R.menu.menu_action_mode, getString(R.string.action_mode_title, recyclerSelectionTracker.selection.size()))
                } else if (!recyclerSelectionTracker.hasSelection() && primaryActionModeController.isInMode()) {
                    primaryActionModeController.finishActionMode()
                } else {
                    primaryActionModeController.setTitle(getString(R.string.action_mode_title, recyclerSelectionTracker.selection.size()))
                }

            }
        })

        recyclerAdapter.setSelectionTracker(recyclerSelectionTracker)
        val customAnimator = object : CustomItemAnimator() {}
        customAnimator.setOnItemAnimatorListener(object : CustomItemAnimator.OnItemAnimatorListener {

            override fun getNumberOfItemsToRemove(): Int = numberOfItemsToRemoveOrAdd

            override fun onAnimationsFinishedOnItemRemoved() {
                logConsoleVerbose("Main list deleted items: $numberOfItemsToRemoveOrAdd")
                viewModel.refreshMyCryptocurrencyResourceList()
            }

            override fun getNumberOfItemsToAdd(): Int = numberOfItemsToRemoveOrAdd

            override fun onAnimationsFinishedOnItemAdded() {
                logConsoleVerbose("Main list restored after delete items: $numberOfItemsToRemoveOrAdd")
                viewModel.refreshMyCryptocurrencyResourceList()
            }
        })

        customAnimator.supportsChangeAnimations = false
        recyclerView.itemAnimator = customAnimator
        val appBarLayout = activity.findViewById<AppBarLayout>(R.id.app_bar_layout)

        appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    if (verticalOffset == 0 && !swipeRefreshLayout.isEnabled) {
                        swipeRefreshLayout.isEnabled = true
                    }
                    else if (verticalOffset < 0 && swipeRefreshLayout.isEnabled && !swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isEnabled = false
                    }
                })

    }

    private fun subscribeUi(activity: FragmentActivity) {

        viewModel = ViewModelProvider(activity, viewModelFactory)[MainViewModel::class.java]
        binding.viewmodel = viewModel

        viewModel.mediatorLiveDataMyCryptocurrencyResourceList.observe(viewLifecycleOwner, Observer { listResource ->

            logConsoleVerbose("Main list resource status: " + listResource.status.toString())

            if (listResource.status != Status.LOADING || listResource.status == Status.LOADING &&
                    binding.emptyList == null && recyclerAdapter.itemCount == 0) {
                binding.listResource = listResource
                binding.emptyList = listResource.data != null && listResource.data.isEmpty()
            }

            if (viewModel.isSwipeRefreshing && !swipeRefreshLayout.isRefreshing) {
                snackbarUnableRefresh?.dismiss()
                snackbarUndoDelete?.dismiss()

                swipeRefreshLayout.isRefreshing = true
                spinnerFiatCode.isEnabled = false

                if (viewModel.newSelectedFiatCurrencyCode != null)
                    spinnerFiatCode.setSelection(
                            getFiatCurrencyPosition(viewModel.newSelectedFiatCurrencyCode), false
                    )
            }

            listResource.data?.let {
                recyclerAdapter.setData(it)
                recyclerSelectionTrackerItemKeyProvider.updataData(it)

                if (listResource.status == Status.ERROR) {
                    viewModel.isSwipeRefreshing = false
                    swipeRefreshLayout.isRefreshing = false
                    spinnerFiatCode.isEnabled = true

                    snackbarUnableRefresh = this.view!!.showSnackbar(R.string.unable_refresh) {
                        onActionButtonClick {
                            viewModel.isSwipeRefreshing = true
                            swipeRefreshLayout.isRefreshing = true
                            spinnerFiatCode.isEnabled = false
                        }
                        onDismissedAction {
                            if (viewModel.newSelectedFiatCurrencyCode != null) {
                                spinnerFiatCode.setSelection(
                                        getFiatCurrencyPosition(viewModel.newSelectedFiatCurrencyCode)
                                )
                            } else viewModel.retry()
                        }
                        onDismissedAnyOfEvents(
                                listOf(Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE,
                                        Snackbar.Callback.DISMISS_EVENT_SWIPE)) {
                            viewModel.newSelectedFiatCurrencyCode = null
                        }
                    }

                    if (viewModel.newSelectedFiatCurrencyCode != null) {
                        spinnerFiatCode.setSelection(getFiatCurrencyPosition(viewModel.getCurrentFiatCurrencyCode()))
                    }
                } else if (listResource.status == Status.SUCCESS_DB ||
                        listResource.status == Status.SUCCESS_NETWORK) {

                    if (viewModel.newSelectedFiatCurrencyCode != null) {
                        viewModel.setNewCurrentFiatCurrencyCode(viewModel.newSelectedFiatCurrencyCode.toString())
                        viewModel.newSelectedFiatCurrencyCode = null
                    }

                    viewModel.isSwipeRefreshing = false
                    swipeRefreshLayout.isRefreshing = false
                    spinnerFiatCode.isEnabled = true
                }
            }

        })
    }

    private fun getFiatCurrencyPosition(newFiatCurrencyCode: String?): Int {
        return resources.getStringArray(R.array.fiat_currency_code_array).indexOf(newFiatCurrencyCode)
    }

    private fun showSnackbarUndoDelete() {

        snackbarUndoDelete = this.view?.showSnackbar(getString(R.string.deleted, deletedItems!!.size), Snackbar.LENGTH_LONG) {
            swipeRefreshLayout.isEnabled = false

            onActionButtonClick(R.string.undo) {

                deletedItems?.let { deletedItems ->

                    numberOfItemsToRemoveOrAdd = deletedItems.size
                    recyclerAdapter.restoreDeletedItems()
                    recyclerSelectionTrackerItemKeyProvider.updataData(recyclerAdapter.getData())
                    viewModel.restoreCryptocurrencyList(deletedItems)
                }

                clean()
            }

            onDismissedAnyOfEvents(
                    listOf(Snackbar.Callback.DISMISS_EVENT_TIMEOUT,
                            Snackbar.Callback.DISMISS_EVENT_SWIPE)) {
                clean()
            }
        }
    }

    private fun clean() {
        deletedItems = null
        recyclerAdapter.setSelectionSequencesToDelete(null)
        swipeRefreshLayout.isEnabled = true
    }

}