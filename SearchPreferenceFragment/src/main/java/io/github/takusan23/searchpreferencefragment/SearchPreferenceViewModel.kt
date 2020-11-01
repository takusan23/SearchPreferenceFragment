package io.github.takusan23.searchpreferencefragment

import android.app.Application
import android.util.Log
import android.util.Xml
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.Preference
import io.github.takusan23.searchpreferencefragment.DataClass.SearchPreferenceParseData
import org.xmlpull.v1.XmlPullParser


/**
 * 検索PreferenceFragmentで利用するViewModel
 * @param preferenceXmlResId 検索で見つけてもらうPreferenceXML。サイトマップ的な役割を果たす
 * */
class SearchPreferenceViewModel(application: Application, private val preferenceXmlResId: Int, private val preferenceFragmentMap: HashMap<String?, Int>) : AndroidViewModel(application) {

    /** Context */
    private val context = getApplication<Application>().applicationContext

    /** [preferenceXmlResId]で指定したPreferenceをパースして生成されたPreferenceが入る配列 */
    val preferenceList = MutableLiveData<ArrayList<SearchPreferenceParseData>>(arrayListOf())

    /** android名前空間 */
    private val PREFERNCE_ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

    /** SearchPreferenceFragmentに置いたEditTextの変更を通知するLiveData */
    val searchEditTextChange = MutableLiveData<String>()

    /** ChildPreferenceFragmentの画面を切り替えるときに使うLiveData */
    val changePreferenceScreen = MutableLiveData<SearchPreferenceParseData>()

    /** 検索したときにPreferenceCategoryが設定済みの場合に、カテゴリ名の部分につける色 */
    var categoryTextHighlightColor = "blue"

    /** 検索で一致した部分をハイライト表示する際に使う色 */
    @Deprecated("isSearchHighlightColorが非推奨のため")
    var searchContainsTextHighlightColor = "red"

    /** 検索結果の説明欄にPreferenceCategoryのandroid:titleの値を最後に付け足すか。 */
    var isCategoryShow = true

    /** 検索に一致した部分をハイライト化するかどうか。現状動作が不安定なので非推奨 */
    @Deprecated("Summaryがうまく動作しないため今の所非推奨")
    var isSearchHighlightColor = false

    init {
        // 利用するXmlを解析しておく
        parsePreferenceXML(preferenceXmlResId, null)
        for (mutableEntry in preferenceFragmentMap) {
            val xmlResId = mutableEntry.value
            val fragmentName = mutableEntry.key
            parsePreferenceXML(xmlResId, fragmentName)
        }
    }

    private fun parsePreferenceXML(xmlResId: Int, fragmentName: String?) {
        val parser = context.resources.getXml(xmlResId)
        var eventType = parser.eventType

        // カテゴリ分けされてるかも
        var categoryName: String? = null

        // 終了まで繰り返す
        while (eventType != XmlPullParser.END_DOCUMENT) {

            // 使うもの
            val title = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "title")
            val fragmentAttr = parser.getAttributeValue(PREFERNCE_ANDROID_NAMESPACE, "fragment")

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
                    // ここらへんはソースそのまま実装した
                    val attrs = Xml.asAttributeSet(parser)
                    Preference(context, attrs).also { pref ->
                        pref.summary = if (categoryName != null) {
                            HtmlCompat.fromHtml("${pref.summary}<br><font color=$categoryTextHighlightColor>$categoryName</font>", HtmlCompat.FROM_HTML_MODE_COMPACT)
                        } else {
                            pref.summary
                        }
                        // 属性の方を優先して登録する。なければ遷移先Fragment
                        pref.fragment = fragmentAttr ?: fragmentName
                        val data = SearchPreferenceParseData(
                            preference = pref,
                            resId = xmlResId,
                            preferenceTitle = pref.title.toString(),
                            preferenceSummary = pref.summary?.toString(),
                            preferenceCategory = categoryName,
                            fragmentName = fragmentName
                        )
                        preferenceList.value?.add(data)
                        pref.setOnPreferenceClickListener {
                            changePreferenceScreen.value = data
                            false
                        }
                    }
                } else if (eventType == XmlPullParser.START_TAG) {
                    Log.e(javaClass.simpleName, "PreferenceのXML解析に失敗しました。")
                    Log.e(javaClass.simpleName, "${parser.lineNumber}行目")
                    Log.e(javaClass.simpleName, "android:title に値が入っていることを確認してください。")
                }
            }
            eventType = parser.next()
        }

    }

    /**
     * 検索ワードでふるいにかける。ハイライト機能付き！！！！
     * @param searchText 検索ワード
     * @return 一致した検索項目
     * */
    fun findPreference(searchText: String): List<SearchPreferenceParseData>? {
        val prefList = preferenceList.value?.filter { preferenceData ->
            preferenceData.preferenceTitle.contains(searchText) || (preferenceData.preferenceSummary ?: "").contains(searchText)
        }
        if (isSearchHighlightColor) {
            prefList?.forEach { pref ->
                // タイトル
                val titleHighlight = highlightText(searchText, pref.preferenceTitle, null)
                pref.preference.title = HtmlCompat.fromHtml(titleHighlight, HtmlCompat.FROM_HTML_MODE_COMPACT)
                // 説明欄
                val categoryText = if (pref.preferenceCategory != null) "\n${pref.preferenceCategory}" else ""
                val summaryText = "${pref.preferenceSummary}$categoryText"
                val summaryHighlight = highlightText(searchText, summaryText, pref.preferenceCategory)
                pref.preference.summary = HtmlCompat.fromHtml(summaryHighlight, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }
        }
        return prefList
    }

    /** 改行コードを<br>へ置き換える関数。なんか<p>で囲まないと色が表示されない？ */
    private fun toHtml(text: String): String {
        return "<p>$text</p>".replace("\n", "<br>")
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
            html = html.replace(categoryText, "<font color='$categoryTextHighlightColor'>$categoryText</font>")
        }
        return html.replace(searchText, "<font color='$searchContainsTextHighlightColor'>$searchText</font>")
    }

}