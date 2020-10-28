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

    private lateinit var viewModel: SearchPreferenceViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_preference_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 一回だけ（画面回転時は無視
        if (savedInstanceState == null) {
            val preferenceXmlName = arguments?.getInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID)
            if (preferenceXmlName != null) {
                viewModel = ViewModelProvider(this, SearchPreferenceViewModelFactory(requireActivity().application, preferenceXmlName)).get(SearchPreferenceViewModel::class.java)
                // PreferenceFragment設置
                val preferenceFragment = SearchPreferenceChildFragment()
                preferenceFragment.arguments = Bundle().apply {
                    putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, preferenceXmlName)
                }
                childFragmentManager.beginTransaction().replace(R.id.search_fragment_host_frame_layout, preferenceFragment).commit()
                viewModel.parsePreferenceXML()
            } else {
                // argumentに詰め忘れたとき
                Log.e(javaClass.simpleName, "Preferenceのxmlが設定できませんでした。リソースIDを確認してください。")
            }
        }

        // テキストボックスの変更を監視
        val editText = view.findViewById<EditText>(R.id.search_fragment_input)
        editText.addTextChangedListener { edit ->
            if (edit.isNullOrEmpty()) {
                // 空だった時

            } else {
                // 入力中

            }
        }


    }

}