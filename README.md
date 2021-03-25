# SearchPreferenceFragment
検索できるPreferenceCompatFragmentです🔍🥳

<p align="center">
    <img src="https://imgur.com/5Ci9BJq.gif" width=500>
</p>

<p align="center">
    <img src="https://imgur.com/6V6cBr2.png" width=200>
    <img src="https://imgur.com/RJ8MPtT.png" width=200>
</p>

# 特徴✨
- 検索できるPreferenceCompatFragment
- **PreferenceCompatFragmentからの移行は簡単（だと思う）**
- **複数のPreferenceCompatFragment(階層 / android:fragment)にも対応**
    - その際はFragmentを切り替えて案内します
    - 階層の方はPreferenceCompatFragmentがそのまま使えます。
- Kotlin
    - [関数など](#関数)
- スクロール+ハイライト機能
    - [デフォルトでは黄色。変更可能（後述）](#ハイライトの色とか回数とか間隔とか)
    - 複数のPreferenceCompatFragmentのときも動作
- SearchPreferenceFragmentを継承してオリジナル検索画面も作成可能
    - [EditTextなしでも動く（後述）](#オリジナル設定画面)
- [Preferenceを押した際のクリックイベント](#Preferenceを押した時は？)

# 使い方
## 導入
JitPackを利用して導入可能です。  
[![](https://jitpack.io/v/takusan23/SearchPreferenceFragment.svg)](https://jitpack.io/#takusan23/SearchPreferenceFragment)

`app`フォルダじゃない方の`build.gradle`を開き一行足します。

```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' } // これ
    }
}
```

そしたら今度、`app`フォルダにある`build.gradle`を開き一行足します。1.3.0の部分は最新版を入れてください。

```gradle
dependencies {
    implementation 'com.github.takusan23:SearchPreferenceFragment:1.3.0'
    // 省略
}
```

## Activity もしくは Fragment のレイアウトに
`FrameLayout`を置いてください。このFrameLayoutにFragmentをセットします。

```xml
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <FrameLayout
        android:id="@+id/activity_main_fragment_host_frame_layout"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintBottom_toTopOf="@+id/activity_main_bottom_navigation_bar"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">
    </FrameLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
```

## Fragmentを置くコードを書く

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 検索Fragment
        val searchPreferenceFragment = SearchPreferenceFragment()
        val bundle = Bundle().apply {
            // このあとここ書く
        }
        // bundleセット
        searchPreferenceFragment.arguments = bundle
        // Fragmentを設置
        supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_host_frame_layout, searchPreferenceFragment).commit()

    }

}
```

## 最初に表示するPreferenceのリソースIDを指定
`SearchPreferenceFragment`にBundleをセットし、PreferenceのリソースIDを指定してください。

```kotlin
val bundle = Bundle().apply {
    // 最初に表示するリソースID
    putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
}
```

## 検索に乗せるPreferenceを集める

(作るのに大変だったところ)このライブラリでは、複数のPreferenceCompatFragmentが有っても登録できれば検索結果に表示できるようになります。さらに既存の`PrefrenceCompatFragment`をそのまま使えます。  
なお、**最初に表示するPreferenceのリソースIDを指定**の項目で指定したリソースIDは書かなくていいです。

必要なものは、`PrefrenceCompatFragment`を継承した設定画面（遷移するFragment）と、そのFragmentに指定しているPreferenceのXMLのリソースIDです。

以下が例です（SubSettingFragmentのところは各自書き換えてください。）

```kotlin
val bundle = Bundle().apply {
    // 検索対象にするPreferenceのXMLのリソースIDとPreferenceCompatFragmentを指定する。
    val map = hashMapOf(SubSettingFragment::class.qualifiedName to R.xml.sub_preference)
    // これと同じこと
    // val map = hashMapOf("io.github.takusan23.searchpreferencefragmentexample.SubSettingFragment" to R.xml.sub_preference)
    putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
}
```

`qualifiedName`ってのは、パッケージ名+クラス名みたいな文字列(こんなの：`io.github.takusan23.searchpreferencefragmentexample.SubSettingFragment`)でKotlinでは(クラス::class.qualifiedName)で取れます。  
別に文字列で`"io.github.takusan23.searchpreferencefragmentexample.SubSettingFragment"`って指定しても大丈夫です。

なおPreferenceのXMLだけで画面遷移する方法は、`<Preference>`要素に`android:fragment`属性をつけるだけでFragment遷移をやってくれます（これはPreferenceCompatFragmentの機能。このライブラリの機能ではない）  
公式ドキュメント：https://developer.android.com/guide/topics/ui/settings/organize-your-settings#split_your_hierarchy_into_multiple_screens

```xml
<Preference
    android:fragment="io.github.takusan23.searchpreferencefragmentexample.SubSettingFragment"
    android:summary="Fragmentが切り替わります"
    android:title="Android コードネーム" />
```

### 階層が一つだけの場合
空のHashmapを渡せばいいです。  
空っぽの場合は**最初に表示するPreferenceのリソースIDを指定**で指定したPreferenceのみが検索対象になります。

```kotlin
val bundle = Bundle().apply {
    // 空っぽのHashmapを渡す。
    val map = hashMapOf<String,Int>()
    putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
}
```


> それと本家では`onPreferenceStartFragment()`を実装してくれと書いてますが、このライブラリでは実装しても使わないので実装しなくていいです。

**これだけです。**  
これで複数のPreferenceCompatFragmentの設定も検索結果に表示できるようになりました。

遷移先Fragment(PreferenceCompatFragment / `android:fragment`)に関しては特に何もしなくて大丈夫です。

### ここまでのコード

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 検索Fragment
        val searchPreferenceFragment = SearchPreferenceFragment()
        val bundle = Bundle().apply {
            // 検索対象にするPreferenceのXMLのリソースIDとPreferenceCompatFragmentを指定する。
            val map = hashMapOf(SubSettingFragment::class.qualifiedName to R.xml.sub_preference)
            putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
            // 最初に表示するリソースID
            putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
        }
        // bundleセット
        searchPreferenceFragment.arguments = bundle

        // Fragmentを設置
        supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_host_frame_layout, searchPreferenceFragment).commit()

    }
}
```

## Preferenceを押した時は？
一番最初に表示するPreferenceのクリックイベントは、`onPreferenceClickFunc`が呼ばれます。  
階層が切り替わった場合のPreferenceのクリックイベントは、`onChildPreferenceFragmentCompatClickFunc`が呼ばれます。  

```kotlin
// 最初に表示しているPreferenceのクリックイベント
searchPreferenceFragment.onPreferenceClickFunc = { pref ->
    Toast.makeText(this, "設定押した！", Toast.LENGTH_SHORT).show()
}
// 別のPreferenceCompatFragmentに切り替わったときのPreferenceクリックイベント
searchPreferenceFragment.onChildPreferenceFragmentCompatClickFunc = { pref ->
    Toast.makeText(this, "別の階層の設定", Toast.LENGTH_SHORT).show()
}
```

だた、`onChildPreferenceFragmentCompatClickFunc`に関してはそれぞれの`PreferenceCompatFragment(`を継承したFragment)でクリックイベントを実装したほうがわかりやすいと思います。(せっかく今までの`PreferenceCompatFragment`をそのまま使えるように書いたんだしさ)

## ハイライトの色とか回数とか間隔とか
デフォルトでは<span style="background:yellow">黄色</span>になっていますが、Bundle経由で指定ができます。

```kotlin
val bundle = Bundle().apply {
    // ハイライトする回数。偶数のみ
    putInt(SearchPreferenceChildFragment.SEARCH_PREFERENCE_REPEAT_COUNT, 6)
    // ハイライトの間隔。ミリ秒で、Longである必要がある
    putLong(SearchPreferenceChildFragment.SEARCH_PREFERENCE_BACKGROUND_REPEAT_DELAY, 200)
    // ハイライトする際の色
    putInt(SearchPreferenceChildFragment.SEARCH_SCROLL_HIGH_LIGHT_COLOR, Color.parseColor("#400000ff"))
}
```

色の他にも、間隔、回数を設定できます。回数は偶数じゃないと色がついたまま終わっちゃうと思います？

## Bundleに入れられる値
さっき説明した2つの値以外にもいくつかあります

- 必須 
    - `SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID`
        - Int
        - 最初に表示するPreferenceのxmlを指定してください。
            - 例：`putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)`

- 任意
    - `SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP`
        - HashMap
        - 検索結果を集めるのに使う。
            - 例：`putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, hashMapOf(SubSettingFragment::class.qualifiedName to R.xml.sub_preference))`
    - `SearchPreferenceChildFragment.SEARCH_PREFERENCE_REPEAT_COUNT`
        - Int
        - 検索結果を押してスクロールし、Preferenceの背景色を何回切り替えるか。偶数のみ
            - 例：`putInt(SearchPreferenceChildFragment.SEARCH_PREFERENCE_REPEAT_COUNT, 6)`
    - `SearchPreferenceChildFragment.SEARCH_PREFERENCE_BACKGROUND_REPEAT_DELAY`
        - Long
        - ハイライトする際の間隔。
            - 例：`putLong(SearchPreferenceChildFragment.SEARCH_PREFERENCE_BACKGROUND_REPEAT_DELAY, 200)`
    - `SearchPreferenceChildFragment.SEARCH_SCROLL_HIGH_LIGHT_COLOR`
        - Int
        - ハイライトの色
            - 例：`putInt(SearchPreferenceChildFragment.SEARCH_SCROLL_HIGH_LIGHT_COLOR, Color.parseColor("#800000ff"))`

すべて入れるとこんな感じ

```kotlin
val bundle = Bundle().apply {
    // 検索対象にするPreferenceのXMLのリソースIDとPreferenceCompatFragmentを指定する。
    val map = hashMapOf(SubSettingFragment::class.qualifiedName to R.xml.sub_preference)
    putSerializable(SearchPreferenceFragment.PREFERENCE_XML_FRAGMENT_NAME_HASH_MAP, map)
    // 最初に表示するFragment
    putInt(SearchPreferenceChildFragment.PREFERENCE_XML_RESOURCE_ID, R.xml.preference)
    // ハイライトする回数。偶数のみ
    putInt(SearchPreferenceChildFragment.SEARCH_PREFERENCE_REPEAT_COUNT, 6)
    // ハイライトの間隔。ミリ秒で、Longである必要がある
    putLong(SearchPreferenceChildFragment.SEARCH_PREFERENCE_BACKGROUND_REPEAT_DELAY, 200)
    // ハイライトする際の色
    putInt(SearchPreferenceChildFragment.SEARCH_SCROLL_HIGH_LIGHT_COLOR, Color.parseColor("#400000ff"))
}
```

# オリジナル設定画面
`SearchPreferenceFragment`を継承することでEditTextの位置を変えたり出来ます。  
`SearchPreferenceFragment`では`PreferenceCompatFragment`を置くためのView、EditTextがあれば動きます。  
というかEditTextも必要なければいりません。

以下例（`com.google.android.material`(マテリアルデザインライブラリ)が無いと動かない）

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/purple_200"
            app:layout_scrollFlags="scroll|enterAlways|snap">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="horizontal">

                <ImageView
                    android:layout_width="wrap_content"
                    android:layout_height="match_parent"
                    android:padding="5dp"
                    app:srcCompat="@drawable/ic_outline_settings_24" />

                <EditText
                    android:id="@+id/fragment_original_search_edit_text"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:hint="検索項目を探す"
                    android:text="" />
            </LinearLayout>

        </com.google.android.material.appbar.CollapsingToolbarLayout>

    </com.google.android.material.appbar.AppBarLayout>

    <FrameLayout
        android:id="@+id/fragment_original_search_fragment_host"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

できたらFragmentのコードを少しだけ書きます。`onViewCreated`の`superなんちゃら`は消して、`init()`関数を呼べばいいです。  
- init()
    - 第1引数 onViewCreatedの第2引数を指定
    - 第2引数 レイアウトに置いたEditTextを指定
    - 第3引数 Fragmentを置くためのViewを指定

```kotlin
class OriginalSearchPreferenceFragment : SearchPreferenceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_original_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // super.onViewCreated(view, savedInstanceState) // 継承元のonViewCreated呼ばないのでコメントアウト
        init(savedInstanceState, fragment_original_search_edit_text, fragment_original_search_fragment_host)
    }

}
```

## EditTextなしバージョン
EditText関係の処理を省いた`initFragment()`関数を呼べばいいです。

```kotlin
class OriginalSearchPreferenceFragment : SearchPreferenceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_original_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // super.onViewCreated(view, savedInstanceState) // 継承元のonViewCreated呼ばないのでコメントアウト

        // Fragmentを置くViewをセット
        initFragment(savedInstanceState, fragment_original_search_fragment_host)

          // 検索する際はこの関数を呼べばいい
        search("")
  }

}
```

なお、検索する際は`search()`関数を利用することで検索ができます。引数に検索ワードを入れてね

# 関数
- onPreferenceClickFunc
    - 最初に表示しているPreferenceを押したときに呼ばれる高階関数
- onChildPreferenceFragmentCompatClickFunc
    - 他のPreferenceCompatFragmentへ画面遷移した後にPreferenceを押したときに呼ばれる高階関数です
- onPreferenceFragmentChangeEventFunc
    - Fragmentが切り替わったときに呼ばれる高階関数

## 番外編
なんで今まで使ってた`PreferenceCompatFragment`が何もしなくてもスクロールできたり背景色変えれたりするのやばくない？って話ですが、  
`Android Jetpack`の`Lifecycle`ってのを使うことで実現してます。  

実はこれ、`Fragment`のライフサイクルを他のActivityやFragmentで受け取れるようになったんですよ！

例えば、FragmentがonStart()の状態になったときを知りたい場合はこう

```kotlin
requireParentFragment().lifecycle.addObserver(object : LifecycleObserver {
    // ライフサイクルがonStart()のときに関数を自動で呼ぶ
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        println("ライフサイクルはonStartだよ！")
    }
})
```

`Lifecycle.Event.ON_START`以外にもありますので試してみては？超便利

# ライセンス
マテリアルアイコンとマテリアルデザインのライブラリのライセンスも一応書いておく

```
--- takusan23/SearchPreferenceFragment ---
            
 Copyright 2020 takusan_23
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
 
 --- google/material-design-icons ---

We have made these icons available for you to incorporate into your products under the Apache License Version 2.0.
Feel free to remix and re-share these icons and documentation in your products. 
We'd love attribution in your app's about screen, but it's not required. The only thing we ask is that you not re-sell these icons.

--- material-components/material-components-android ---

Apache-2.0 License

```
