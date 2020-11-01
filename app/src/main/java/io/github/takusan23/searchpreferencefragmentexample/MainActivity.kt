package io.github.takusan23.searchpreferencefragmentexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.takusan23.searchpreferencefragment.SearchPreferenceChildFragment
import io.github.takusan23.searchpreferencefragment.SearchPreferenceFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // オリジナル版と継承して作ったバージョンを切り替えるなど
        activity_main_bottom_navigation_bar.setOnNavigationItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.activity_main_menu_default -> {
                    SearchPreferenceFragment()
                }
                else -> {
                    OriginalSearchPreferenceFragment()
                }
            }
            val bundle = Bundle().apply {
                val map = hashMapOf(
                    SubSettingFragment::class.qualifiedName to R.xml.sub_preference
                )
                putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
                putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
            }
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_host_frame_layout, fragment).commit()
            true
        }
        activity_main_bottom_navigation_bar.selectedItemId = R.id.activity_main_menu_default
    }

}