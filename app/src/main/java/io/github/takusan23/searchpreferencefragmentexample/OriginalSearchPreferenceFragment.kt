package io.github.takusan23.searchpreferencefragmentexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.takusan23.searchpreferencefragment.SearchPreferenceFragment
import io.github.takusan23.searchpreferencefragmentexample.databinding.FragmentOriginalSearchBinding

/**
 * [SearchPreferenceFragment]を継承したオリジナル版
 * */
class OriginalSearchPreferenceFragment : SearchPreferenceFragment() {

    private val viewBinding by lazy { FragmentOriginalSearchBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // super.onViewCreated(view, savedInstanceState) // 継承元のonViewCreated呼ばないのでコメントアウト

        // Fragmentを置くViewをセット
        initFragment(savedInstanceState, viewBinding.fragmentOriginalSearchFragmentHost)

        // 検索する際はこの関数を呼べばいい
        search("")

    }

}