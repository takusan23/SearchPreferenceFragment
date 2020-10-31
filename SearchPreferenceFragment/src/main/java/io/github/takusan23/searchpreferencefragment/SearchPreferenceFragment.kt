package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

/**
 * 検索可能PreferenceFragment。このFragmentに[androidx.preference.PreferenceFragmentCompat]を置く
 * */
class SearchPreferenceFragment : Fragment() {

    companion object {
        const val PREFERENCE_XML_RESOURCE_LIST = "preference_xml_resource_list"
    }

    private lateinit var viewModel: SearchPreferenceViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_preference_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferenceXmlList = arguments?.getIntArray(PREFERENCE_XML_RESOURCE_LIST)
        val preferenceXmlId = arguments?.getInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID)
        if (preferenceXmlList == null) {
            Log.e(javaClass.simpleName, "XMLが未指定です。null")
            return
        }
        if(preferenceXmlId == null){
            Log.e(javaClass.simpleName,"最初に表示するPreferenceのXMLのリソースIDが確認にできませんでした")
            return
        }

        viewModel = ViewModelProvider(this, SearchPreferenceViewModelFactory(requireActivity().application, preferenceXmlList)).get(SearchPreferenceViewModel::class.java)

        // 一回だけ（画面回転時は無視
        if (savedInstanceState == null) {
            // PreferenceFragment設置
            val preferenceFragment = SearchPreferenceChildFragment()
            preferenceFragment.arguments = Bundle().apply {
                putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, preferenceXmlId)
            }
            childFragmentManager.beginTransaction().replace(R.id.search_fragment_host_frame_layout, preferenceFragment).commit()
        }


        // テキストボックスの変更を監視
        val editText = view.findViewById<EditText>(R.id.search_fragment_input)
        editText.addTextChangedListener { edit ->
            viewModel.searchEditTextChange.value = edit.toString()
        }


    }

}