package io.github.takusan23.searchpreferencefragment.DataClass

import androidx.preference.Preference
import androidx.preference.PreferenceCategory

/**
 * 検索内容に一致したPreferenceを返すときに使ってるデータクラス
 * @param preference 検索に一致したPreference
 * @param resId [preference]が所属してるxmlのリソースID
 * @param preferenceTitle Preferenceのタイトル
 * @param preferenceSummary Preferenceの説明欄
 * @param preferenceCategory PreferenceCategoryで囲われたPreferenceの場合はカテゴリ名が入る
 * */
data class SearchPreferenceParseData(
    val preference: Preference,
    val resId: Int,
    val preferenceTitle: String,
    val preferenceSummary: String?,
    val preferenceCategory: String?,
    val fragmentName: String?,
)