package io.github.takusan23.searchpreferencefragment

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceViewHolder

/**
 * なんか、[PreferenceGroupAdapter]を継承しようとすると赤くなるので、アノテーションがついてる
 * */
@SuppressLint("RestrictedApi")
open class SearchPreferenceChildRecyclerViewAdapter(preferenceGroup: PreferenceGroup) : PreferenceGroupAdapter(preferenceGroup) {

    /**
     * Preferenceに色を付ける際は、このPairに値を入れてください。nullにすることで戻せます
     * FirstはPreferenceのKey、SecondはPreferenceの背景色です。
     * */
    var highlightPreferenceKey: Pair<Int, Int>? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        val preference = getItem(position)
        if (highlightPreferenceKey != null) {
            // 背景色を設定
            if (highlightPreferenceKey!!.first == position) {
                holder.itemView.background = ColorDrawable(highlightPreferenceKey!!.second)
            } else {
                holder.itemView.background = null
            }
        } else {
            // RecyclerViewはその名の通りViewがリサイクルされるので、if書いたらelseもちゃんと書かないとリサイクル前のViewの状態になる。難しい
            holder.itemView.background = null
        }
    }

}