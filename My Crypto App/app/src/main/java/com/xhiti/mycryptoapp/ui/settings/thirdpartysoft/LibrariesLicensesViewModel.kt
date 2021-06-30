package com.baruckis.kriptofolio.ui.settings.thirdpartysoft

import androidx.lifecycle.ViewModel
import com.baruckis.kriptofolio.db.LibraryLicenseInfo
import com.baruckis.kriptofolio.repository.LicensesRepository
import com.baruckis.kriptofolio.utilities.localization.StringsLocalization
import javax.inject.Inject

class LibrariesLicensesViewModel @Inject constructor(
        licensesRepository: LicensesRepository) : ViewModel() {

    val librariesLicensesData: List<LibraryLicenseInfo> = licensesRepository.getLibrariesLicensesList()

    val noBrowserFoundMessage: String = licensesRepository.getNoBrowserFoundMessage()

    val stringsLocalization: StringsLocalization = licensesRepository.getStringsLocalization()
}