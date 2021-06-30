package com.baruckis.kriptofolio.ui.mainlist

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.ui.common.BaseActivity
import com.baruckis.kriptofolio.ui.settings.SettingsActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_list.*
import javax.inject.Inject

class MainActivity : BaseActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        //supportActionBar?.subtitle = getString(R.string.app_subtitle)
        subscribeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    private fun subscribeUi() {

        viewModel.liveDataTotalHoldingsValueOnDateText.observe(this, Observer<String> { data ->
            val txt = StringBuilder(getString(R.string.string_total_value_holdings))
            if (data.toString().isNotEmpty())
                txt.append(getString(R.string.string_total_value_on_date_time, data))
            textview_total_value_on_date_time.text = txt
        })

        viewModel.liveDataTotalHoldingsValueFiat24hText.observe(this, Observer<SpannableString> { data ->
            textview_total_value_change_24h.text = data
        })

        viewModel.liveDataTotalHoldingsValueFiatText.observe(this, Observer<String> { data ->
            textview_fiat_value.text = data
            textview_fiat_value.requestLayout()
        })

        viewModel.liveDataTotalHoldingsValueCryptoText.observe(this, Observer<String> { data ->
            textview_crypto_value.text = data
            textview_crypto_value.requestLayout()
        })

        viewModel.liveDataCurrentFiatCurrencySign.observe(this, Observer<String> { data ->
            text_column_coin_fiat_btc_price.text = StringBuilder(getString(R.string.string_column_coin_fiat_price_amount, data, data))
            text_column_coin_change_24h_1h_7d.text = StringBuilder(getString(R.string.string_column_coin_change_24h_1h_7d, data))
        })

    }
}
