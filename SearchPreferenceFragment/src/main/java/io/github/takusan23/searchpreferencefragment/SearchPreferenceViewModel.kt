package io.github.takusan23.searchpreferencefragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.Preference
import org.xmlpull.v1.XmlPullParser

class SearchPreferenceViewModel(application: Application, val preferenceXmlRes: Int) :AndroidViewModel(application){

    /** Context */
    private val context = getApplication<Application>().applicationContext

    private val preferenceList = MutableLiveData<ArrayList<Preference>>()

    private val PREFERNCE_ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"


    /**  */
    fun parsePreferenceXML() {
        preferenceList.value?.clear()
        val parser = context.resources.getXml(preferenceXmlRes)
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
                    preferenceList.value?.add(preference)
                }
            }
            eventType = parser.next()
        }
        preferenceList.value = preferenceList.value
    }

}