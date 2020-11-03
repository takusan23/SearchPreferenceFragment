package io.github.takusan23.searchpreferencefragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * [SearchPreferenceFragment]に置くFragment。設定項目一覧はこのFragmentで表示している
 * */
class SearchPreferenceChildFragment : PreferenceFragmentCompat() {

    /** 最初に表示されるPreferenceのID */
    private val defaultPreferenceResId by lazy { arguments?.getInt(PREFERENCE_XML_RESOURCE_ID) }

    /** 該当する設定項目の色をつける */
    private val highLightColor by lazy { arguments?.getInt(SEARCH_SCROLL_HIGH_LIGHT_COLOR, Color.parseColor("#80ffff00")) ?: Color.parseColor("#80ffff00") }

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
     * このFragment([SearchPreferenceChildFragment])を置いてるFragment。
     * */
    private val searchPreferenceFragment by lazy { (requireParentFragment() as SearchPreferenceFragment) }

    /**
     * Fragment切り替えに失敗するので手直し
     * その他にも検索結果押したときもandroid:fragment指定時はこれが使われる
     * */
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        // 高階関数を呼ぶ
        searchPreferenceFragment.onPreferenceClickFunc?.invoke(preference)
        if (preference?.fragment != null) {

            // なんか文字列からFragment作ってる
            val fragment = createFragment(preference.fragment)
            // スクロール先のPreferenceの名前を入れるなど
            fragment.arguments = Bundle().apply {
                putString("scroll_key", preference.key)
                putString("scroll_title", preference.title?.toString())
            }
            // Fragment設置
            searchPreferenceFragment.setFragment(fragment)

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

                        // クリックイベントを上書きするか
                        val clickFunc = (requireParentFragment() as? SearchPreferenceFragment)?.onChildPreferenceFragmentCompatClickFunc
                        if (clickFunc != null) {
                            getAllPreference(preferenceScreen).forEach { preference ->
                                preference.setOnPreferenceClickListener {
                                    // なお、第3階層目からエラーが出るので直す
                                    val fragmentPath = preference.fragment
                                    if (fragmentPath == null) {
                                        // Fragment未設定時のみ
                                        clickFunc(preference)
                                    }
                                    false
                                }
                            }
                        }
                        preferenceFragmentFix(fragment)
                    }
                }
            })
            return true
        }
        return false
    }

    /**
     * [PreferenceFragmentCompat.onPreferenceTreeClick]のFragment置き換えがうまく動かないためそれを直す
     *
     * - なんで動かないの？
     *      - Preferenceを押すと、[PreferenceFragmentCompat.onPreferenceTreeClick]が呼ばれる
     *      - 親のFragment もしくは 親のActivity に [PreferenceFragmentCompat.OnPreferenceStartFragmentCallback] が実装されていれば、onPreferenceTreeClick()が呼ばれる
     *          - Activityの方なら動くが、Fragmentに関しては常にnullを返してるためFragmentに実装しても無駄
     *      - わざわざActivityに書かせるのもあれなので書いた
     * */
    private fun preferenceFragmentFix(preferenceFragmentCompat: PreferenceFragmentCompat) {
        preferenceFragmentCompat.lifecycle.addObserver(object : LifecycleObserver {
            // Fragmentがstartのときに呼ばれる
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                getAllPreference(preferenceFragmentCompat.preferenceScreen).forEach { preference ->
                    // Fragmentが設定されている場合は、処理を変える
                    val fragmentPath = preference.fragment
                    if (fragmentPath != null) {
                        // そのままの実装でFragmentを置き換えるともれなくエラーが出るので書き換える
                        val fragment = createFragment(fragmentPath)
                        // 再帰的に呼ぶ
                        if (fragment is PreferenceFragmentCompat) {
                            preferenceFragmentFix(fragment)
                        }
                        // onPreferenceTreeClickを呼ばないため
                        preference.fragment = null
                        // fragmentが設定されているときのみクリックイベントを書き換え
                        preference.setOnPreferenceClickListener {
                            searchPreferenceFragment.setFragment(fragment)
                            false
                        }
                    }
                }
            }

        })
    }

    /** 文字列からFragmentを生成する */
    private fun createFragment(path: String): Fragment {
        // なんか文字列からFragment作ってる
        return parentFragmentManager.fragmentFactory.instantiate(requireActivity().classLoader, path)
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
        if (pos == RecyclerView.NO_POSITION) {
            return
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