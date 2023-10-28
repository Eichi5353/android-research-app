package com.example.myprototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import java.util.*

class BitmapViewModel:ViewModel() {
    var range: IntRange? = null //乱数の範囲を取得するため
    var RefList: ArrayList<String>? = arrayListOf()//storage内の画像の場所を取得するための配列
    val storage = Firebase.storage
    var ONE_MEGABYTE:Long?=1024 * 1024 *10
    var gsReference: StorageReference? =null

    /*
    //LiveDataなし->画面遷移で情報消失
    private var mBitmap: Bitmap? = null

    fun getBitmap(): Bitmap? {
        return mBitmap
    }
    fun setBitmap(bitmap: Bitmap) {
        mBitmap = bitmap
    }

     */


    //LiveDataを使うほう
    //Listにしたほうがよくね？？
    val mBitmap:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val mBitmap2:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val mBitmap3:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()

    //mondaiのURIを保存する
    val mUri:MutableLiveData<String> = MutableLiveData<String>()
    val mUri2:MutableLiveData<String> = MutableLiveData<String>()
    val mUri3:MutableLiveData<String> = MutableLiveData<String>()


    //問題画像のURL
    val mUrl: MutableLiveData<StorageReference> = MutableLiveData<StorageReference>()



    //takeImg
    val tBitmap:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()

    //URI
    val tUri:MutableLiveData<Uri> = MutableLiveData<Uri>()


    //結果得点
    val result1:MutableLiveData<String> = MutableLiveData<String>()
    val result2:MutableLiveData<String> = MutableLiveData<String>()
    val result3:MutableLiveData<String> = MutableLiveData<String>()

    //結果表示Bitmap
    val rmBitmap:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val rmBitmap2:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val rmBitmap3:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val rtBitmap:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val rtBitmap2:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val rtBitmap3:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()


    /*
    fun getBitmap(): MutableLiveData<Bitmap> {
        return mBitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        mBitmap.value = bitmap
    }

    fun loadBitmap(){

    }

 */
    fun choseimg() {
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
                    Log.i("Storage", "prefix is $prefix")
                }

                items.forEach { item ->
                    // All the items under listRef.
                    Log.i("Storage", "item is $item")
                    RefList?.add("$item")
                    Log.i("Storage", "items are $RefList")
                }

                //乱数生成（1から配列の大きさ)
                range = (1..RefList!!.size)
                val size = RefList!!.size
                Log.i("Storage", "array_size is $size")

                //関数にしたほうが見やすいか？
                //ここを撮影画像のみ選べるようにする　or 直前の撮影画像を持ってくる
                val random_num = range?.random()
                //選ばれた配列
                val result = RefList!![random_num!! - 1]
                Log.i("Storage", "random_num is $random_num")
                Log.i("Storage", "random_item is $result")

                gsReference = storage.getReferenceFromUrl(result)
                ONE_MEGABYTE = 1024 * 1024 * 10 //最大ダウンロードできるバイトサイズ　小さいとエラーになる
                gsReference?.getBytes(ONE_MEGABYTE!!)?.addOnSuccessListener {
                    //bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

                    //ここ追加したぞ！！
                    mBitmap.setValue(BitmapFactory.decodeByteArray(it, 0, it.size))
                    Log.d("ViewModel","$mBitmap")
                }
            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
            }
    }
    /*
    private var gsReference= MutableLiveData<StorageReference>()
    fun getReference():MutableLiveData<StorageReference>{
        return gsReference
    }
    fun setReference(gsRef:StorageReference){
        gsReference.value=gsRef
    }
     */


}