package io.github.takusan23.searchpreferencefragmentexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
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
                // 検索対象にするPreferenceのXMLのリソースIDとPreferenceCompatFragmentを指定する。
                val map = hashMapOf(
                    SubSettingFragment::class.qualifiedName to R.xml.sub_preference
                )
                putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
                // 最初に表示するFragment
                putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
            }
            fragment.arguments = bundle

            // FragmentのPreferenceを押したときに呼ばれる高階関数
            fragment.onPreferenceClickFunc = { preference ->
                Toast.makeText(this, preference?.title, Toast.LENGTH_SHORT).show()
            }

            fragment.onChildPreferenceFragmentCompatClickFunc = { preference ->
                Toast.makeText(this, preference?.title, Toast.LENGTH_SHORT).show()
            }

            supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_host_frame_layout, fragment).commit()
            true
        }
        activity_main_bottom_navigation_bar.selectedItemId = R.id.activity_main_menu_default
    }

}