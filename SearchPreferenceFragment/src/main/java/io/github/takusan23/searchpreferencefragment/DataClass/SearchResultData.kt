package io.github.takusan23.searchpreferencefragment.DataClass

import androidx.preference.PreferenceCategory

/**
 * 検索結果のLiveDataで送るデータをまとめたデータクラス
 * @param resId 選択したPreferenceが所属してるxmlのリソースID
 * @param preferenceId Preferenceのキー。
 * */
data class SearchResultData(
    val resId: Int,
    val preferenceId: String?,
)