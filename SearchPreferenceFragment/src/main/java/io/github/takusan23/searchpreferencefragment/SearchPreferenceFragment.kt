package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_search_preference_fragment.*
import org.xmlpull.v1.XmlPullParser

/**
 * 検索可能PreferenceFragment。このFragmentに[androidx.preference.PreferenceFragmentCompat]を置く
 * */
class SearchPreferenceFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_preference_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 一回だけ（画面回転時は無視
        if (savedInstanceState == null) {
            println(arguments)
            val preferenceXmlName = arguments?.getInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID)
            if (preferenceXmlName != null) {
                // PreferenceFragment設置
                val preferenceFragment = SearchPreferenceChildFragment()
                preferenceFragment.arguments = Bundle().apply {
                    putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, preferenceXmlName)
                }
                childFragmentManager.beginTransaction().replace(R.id.search_fragment_host_frame_layout, preferenceFragment).commit()
            } else {
                // argumentに詰め忘れたとき
                Log.e(javaClass.simpleName, "Preferenceのxmlが設定できませんでした。リソースIDを確認してください。")
            }
        }

        // テキストボックスの変更を監視
        search_fragment_input.addTextChangedListener {

        }


    }

    fun parsePreferenceXML(preferenceXmlRes: Int){
        val parser = requireContext().resources.getXml(preferenceXmlRes)

    }

}