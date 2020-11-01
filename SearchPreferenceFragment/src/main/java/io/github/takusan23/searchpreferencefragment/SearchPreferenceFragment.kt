package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_search_preference_fragment.*

/**
 * 検索可能PreferenceFragment。このFragmentに[androidx.preference.PreferenceFragmentCompat]を置く
 *
 * なお、このFragmentを置く際に、[setArguments]で以下の値をセットしておく必要があります。
 * - [SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP]
 *      - String to Int のHashMap。
 *      - Fragmentのパスをキーにして検索対象にするXMLのリソースIDのHashMap
 * - [SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID]
 *      - Int
 *      - 設定画面として最初に表示する項目。
 *
 * 設置例
 * ```
 * val bundle = Bundle().apply {
 *     val map = hashMapOf(
 *         SubSettingFragment::class.qualifiedName to R.xml.sub_preference
 *     )
 *     putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
 *     putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
 * }
 * fragment.arguments = bundle
 * ```
 *
 * ### UIを変更したい場合はこのクラスを継承して、
 * - [onCreateView]で指定するレイアウトに[EditText]とFragmentを置くView（[android.widget.FrameLayout]がいいらしい？）を最低限入れてね
 * - 指定できたら、[init]関数を呼んでください。第二引数以降は、レイアウトに置いたViewを指定してください。
 *
 * ### 仕組み
 * 置いておいた[SearchPreferenceChildFragment]にEditTextのテキスト変更通知をLiveDataを利用して飛ばして、Preferenceを動的に追加しています。
 *
 * なお、PreferenceのFragment属性により画面推移が実行された状態の場合で検索を実行すると、Fragmentは最初のFragmentへ戻す（[androidx.fragment.app.FragmentManager.popBackStack]）を実行します
 *
 * */
open class SearchPreferenceFragment : Fragment() {

    companion object {
        /**
         * FragmentのパスとPreferenceのリソースIDのHashMap。なお、階層をつけない場合は空っぽでいい。
         *
         * 例：
         * ```
         * val map = hashMapOf(
         *     SubSettingFragment::class.qualifiedName to R.xml.sub_preference
         * )
         * putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
         * ```
         * */
        const val PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP = "preference_xml_fragment_hash_map"

        /**
         * 最初に表示していた[SearchPreferenceChildFragment]へ戻す際に使う。基本使うことはない
         * */
        const val CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG = "child_search_fragment_first"
    }

    /** 検索の中身などを保持するViewModel */
    private lateinit var viewModel: SearchPreferenceViewModel

    private var fragmentHostLayout: View? = null

    /** レイアウト指定 */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_preference_fragment, container, false)
    }

    /** メイン処理 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState, search_fragment_input, search_fragment_host_frame_layout)
    }

    /**
     * [onViewCreated]あたりでこの関数を呼んでください。Fragment設置やViewModelの用意をします。
     * @param savedInstanceState 画面回転時かどうかの判断で利用。[onViewCreated]の第二引数のやつ
     * @param editText 検索時に利用するEditText
     * @param fragmentHostLayout Fragmentを置くView
     * */
    fun init(savedInstanceState: Bundle?, editText: EditText, fragmentHostLayout: View) {
        // もらう
        val preferenceXmlId = arguments?.getInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID)
        val preferenceFragmentMap = arguments?.getSerializable(PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP) as? HashMap<String?, Int>

        // 必須項目があるか
        if (preferenceXmlId == null) {
            Log.e(javaClass.simpleName, "最初に表示するPreferenceのXMLのリソースIDが確認にできませんでした。")
            return
        }
        if (preferenceFragmentMap == null) {
            Log.e(javaClass.simpleName, "検索対象にするXML:FragmentNameのHashMapが未指定です。")
            return
        }

        this.fragmentHostLayout = fragmentHostLayout

        // ViewModel初期化
        viewModel = ViewModelProvider(this, SearchPreferenceViewModelFactory(requireActivity().application, preferenceXmlId, preferenceFragmentMap)).get(SearchPreferenceViewModel::class.java)

        // 一回だけ（画面回転時は無視
        if (savedInstanceState == null) {
            // PreferenceFragment設置
            val preferenceFragment = SearchPreferenceChildFragment()
            preferenceFragment.arguments = Bundle().apply {
                putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, preferenceXmlId)
            }
            setFragment(preferenceFragment, CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG)
        }

        // テキストボックスの変更を監視
        editText.addTextChangedListener { edit ->
            // もし、Fragmentを切り替えてしまった場合は、最初のFragment（SearchPreferenceChildFragment）へ戻す。
            if (childFragmentManager.findFragmentById(fragmentHostLayout.id)?.tag != CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG) {
                childFragmentManager.popBackStack(CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG, 0)
            }
            viewModel.searchEditTextChange.value = edit.toString()
        }

        // PreferenceのXML切り替わったらEditTextクリア
        viewModel.changePreferenceScreen.observe(viewLifecycleOwner) { result ->
            editText.setText("")
        }

        // 戻るキー押したとき
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                requireActivity().finish()
            }
        }

    }

    /**
     * Fragmentを置く関数
     * @param fragment その名の通り
     * @param tag Fragmentを探すときに使えるタグと、popBackStackで使えるタグをセットできる
     * */
    fun setFragment(fragment: Fragment, tag: String? = null) {
        if (fragmentHostLayout != null) {
            childFragmentManager.beginTransaction().replace(fragmentHostLayout!!.id, fragment, tag).addToBackStack(tag).commit()
        }
    }

}