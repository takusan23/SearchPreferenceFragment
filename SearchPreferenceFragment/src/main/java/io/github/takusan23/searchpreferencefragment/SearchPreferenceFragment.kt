package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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
 * - 指定できたら、[onViewCreated]あたりで[init]関数を呼んでください。第二引数以降は、レイアウトに置いたViewを指定してください。
 *      - [onViewCreated]の[super]はコメントアウトしてね
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

    /** Fragmentを設置するView */
    private var fragmentHostLayout: View? = null

    /**
     * 表示しているPreferenceを押したときに呼ばれる高階関数。
     *
     * [Preference.setOnPreferenceClickListener]の代わり
     *
     * */
    var onPreferenceClickFunc: ((preference: Preference?) -> Unit)? = null

    /**
     * 切り替え先Fragmentが[androidx.preference.PreferenceFragmentCompat]だったときに、Preferenceを押したときに呼ばれる関数
     *
     * ただ、この関数を使うより各PreferenceCompatFragment(を継承したFragment)でクリックイベントを実装したほうがいいと思う(わかりにくくなりそう)。個人的非推奨
     *
     * (遷移先Fragmentとは→Preference要素に「android.fragment」属性として指定したFragmentのこと。以下例)
     *
     * ```xml
     * <Preference
     *      android:title="Fragment切り替え"
     *      android:fragment="ChildPreferenceFragment"  // ここで指定したFragmentのこと
     *      />
     * ```
     * */
    var onChildPreferenceFragmentCompatClickFunc: ((preference: Preference?) -> Unit)? = null

    /**
     * Fragmentが切り替わった際に呼ばれる関数です。
     * */
    var onPreferenceFragmentChangeEventFunc: (() -> Unit)? = null

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
     *
     * なお、この関数を使わなくても[initFragment]を最初に呼んで、検索したいときに[search]を呼べばこの関数を利用しなくてもいいです。(EditTextがなくても良くなる)
     *
     * @param savedInstanceState 画面回転時かどうかの判断で利用。[onViewCreated]の第二引数のやつ
     * @param editText 検索時に利用するEditText
     * @param fragmentHostLayout Fragmentを置くView
     * */
    fun init(savedInstanceState: Bundle?, editText: EditText, fragmentHostLayout: View) {

        // Fragmentをセットする
        initFragment(savedInstanceState, fragmentHostLayout)

        // EditTextをセットする
        initEditText(editText)
    }

    /**
     * Fragmentをセットする関数。必須項目
     * */
    fun initFragment(savedInstanceState: Bundle?, fragmentHostLayout: View) {
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
            preferenceFragment.arguments = arguments // めんどいから全部渡す
            setFragment(preferenceFragment, CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG)
        }

        // 戻るキー押したとき
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (childFragmentManager.findFragmentById(fragmentHostLayout.id)?.tag == CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG) {
                requireActivity().finish()
            } else {
                childFragmentManager.popBackStack()
            }
        }

        // PreferenceのXML切り替わったら変更高階関数を呼ぶ
        viewModel.changePreferenceScreen.observe(viewLifecycleOwner) { result ->
            onPreferenceFragmentChangeEventFunc?.invoke()
        }

    }

    /**
     * EditTextをセットする関数。利用すると自動でEditTextの中身を監視します。
     * こっちは必須じゃないけど、代わりにEditTextの変更イベントを検知して、[search]関数を呼ぶ必要があります。
     * */
    fun initEditText(editText: EditText) {

        // テキストボックスの変更を監視
        editText.addTextChangedListener(afterTextChanged = { text ->
            // もし、Fragmentを切り替えてしまった場合は、最初のFragment（SearchPreferenceChildFragment）へ戻す。
            search(text.toString())
        })

        // PreferenceのXML切り替わったらEditTextクリア
        viewModel.changePreferenceScreen.observe(viewLifecycleOwner) { result ->
            editText.setText("")
        }
    }

    /**
     * 検索する関数。[initEditText]の中で使ってる。検索ボタンを実装したい場合はどうぞ
     * [EditText.addTextChangedListener]の中で利用する
     * @param searchWord 検索ワード
     * */
    fun search(searchWord: String) {
        if (fragmentHostLayout == null) {
            Log.e(javaClass.simpleName, "Fragmentを設置するfragmentHostLayoutがnullです")
            return
        }
        // もし、Fragmentを切り替えてしまった場合は、最初のFragment（SearchPreferenceChildFragment）へ戻す。
        if (childFragmentManager.findFragmentById(fragmentHostLayout!!.id)?.tag != CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG) {
            childFragmentManager.popBackStack(CHILD_SEARCH_PREFRENCE_BACK_STACK_TAG, 0)
        }
        viewModel.searchEditTextChange.postValue(searchWord)
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