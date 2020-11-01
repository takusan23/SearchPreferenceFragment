package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.LinearLayoutManager

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
     * その他にも検索結果押したときもandroid:fragment指定時はこれが使われる
     * */
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.fragment != null) {
            // なんか文字列からFragment作ってる
            val fragment = parentFragmentManager.fragmentFactory.instantiate(requireActivity().classLoader, preference.fragment)
            // スクロール先のPreferenceの名前を入れるなど
            fragment.arguments = Bundle().apply {
                putString("scroll_key", preference.key)
                putString("scroll_title", preference.title?.toString())
            }
            (requireParentFragment() as SearchPreferenceFragment).setFragment(fragment)
            /**
             * スクロールを実行する。本来は生成したFragmentでなにか処理を行う場合はクソ面倒なことになりそうだけど、
             * ライフサイクルライブラリのおかげでこんな書き方ができる。くっっっそ便利
             * */
            fragment.lifecycle.addObserver(object : LifecycleObserver {

                // ライフサイクルがonStart()のときに関数を自動で呼ぶ
                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun preferenceScroll() {
                    /**
                     * もし遷移先Fragmentが[PreferenceFragmentCompat]なら該当Preferenceまでスクロールを実行させる
                     * */
                    (fragment as? PreferenceFragmentCompat)?.apply {
                        val scrollTitle = arguments?.getString("scroll_title")
                        val scrollKey = arguments?.getString("scroll_key")

                        val pos = getAllPreference(preferenceScreen).indexOfFirst { preference ->
                            if (scrollKey != null) {
                                preference.key == scrollKey
                            } else {
                                Log.e(javaClass.simpleName, "Preferenceにkeyが設定されていなかったため、同じタイトルの設定項目へスクロールします。")
                                preference.title == scrollTitle
                            }
                        }
                        // なんか遅延させると動く
                        view?.postDelayed({
                            println("はい $pos $isAdded")
                            if (isAdded) {
                                /**
                                 * LayoutManager経由でスクロールする。RecyclerViewにもスクロール関数が生えてるけど、なんか実装空っぽだった
                                 *
                                 * なので、[PreferenceFragmentCompat.scrollToPreference]も、RecyclerViewの実装空っぽスクロール関数を呼んでいるため動かない。
                                 * */
                                (listView.layoutManager as LinearLayoutManager).apply {
                                    scrollToPositionWithOffset(pos, 0)
                                }
                            }
                        }, 500)

                    }

                }
            })
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
            // 同じリソースIDなら
            if (result.resId == defaultPreferenceResId) {
                // スクロール
                val pos = getAllPreference(preferenceScreen).indexOfFirst { preference ->
                    if (result.preference.key != null) {
                        preference.key == result.preference.key
                    } else {
                        Log.e(javaClass.simpleName, "Preferenceにkeyが設定されていなかったため、同じタイトルの設定項目へスクロールします。")
                        preference.title == result.preferenceTitle
                    }
                }
                // なんか遅延させると動く
                view.postDelayed({
                    if (isAdded) {
                        /**
                         * LayoutManager経由でスクロールする。RecyclerViewにもスクロール関数が生えてるけど、なんか実装空っぽだった
                         *
                         * なので、[PreferenceFragmentCompat.scrollToPreference]も、RecyclerViewの実装空っぽスクロール関数を呼んでいるため動かない。
                         * */
                        (listView.layoutManager as LinearLayoutManager).apply {
                            scrollToPositionWithOffset(pos, 0)
                        }
                    }
                }, 500)
            }
        }

    }

    /** Preferenceをすべて見つけて配列にして返す関数 */
    private fun getAllPreference(preferenceScreen: PreferenceScreen): ArrayList<Preference> {
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