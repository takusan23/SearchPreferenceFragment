package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_search_preference_fragment.*

/**
 * 検索可能PreferenceFragment。このFragmentに[androidx.preference.PreferenceFragmentCompat]を置く
 *
 * なお、このFragmentを置く際に、[setArguments]で以下の値をセットしておく必要があります。
 * - [SearchPreferenceFragment.PREFERENCE_XML_RESOURCE_LIST]
 *      - Int配列。
 *      - 検索対象にするXMLのリソースID
 * - [SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID]
 *      - Int
 *      - 設定画面として最初に表示する項目。
 * 設置例：
 * ```
 * val searchPreferenceFragment = SearchPreferenceFragment()
 * val bundle = Bundle().apply {
 *     putIntArray(SearchPreferenceFragment.PREFERENCE_XML_RESOURCE_LIST, intArrayOf(R.xml.preference))
 *     putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
 * }
 * searchPreferenceFragment.arguments = bundle
 * ```
 *
 * UIを変更したい場合はこのクラスを継承して、
 * - [onCreateView]で指定するレイアウトに[EditText]とFragmentを置くView（[android.widget.FrameLayout]がいいらしい？）を最低限入れてね
 * - 指定できたら、[init]関数を呼んでください。第二引数以降は、レイアウトに置いたViewを指定してください。
 * */
open class SearchPreferenceFragment : Fragment() {

    companion object {
        /** 検索対象にするXMLのID配列。Intの配列になるはず */
        const val PREFERENCE_XML_RESOURCE_LIST = "preference_xml_resource_list"
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
    fun init(savedInstanceState: Bundle?, editText: EditText?, fragmentHostLayout: View?) {
        // もらう
        val preferenceXmlList = arguments?.getIntArray(PREFERENCE_XML_RESOURCE_LIST)
        val preferenceXmlId = arguments?.getInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID)

        // 必須項目があるか
        if (preferenceXmlList == null) {
            Log.e(javaClass.simpleName, "検索対象にするXML配列が未指定です。")
            return
        }
        if (preferenceXmlId == null) {
            Log.e(javaClass.simpleName, "最初に表示するPreferenceのXMLのリソースIDが確認にできませんでした。")
            return
        }
        if (editText == null) {
            Log.e(javaClass.simpleName, "検索で利用するEditTextが未指定です。")
            return
        }
        if (fragmentHostLayout == null) {
            Log.e(javaClass.simpleName, "Fragmentを表示するViewが未指定です。")
            return
        }

        this.fragmentHostLayout = fragmentHostLayout

        // ViewModel初期化
        viewModel = ViewModelProvider(this, SearchPreferenceViewModelFactory(requireActivity().application, preferenceXmlList)).get(SearchPreferenceViewModel::class.java)

        // 一回だけ（画面回転時は無視
        if (savedInstanceState == null) {
            // PreferenceFragment設置
            val preferenceFragment = SearchPreferenceChildFragment()
            preferenceFragment.arguments = Bundle().apply {
                putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, preferenceXmlId)
            }
            childFragmentManager.beginTransaction().replace(fragmentHostLayout.id, preferenceFragment).commit()
        }

        // テキストボックスの変更を監視
        editText.addTextChangedListener { edit ->
            viewModel.searchEditTextChange.value = edit.toString()
        }

        // PreferenceのXML切り替わったらEditTextクリア
        viewModel.changePreferenceScreen.observe(viewLifecycleOwner) { result ->
            editText.setText("")
        }

        // 戻るキー押したとき
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            childFragmentManager.popBackStack()
        }

    }

    /** Fragmentを置く関数 */
    fun setFragment(fragment: Fragment, tag: String? = null) {
        if (fragmentHostLayout != null) {
            childFragmentManager.beginTransaction().replace(fragmentHostLayout!!.id, fragment, tag).addToBackStack(tag).commit()
        }
    }

}