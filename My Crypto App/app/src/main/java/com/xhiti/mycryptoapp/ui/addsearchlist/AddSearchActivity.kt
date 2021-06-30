package com.baruckis.kriptofolio.ui.addsearchlist

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.databinding.ActivityAddSearchBinding
import com.baruckis.kriptofolio.db.Cryptocurrency
import com.baruckis.kriptofolio.db.MyCryptocurrency
import com.baruckis.kriptofolio.dependencyinjection.Injectable
import com.baruckis.kriptofolio.ui.addsearchlist.CryptocurrencyAmountDialog.Companion.DIALOG_CRYPTOCURRENCY_AMOUNT_TAG
import com.baruckis.kriptofolio.ui.common.BaseActivity
import com.baruckis.kriptofolio.ui.common.RetryCallback
import com.baruckis.kriptofolio.utilities.*
import com.baruckis.kriptofolio.utilities.localization.StringsLocalization
import com.baruckis.kriptofolio.vo.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_add_search.*
import kotlinx.coroutines.*
import javax.inject.Inject

class AddSearchActivity : BaseActivity(), Injectable, CryptocurrencyAmountDialog.CryptocurrencyAmountDialogListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: AddSearchViewModel
    lateinit var binding: ActivityAddSearchBinding
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var listAdapter: AddSearchListAdapter
    private var snackbar: Snackbar? = null
    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var searchQuery: String? = null

    @Inject
    lateinit var stringsLocalization: StringsLocalization

    companion object {
        const val EXTRA_ADD_TASK_DESCRIPTION = "add_task"
        private const val SEARCH_KEY = "search"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_KEY, null)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_search)

        setSupportActionBar(binding.toolbar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listAdapter = AddSearchListAdapter(this) { cryptocurrency -> cryptocurrencyClick(cryptocurrency) }
        listview_activity_add_search.adapter = listAdapter

        swipeRefreshLayout = swiperefresh_activity_add_search

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorForSwipeRefreshProgress1,
                R.color.colorForSwipeRefreshProgress2,
                R.color.colorForSwipeRefreshProgress3)

        swipeRefreshLayout.setOnRefreshListener {
            snackbar?.dismiss()

            enableSearchMenuItem(false)

            viewModel.isSwipeRefreshing = true
            viewModel.retry()
        }

        binding.myRetryCallback = object : RetryCallback {
            override fun retry() {
                this@AddSearchActivity.retry()
            }
        }

        subscribeUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        searchQuery = searchView?.query.toString()
        outState.putString(SEARCH_KEY, searchQuery)
    }

    override fun onDestroy() {
        textChangeDelayJob?.cancel()
        super.onDestroy()
    }

    private fun retry() {
        snackbar?.dismiss()
        enableSearchMenuItem(false)
        viewModel.retry()
    }

    override fun onCryptocurrencyAmountDialogConfirmButtonClick(cryptocurrencyAmountDialog: CryptocurrencyAmountDialog) {
        val amount = cryptocurrencyAmountDialog.getAmount()
        viewModel.selectedCryptocurrency?.let { myCryptocurrency ->
            myCryptocurrency.amount = amount
            myCryptocurrency.amountFiat =
                    getAmountFiatCounted(amount, myCryptocurrency.cryptoData.priceFiat)
            myCryptocurrency.amountFiatChange24h =
                    getAmountFiatChange24hCounted(myCryptocurrency.amountFiat, myCryptocurrency.cryptoData.pricePercentChange24h)
        }

        val result = Intent()
        result.putExtra(EXTRA_ADD_TASK_DESCRIPTION, viewModel.selectedCryptocurrency)
        setResult(Activity.RESULT_OK, result)

        viewModel.selectedCryptocurrency = null
        cryptocurrencyAmountDialog.dismiss()
        finish()
    }

    override fun onCryptocurrencyAmountDialogCancel() {
        viewModel.selectedCryptocurrency = null
    }

    private fun cryptocurrencyClick(cryptocurrency: Cryptocurrency) {
        val cryptocurrencyAmountDialog =
                CryptocurrencyAmountDialog.newInstance(
                        title = String.format(getString(R.string.dialog_cryptocurrency_amount_title), cryptocurrency.name),
                        hint = getString(R.string.dialog_cryptocurrency_amount_hint),
                        confirmButton = getString(R.string.dialog_cryptocurrency_amount_confirm_button),
                        cancelButton = getString(R.string.dialog_cryptocurrency_amount_cancel_button),
                        error = getString(R.string.dialog_cryptocurrency_amount_error))

        viewModel.selectedCryptocurrency = MyCryptocurrency(cryptocurrency.id, cryptocurrency)

        cryptocurrencyAmountDialog.show(supportFragmentManager, DIALOG_CRYPTOCURRENCY_AMOUNT_TAG)
    }

    private fun subscribeUi() {

        viewModel = ViewModelProvider(this, viewModelFactory)[AddSearchViewModel::class.java]
        viewModel.mediatorLiveDataCryptocurrencyResourceList.observe(this, Observer { listResource ->

            if (listResource.status != Status.LOADING || listAdapter.isEmpty) {
                binding.myListResource = listResource
                enableSearchMenuItem(false)
            }

            if (viewModel.isSwipeRefreshing && !swipeRefreshLayout.isRefreshing) {
                showSwipeRefreshing(true)
                enableSearchMenuItem(false)
            }

            listResource.data?.let {
                listAdapter.setData(it)

                viewModel.lastUpdatedOnDate = if (!it.isEmpty())
                    formatDate(it.first().lastFetchedDate,
                            viewModel.getCurrentDateFormat(), viewModel.getCurrentTimeFormat(),
                            stringsLocalization.getString(R.string.time_format_am),
                            stringsLocalization.getString(R.string.time_format_pm))
                            else ""
                info_activity_add_search.text = StringBuilder(getString(R.string.string_info_last_updated_on_date_time, viewModel.lastUpdatedOnDate)).toString()

                if (listResource.status == Status.ERROR) {
                    showSwipeRefreshing(false)
                    enableSearchMenuItem(true)

                    snackbar = findViewById<CoordinatorLayout>(R.id.coordinator_add_search).showSnackbar(R.string.unable_refresh) {
                        onActionButtonClick {
                            showSwipeRefreshing(true)
                            enableSearchMenuItem(false)
                        }
                        onDismissedAction { retry() }
                    }

                } else if (listResource.status == Status.SUCCESS_DB || listResource.status == Status.SUCCESS_NETWORK) {
                    showSwipeRefreshing(false)
                    enableSearchMenuItem(true)
                }
            }

        })

    }

    private fun showSwipeRefreshing(isRefreshing: Boolean) {
        viewModel.isSwipeRefreshing = isRefreshing
        swipeRefreshLayout.isRefreshing = isRefreshing
    }

    private fun enableSearchMenuItem(isEnabled: Boolean) {
        searchMenuItem?.isEnabled = isEnabled
        viewModel.isSearchMenuItemEnabled = isEnabled
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_search, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu?.findItem(R.id.search)
        searchMenuItem?.setOnActionExpandListener(searchExpandListener)
        searchMenuItem?.isEnabled = viewModel.isSearchMenuItemEnabled

        searchView = searchMenuItem?.actionView as SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.maxWidth = Integer.MAX_VALUE
        searchView?.setOnQueryTextListener(searchListener)

        if (!searchQuery.isNullOrEmpty()) {
            searchMenuItem?.expandActionView()
            searchView?.setQuery(searchQuery, true)
        }

        return true
    }

    private var textChangeDelayJob: Job? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val searchListener = object : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            query?.let { search(it) }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {

            textChangeDelayJob?.cancel()
            textChangeDelayJob = uiScope.launch() {
                newText?.let {
                    delay(SEARCH_TYPING_DELAY_MILLISECONDS)
                    search(it)
                }
            }

            return true
        }

        private fun search(searchText: String) {
            viewModel.search("%$searchText%").observe(this@AddSearchActivity, Observer {
                if (it == null) return@Observer
                listAdapter.setData(it)
                if (searchMenuItem?.isActionViewExpanded == true) {
                    info_activity_add_search.text = StringBuilder(getString(R.string.string_info_results_of_search, it.size.toString())).toString()
                }
            })
        }
    }

    private val searchExpandListener = object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
            swipeRefreshLayout.isEnabled = false
            info_activity_add_search.text = StringBuilder(getString(R.string.string_info_results_of_search, listAdapter.count.toString())).toString()
            return true
        }

        override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
            textChangeDelayJob?.cancel()
            swipeRefreshLayout.isEnabled = true
            info_activity_add_search.text = StringBuilder(getString(R.string.string_info_last_updated_on_date_time, viewModel.lastUpdatedOnDate)).toString()
            return true
        }

    }

}