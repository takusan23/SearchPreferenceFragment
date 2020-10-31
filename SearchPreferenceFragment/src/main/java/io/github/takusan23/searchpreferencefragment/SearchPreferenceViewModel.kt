package io.github.takusan23.searchpreferencefragment

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.xmlpull.v1.XmlPullParser

class SearchPreferenceViewModel(application: Application, val preferenceXmlRes: IntArray) :AndroidViewModel(application){

    /** Context */
    private val context = getApplication<Application>().applicationContext

    val preferenceList = MutableLiveData<ArrayList<SearchPreferenceParseData>>(arrayListOf())

    private val PREFERNCE_ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

     val searchEditTextChange = MutableLiveData<String>()

    init {
        // 利用するXmlを解析しておく
        parsePreferenceXML()
    }

    fun parsePreferenceXML() {
        preferenceXmlRes.forEach {resId->
            val parser = context.resources.getXml(resId)
            var eventType = parser.eventType
            // 終了まで繰り返す
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.name != null) {
                    val title = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "title")
                    val summary = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "summary")
                    val fragment = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "fragment")
                    // null以外
                    if (title != null || summary != null) {
                        val preference = Preference(context).also { pref ->
                            pref.title = title
                            pref.summary = summary
                            pref.fragment = fragment
                        }
                        preference.extras.apply {
                            putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID,resId)
                        }
                        preference.fragment = "io.github.takusan23.searchpreferencefragment.SearchPreferenceChildFragment"
                        preferenceList.value?.add(SearchPreferenceParseData(preference,resId))
                    }
                }
                eventType = parser.next()
            }
        }
    }

}