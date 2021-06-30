package com.baruckis.kriptofolio.dependencyinjection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baruckis.kriptofolio.ui.addsearchlist.AddSearchViewModel
import com.baruckis.kriptofolio.ui.mainlist.MainViewModel
import com.baruckis.kriptofolio.ui.settings.thirdpartysoft.LibrariesLicensesViewModel
import com.baruckis.kriptofolio.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddSearchViewModel::class)
    abstract fun bindAddSearchViewModel(addSearchViewModel: AddSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LibrariesLicensesViewModel::class)
    abstract fun bindLibrariesLicensesViewModel(librariesLicensesViewModel: LibrariesLicensesViewModel): ViewModel


    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}