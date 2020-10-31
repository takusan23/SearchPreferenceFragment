package io.github.takusan23.searchpreferencefragmentexample

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SubSettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sub_preference, rootKey)
    }
}