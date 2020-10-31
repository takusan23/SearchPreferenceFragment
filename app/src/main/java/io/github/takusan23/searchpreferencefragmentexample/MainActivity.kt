package io.github.takusan23.searchpreferencefragmentexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.takusan23.searchpreferencefragment.SearchPreferenceChildFragment
import io.github.takusan23.searchpreferencefragment.SearchPreferenceFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchPreferenceFragment = SearchPreferenceFragment()
        val bundle = Bundle().apply {
            putIntArray(SearchPreferenceFragment.PREFERENCE_XML_RESOURCE_LIST, intArrayOf(R.xml.preference))
            putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
        }
        searchPreferenceFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_host_frame_layout,searchPreferenceFragment).commit()

    }
}