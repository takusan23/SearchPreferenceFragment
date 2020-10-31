package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen

/**
 * [SearchPreferenceFragment]に置くFragment。設定項目一覧はこのFragmentで表示している
 * */
class SearchPreferenceChildFragment : PreferenceFragmentCompat() {

    companion object {
        const val PREFERENCE_XML_RESOURCE_ID = "preference_xml_resource_id"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceXml = arguments?.getInt(PREFERENCE_XML_RESOURCE_ID) ?: return
        setPreferencesFromResource(preferenceXml, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by viewModels<SearchPreferenceViewModel>({ requireParentFragment() })

        // 設定項目を配列にしまう
        val preferenceListScreen = arrayListOf<Preference>().apply {
            // 再帰的に呼び出す
            fun getChildPreference(group: PreferenceGroup) {
                val preferenceCount = group.preferenceCount
                repeat(preferenceCount) { index ->
                    val preference = group.getPreference(index)
                    add(preference)
                    if (preference is PreferenceGroup) {
                        getChildPreference(preference)
                    }
                }
            }
            getChildPreference(preferenceScreen)
        }

        // 設定項目配列を取得
        val preferenceList = viewModel.preferenceList.value

        viewModel.searchEditTextChange.observe(viewLifecycleOwner) { editText ->

            preferenceScreen.removeAll()

            // PreferenceCategory以外を非表示
            // 部分一致した設定のみを表示する。なお、所属してるPreferenceCategoryが非表示だと出ない
            val prefList = preferenceList?.filter { preferenceData -> preferenceData.preference.title?.contains(editText) == true }
            when {
                prefList?.isNotEmpty() == true -> {
                    // 追加する
                    prefList.forEach { preference -> preferenceScreen.addPreference(preference.preference) }
                }
                editText.isEmpty() -> {
                    // 未入力なら初期状態に
                    preferenceListScreen.forEach { preference -> preferenceScreen.addPreference(preference) }
                }
            }

        }

    }

}