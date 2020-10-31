package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
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
        if (defaultPreferenceResId != null) {
            setPreferencesFromResource(defaultPreferenceResId!!, rootKey)
        }
    }

    /**
     * Fragment切り替えに失敗するので手直し
     * */
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.fragment != null) {
            val fragment = parentFragmentManager.fragmentFactory.instantiate(requireActivity().classLoader, preference.fragment)
            (requireParentFragment() as SearchPreferenceFragment).setFragment(fragment)
            return true
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel取得
        val viewModel by viewModels<SearchPreferenceViewModel>({ requireParentFragment() })

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
            // nullの時は同じFragmentに有るってことで
            if (result.resId != defaultPreferenceResId) {
                // PreferenceFragment設置
                val preferenceFragment = SearchPreferenceChildFragment()
                preferenceFragment.arguments = Bundle().apply {
                    putInt(PREFERENCE_XML_RESOURCE_ID, result.resId)
                }
                (requireParentFragment() as SearchPreferenceFragment).setFragment(preferenceFragment, result.resId.toString())
            }
        }

    }

    /** Preferenceをすべて見つけて配列にして返す関数 */
    private fun getAllPreference(): ArrayList<Preference> {
        // 設定項目を配列にしまう
        val preferenceList = arrayListOf<Preference>().apply {
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
        return preferenceList
    }

}