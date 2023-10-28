package com.example.myprototype

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2


class MainViewModel(handle: SavedStateHandle): ViewModel() {
    var range: IntRange? = null //乱数の範囲を取得するため
    var RefList: ArrayList<String>? = arrayListOf()//storage内の画像の場所を取得するための配列
    val storage = Firebase.storage
    private var storageReference: StorageReference? = null

    //表示した画像を保つためのやつ
    var image: Bitmap? = null
    //var bitmap: Bitmap? = null
    var picture_location: String? = null
    var gsReference: StorageReference? =null
    var ONE_MEGABYTE:Long?=null


    private fun getFirebase(){
        val listRef = storage.reference.child("images")
        //storageReference = storage.reference

        //そのノードにある画像の場所をすべて取得してRefList配列に格納する
        //そのあと取得した数分の大きさの乱数を生成し，ランダムで画像を表示するようにする
        listRef.listAll()
            .addOnSuccessListener { (items, prefixes) ->
                prefixes.forEach { prefix ->
                    // All the prefixes under listRef.
                    // You may call listAll() recursively on them.
                    //これは枕詞みたいなもんですか？
                    Log.i("Storage","prefix is $prefix")
                }

                items.forEach { item ->
                    // All the items under listRef.
                    Log.i("Storage","item is $item")
                    RefList?.add("$item")
                    Log.i("Storage","items are $RefList")
                }

                //乱数生成（1から配列の大きさ)
                range = (1..RefList!!.size)
                val size =RefList!!.size
                Log.i("Storage","array_size is $size")


                //関数にしたほうが見やすいか？
                //ここを撮影画像のみ選べるようにする　or 直前の撮影画像を持ってくる
                val random_num = range?.random()
                //選ばれた配列
                val result= RefList!![random_num!!]
                Log.i("Storage","random_num is $random_num")
                Log.i("Storage","random_item is $result")

                gsReference = storage.getReferenceFromUrl(result)
                ONE_MEGABYTE = 1024 * 1024 *10 //最大ダウンロードできるバイトサイズ　小さすぎるとエラーになる

            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
            }
    }



    private lateinit var bitmap: MutableLiveData<Bitmap>
    //by lazy { MutableLiveData<Bitmap>() }

    //imageViewとかViewの部品の場合は？
    private val mimg = MutableLiveData(0)
    val img: MutableLiveData<Int> = mimg
    //????????
    val qurImg:MutableLiveData<ImageView> =handle.getLiveData<ImageView>("image")


    //このcountって変数が画面遷移などで値が変わらない変数になる
    var count: Int = 0
        private set

    fun onClickCountUp() {
        count++
    }

    //ただこのクラス内の変数bitmapの値を得るためだけのもの
    fun getBitmap():LiveData<Bitmap>{
        return bitmap
    }
    fun setBitmap(bitmp: Bitmap){
        val bitmap=bitmp
    }

    //非同期処理　必要なのかよくわからないが
    fun excuteImage() {
        Log.d("Coroutine","executeThread start")
        var ayncBitmap: Bitmap?
        runBlocking {
            withContext(Dispatchers.IO) {
                // 3秒スリープ（重たい処理）
                Thread.sleep(3000)
                //ayncBitmap = bitmap
            }
        }
        //return ayncBitmap
    }
}