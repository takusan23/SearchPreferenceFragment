package io.github.takusan23.searchpreferencefragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SearchPreferenceViewModelFactory(private val application: Application, private val preferenceXmlResourceId: Int, private val preferenceFragmentMap: HashMap<String?, Int>) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchPreferenceViewModel(application, preferenceXmlResourceId, preferenceFragmentMap) as T
    }

}