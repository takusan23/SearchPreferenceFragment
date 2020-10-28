package io.github.takusan23.searchpreferencefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import com.google.android.material.textfield.TextInputLayout
import org.xmlpull.v1.XmlPullParser

/**
 * 検索可能PreferenceFragment。このFragmentに[androidx.preference.PreferenceFragmentCompat]を置く
 * */
class SearchPreferenceFragment : Fragment() {

    private val preferenceList = arrayListOf<Preference>()

    private val PREFERNCE_ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_preference_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 一回だけ（画面回転時は無視
        if (savedInstanceState == null) {
            val preferenceXmlName = arguments?.getInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID)
            if (preferenceXmlName != null) {
                // PreferenceFragment設置
                val preferenceFragment = SearchPreferenceChildFragment()
                preferenceFragment.arguments = Bundle().apply {
                    putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, preferenceXmlName)
                }
                childFragmentManager.beginTransaction().replace(R.id.search_fragment_host_frame_layout, preferenceFragment).commit()
                parsePreferenceXML(preferenceXmlName)
            } else {
                // argumentに詰め忘れたとき
                Log.e(javaClass.simpleName, "Preferenceのxmlが設定できませんでした。リソースIDを確認してください。")
            }
        }

        // テキストボックスの変更を監視
        view.findViewById<EditText>(R.id.search_fragment_input).addTextChangedListener {

        }


    }

    fun parsePreferenceXML(preferenceXmlRes: Int) {
        val parser = requireContext().resources.getXml(preferenceXmlRes)
        var eventType = parser.eventType
        // 終了まで繰り返す
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.name != null) {
                val title = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "title")
                val summary = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "summary")
                    // null以外
                if(title != null || summary != null){
                    val preference = Preference(requireContext()).also { pref ->
                        pref.title = title
                        pref.summary = summary
                    }
                    preferenceList.add(preference)
                }
            }
            eventType = parser.next()
        }
        println(preferenceList.map { preference -> preference.title })
    }

}