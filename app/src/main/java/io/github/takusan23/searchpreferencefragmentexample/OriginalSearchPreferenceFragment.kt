package io.github.takusan23.searchpreferencefragmentexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.takusan23.searchpreferencefragment.SearchPreferenceFragment
import kotlinx.android.synthetic.main.fragment_original_search.*

/**
 * [SearchPreferenceFragment]を継承したオリジナル版
 * */
class OriginalSearchPreferenceFragment : SearchPreferenceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_original_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // super.onViewCreated(view, savedInstanceState) // 継承元のonViewCreated呼ばないのでコメントアウト
        init(savedInstanceState, fragment_original_search_edit_text, fragment_original_search_fragment_host)
    }

}