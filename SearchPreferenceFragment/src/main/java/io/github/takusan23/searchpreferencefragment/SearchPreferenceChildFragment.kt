package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup

/**
 * [SearchPreferenceFragment]に置くFragment。設定項目一覧はこのFragmentで表示している
 * */
class SearchPreferenceChildFragment : PreferenceFragmentCompat() {

    /** 最初に表示されるPreferenceのID */
    private val defaultPreferenceResId by lazy { arguments?.getInt(PREFERENCE_XML_RESOURCE_ID) }

    companion object {
        /** 最初に表示するPreferenceのリソースID */
        const val PREFERENCE_XML_RESOURCE_ID = "preference_xml_resource_id"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        defaultPreferenceResId?.apply {
            setPreferencesFromResource(this, rootKey)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel取得
        val viewModel by viewModels<SearchPreferenceViewModel>({ requireParentFragment() })

        // すべての設定項目配列を取得
        val preferenceList = viewModel.preferenceList.value

        // SearchPreferenceFragmentにあるEditTextのLiveData
        viewModel.searchEditTextChange.observe(viewLifecycleOwner) { editText ->
            // 初期化
            preferenceScreen.removeAll()
            if (editText.isNotEmpty()) {
                // なんかしら入力している場合
                // 部分一致した設定のみを表示する。なお、所属してるPreferenceCategoryが非表示だと出ない
                val prefList = viewModel.findPreference(editText)
                // 追加する
                prefList?.forEach { preference -> preferenceScreen.addPreference(preference.preference) }
            } else {
                // 未入力なら初期状態に
                if (defaultPreferenceResId != null) {
                    addPreferencesFromResource(defaultPreferenceResId!!)
                }
            }
        }

        // 検索結果Preferenceを押したときのコールバック的なLiveData
        viewModel.changePreferenceScreen.observe(viewLifecycleOwner) { result ->
            // 設定項目消し飛ばす
            preferenceScreen.removeAll()
            // Preferenceを切り替える
            addPreferencesFromResource(result.resId)
            // 該当する項目までスクロール
            // TODO: 2020/10/31 できてない
            scrollToPreference(result.preferenceId)
        }

    }

}