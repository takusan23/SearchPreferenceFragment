package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

/**
 * [SearchPreferenceFragment]に置くFragment。設定項目一覧はこのFragmentで表示している
 * */
class SearchPreferenceChildFragment :PreferenceFragmentCompat(){

    companion object {
        const val PREFERENCE_XML_RESOURCE_ID = "preference_xml_resource_id"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceXml = arguments?.getInt(PREFERENCE_XML_RESOURCE_ID) ?: return
        setPreferencesFromResource(preferenceXml, rootKey)
    }

}