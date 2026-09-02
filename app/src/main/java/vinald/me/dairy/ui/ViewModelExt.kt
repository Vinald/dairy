package vinald.me.dairy.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import vinald.me.dairy.DiaryApplication
import vinald.me.dairy.di.AppContainer

/** Access the manual DI graph from a [androidx.lifecycle.ViewModel] factory. */
val CreationExtras.appContainer: AppContainer
    get() = (this[APPLICATION_KEY] as DiaryApplication).container
