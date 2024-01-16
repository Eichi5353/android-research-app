package com.example.myprototype

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapsCountViewModel: ViewModel() {
    val visitCount = MutableLiveData<Int>()
    val result1 = MutableLiveData<String>()
    val result2 = MutableLiveData<String>()
    val result3 = MutableLiveData<String>()
    val result4 = MutableLiveData<String>()
    val result5 = MutableLiveData<String>()

//    リスト型でできる？
    val resultList = mutableListOf<String>()
//    押したボタンの情報を保持するリスト
    val clickedButtonList = mutableListOf<Int>()
//問題画像リスト
    val queryList = mutableListOf<Bitmap>()
//    撮影画像リスト
    val takingList = mutableListOf<Bitmap>()


    init {
        // 初期値を設定
        visitCount.value = 0
    }

    fun incrementVisitCount() {
        // LiveDataの値を更新
        visitCount.value = visitCount.value?.plus(1)
    }
}