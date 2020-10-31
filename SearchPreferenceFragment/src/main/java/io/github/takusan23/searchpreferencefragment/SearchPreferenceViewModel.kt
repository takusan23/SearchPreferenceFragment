package io.github.takusan23.searchpreferencefragment

import android.app.Application
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.Preference
import io.github.takusan23.searchpreferencefragment.DataClass.SearchPreferenceParseData
import io.github.takusan23.searchpreferencefragment.DataClass.SearchResultData
import org.xmlpull.v1.XmlPullParser

/**
 * 検索PreferenceFragmentで利用するViewModel
 * @param preferenceXmlResList 検索で見つけてもらうPreferenceXML。サイトマップ的な役割を果たす
 * */
class SearchPreferenceViewModel(application: Application, private val preferenceXmlResList: IntArray) : AndroidViewModel(application) {

    /** Context */
    private val context = getApplication<Application>().applicationContext

    /** [preferenceXmlResList]で指定したPreferenceをパースして生成されたPreferenceが入る配列 */
    val preferenceList = MutableLiveData<ArrayList<SearchPreferenceParseData>>(arrayListOf())

    /** android名前空間 */
    private val PREFERNCE_ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

    /** SearchPreferenceFragmentに置いたEditTextの変更を通知するLiveData */
    val searchEditTextChange = MutableLiveData<String>()

    /** ChildPreferenceFragmentの画面を切り替えるときに使うLiveData */
    val changePreferenceScreen = MutableLiveData<SearchResultData>()

    /** 検索したときにPreferenceCategoryが設定済みの場合に、カテゴリ名の部分につける色 */
    var categoryTextHighlightColor = "blue"

    var searchContainsTextHighlightColor = "red"

    /** 検索結果の説明欄にPreferenceCategoryのandroid:titleの値を最後に付け足すか。 */
    var isCategoryShow = true

    init {
        // 利用するXmlを解析しておく
        parsePreferenceXML()
    }

    private fun parsePreferenceXML() {
        preferenceXmlResList.forEach { resId ->

            val parser = context.resources.getXml(resId)
            var eventType = parser.eventType

            // カテゴリ分けされてるかも
            var categoryName: String? = null

            // 終了まで繰り返す
            while (eventType != XmlPullParser.END_DOCUMENT) {

                // 使うもの
                val title = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "title")
                val summary = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "summary")
                val key = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "key")

                // PreferenceCategoryなら名前を控える。
                if (parser.name == "PreferenceCategory" && isCategoryShow) {
                    if (eventType == XmlPullParser.START_TAG) {
                        categoryName = title // 囲いはじめ
                    }
                    if (eventType == XmlPullParser.END_TAG) {
                        categoryName = null // 終了タグ
                    }
                }

                // PreferenceScreenはいらない
                if (parser.name != null && parser.name != "PreferenceScreen" && parser.name != "PreferenceCategory") {

                    // title が null 以外
                    if (title != null) {
                        val preference = Preference(context).also { pref ->
                            pref.title = title
                            pref.summary = summary
                            // スクロールする際に使う？
                            pref.key = key
                            pref.setOnPreferenceClickListener {
                                // ChildPreferenceFragmentに置いたLiveDataへ送信
                                changePreferenceScreen.value = SearchResultData(resId, it.key)
                                false
                            }
                        }
                        preferenceList.value?.add(SearchPreferenceParseData(preference, resId, title, summary, categoryName))
                    } else if (eventType == XmlPullParser.START_TAG) {
                        Log.e(javaClass.simpleName, "PreferenceのXML解析に失敗しました。")
                        Log.e(javaClass.simpleName, "${parser.lineNumber}行目")
                        Log.e(javaClass.simpleName, "android:title に値が入っていることを確認してください。")
                    }

                }
                eventType = parser.next()
            }
        }
    }

    /**
     * 検索ワードでふるいにかける。ハイライト機能付き！！！！
     * @param searchText 検索ワード
     * @return 一致した検索項目
     * */
    fun findPreference(searchText: String): List<SearchPreferenceParseData>? {
        val prefList = preferenceList.value?.filter { preferenceData ->
            preferenceData.preference.title?.contains(searchText) == true || preferenceData.preference.summary?.contains(searchText) == true
        }
        prefList?.forEach { pref ->
            val titleHighlight = highlightText(searchText, pref.preference.title.toString(), null)
            pref.preference.title = HtmlCompat.fromHtml(titleHighlight, HtmlCompat.FROM_HTML_MODE_COMPACT)
            // 説明欄
            val summaryText = if (pref.preferenceCategory != null) {
                "${pref.preferenceSummary}\n${pref.preferenceCategory}"
            } else {
                pref.preferenceSummary
            } ?: return@forEach
            val summaryHighlight = highlightText(searchText, summaryText, pref.preferenceCategory)
            pref.preference.summary = HtmlCompat.fromHtml(summaryHighlight, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
        return prefList
    }

    /** 改行コードを<br>へ置き換える関数 */
    private fun toHtml(text: String): String {
        return text.replace("\n", "<br>")
    }

    /**
     * 検索で一致した部分の色を変える
     * @param searchText 検索ワード
     * @param text 本文
     * @param categoryText カテゴリ名の部分も色を付ける場合はカテゴリ名を指定。
     * */
    private fun highlightText(searchText: String, text: String, categoryText: String?): String {
        var html = toHtml(text)
        if (categoryText != null) {
            // カテゴリ名の色を変える
            html = html.replace(categoryText, "<font color=$categoryTextHighlightColor>$categoryText</font>")
        }
        return html.replace(searchText, "<font color=$searchContainsTextHighlightColor>$searchText</font>")
    }

}