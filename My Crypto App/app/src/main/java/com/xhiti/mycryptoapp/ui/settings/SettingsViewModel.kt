package com.baruckis.kriptofolio.ui.settings

import androidx.lifecycle.ViewModel
import com.baruckis.kriptofolio.repository.CryptocurrencyRepository
import com.baruckis.kriptofolio.repository.LicensesRepository
import com.baruckis.kriptofolio.utilities.localization.StringsLocalization
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
        cryptocurrencyRepository: CryptocurrencyRepository,
        licensesRepository: LicensesRepository,
        val stringsLocalization: StringsLocalization) : ViewModel() {

    val currentLanguage = cryptocurrencyRepository.getCurrentLanguage()

    val currentFiatCurrencyCode = cryptocurrencyRepository.getCurrentFiatCurrencyCode()

    val currentDateFormat = cryptocurrencyRepository.getCurrentDateFormat()

    val appLicenseData: String = licensesRepository.getAppLicense()

    val noBrowserFoundMessage: String = licensesRepository.getNoBrowserFoundMessage()
}