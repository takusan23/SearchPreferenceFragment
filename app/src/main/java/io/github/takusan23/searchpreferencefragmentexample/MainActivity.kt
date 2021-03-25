package io.github.takusan23.searchpreferencefragmentexample

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.takusan23.searchpreferencefragment.SearchPreferenceChildFragment
import io.github.takusan23.searchpreferencefragment.SearchPreferenceFragment
import io.github.takusan23.searchpreferencefragmentexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        // オリジナル版と継承して作ったバージョンを切り替えるなど
        viewBinding.activityMainBottomNavigationBar.setOnNavigationItemSelectedListener {
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
                // ライセンス画面へ
                if (preference!!.key == "setting_kono_app") {
                    startActivity(Intent(this, LicenseActivity::class.java))
                }
            }

            fragment.onChildPreferenceFragmentCompatClickFunc = { preference ->
                Toast.makeText(this, preference?.title, Toast.LENGTH_SHORT).show()
            }

            // Preference取得
            fragment.preferenceListCallBack = { list ->
                list.forEach { println(it) }
            }

            supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_host_frame_layout, fragment).commit()
            true
        }
        viewBinding.activityMainBottomNavigationBar.selectedItemId = R.id.activity_main_menu_default
    }

}