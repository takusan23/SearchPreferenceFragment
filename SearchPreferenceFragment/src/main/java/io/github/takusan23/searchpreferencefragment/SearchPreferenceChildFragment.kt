package io.github.takusan23.searchpreferencefragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.postDelayed
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.preference.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * [SearchPreferenceFragment]に置くFragment。設定項目一覧はこのFragmentで表示している
 * */
class SearchPreferenceChildFragment : PreferenceFragmentCompat() {

    /** 最初に表示されるPreferenceのID */
    private val defaultPreferenceResId by lazy { arguments?.getInt(PREFERENCE_XML_RESOURCE_ID) }

    /** 該当する設定項目の色をつける */
    private val highLightColor by lazy { arguments?.getInt(SEARCH_SCROLL_HIGH_LIGHT_COLOR, Color.parseColor("#80ffff00")) ?: Color.parseColor("#ffff00") }

    /** 該当する設定項目の色をつける間隔。 */
    private val delayTime by lazy { arguments?.getLong(SEARCH_PREFERENCE_BACKGROUND_REPEAT_DELAY, 500L) ?: 500L }

    /** 該当する設定項目の色をつけるの繰り返し回数 */
    private val repeatCount by lazy { arguments?.getInt(SEARCH_PREFERENCE_REPEAT_COUNT, 9) ?: 9 }

    companion object {
        /** 最初に表示するPreferenceのリソースID */
        const val PREFERENCE_XML_RESOURCE_ID = "preference_xml_resource_id"

        /** [SEARCH_SCROLL_HIGH_LIGHT_COLOR]の切り替えを何回行うか。奇数じゃないとだめかも */
        var SEARCH_PREFERENCE_REPEAT_COUNT = "repeat_count"

        /** [SEARCH_SCROLL_HIGH_LIGHT_COLOR]の切り替えの間隔 */
        var SEARCH_PREFERENCE_BACKGROUND_REPEAT_DELAY = "repeat_delay"

        /** 検索結果を押したときに設定項目をハイライトさせる色 */
        var SEARCH_SCROLL_HIGH_LIGHT_COLOR = "high_light_color"
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
             * スクロールを実行する。Fragmentのライフサイクルに合わせて書かないとエラーが出ちゃうから無理やり遅延させたりする必要があったりしたけど、
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
                        // スクロール
                        scroll(getAllPreference(preferenceScreen), listView, scrollKey, scrollTitle)
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

        // 表示中のPreferenceを配列にして持っておく
        val preferenceList = getAllPreference(preferenceScreen)

        // 検索結果Preferenceを押したときのコールバック的なLiveData
        viewModel.changePreferenceScreen.observe(viewLifecycleOwner) { result ->
            // 同じリソースIDなら
            if (result.resId == defaultPreferenceResId) {
                scroll(preferenceList, listView, result.preference.key, result.preferenceTitle)
            }
        }

    }

    private fun scroll(preferenceList: ArrayList<Preference>, listView: RecyclerView, preferenceKey: String?, preferenceTitle: String?) {
        // スクロール
        val pos = preferenceList.indexOfFirst { preference ->
            if (preferenceKey != null) {
                preference.key == preferenceKey
            } else {
                Log.e(javaClass.simpleName, "Preferenceにkeyが設定されていなかったため、同じタイトルの設定項目へスクロールします。")
                preference.title == preferenceTitle
            }
        }
        // なんか遅延させると動く
        listView.postDelayed(500) {
            /**
             * LayoutManager経由でスクロールする。RecyclerViewにもスクロール関数が生えてるけど、なんか実装空っぽだった
             *
             * なので、[PreferenceFragmentCompat.scrollToPreference]も、RecyclerViewの実装空っぽスクロール関数を呼んでいるため動かない。
             * */
            val visibleItem = (listView.layoutManager as LinearLayoutManager).let {
                // 真ん中へスクロールさせたいので
                (it.findLastCompletelyVisibleItemPosition() - it.findFirstVisibleItemPosition()) / 2
            }
            listView.smoothScrollToPosition(pos + visibleItem)
            // リピートさせる
            var time = delayTime
            repeat(repeatCount) { count ->
                // コルーチンだともっときれいにかけそう
                listView.postDelayed(time) {
                    // RecyclerViewの指定した位置のViewを取得して背景色を変更
                    (listView.layoutManager as LinearLayoutManager).findViewByPosition(pos)?.apply {
                        background = if (background == null) ColorDrawable(highLightColor) else null
                        // 最後は強制null
                        if (count == 8) {
                            background = null
                        }
                    }
                }
                time += delayTime
            }
        }
    }

    /** Preferenceをすべて見つけて配列にして返す関数 */
    private fun getAllPreference(preferenceScreen: PreferenceScreen): ArrayList<Preference> {
        // 設定項目を配列にしまう
        return arrayListOf<Preference>().apply {
            // 再帰的に呼び出す
            fun getChildPreference(group: PreferenceGroup) {
                val preferenceCount = group.preferenceCount
                repeat(preferenceCount) { index ->
                    val preference = group.getPreference(index)
                    // 表示中のみインデックス化
                    if (preference.isVisible) {
                        add(preference)
                    }
                    if (preference is PreferenceGroup) {
                        getChildPreference(preference)
                    }
                }
            }
            getChildPreference(preferenceScreen)
        }
    }

}