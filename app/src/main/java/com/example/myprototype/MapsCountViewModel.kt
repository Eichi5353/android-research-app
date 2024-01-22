package com.example.myprototype

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapsCountViewModel: ViewModel() {
    val visitCount = MutableLiveData<Int>()
    val visitCount2 = MutableLiveData<Int>()

    val result1 = MutableLiveData<String>()
    val result2 = MutableLiveData<String>()
    val result3 = MutableLiveData<String>()
    val result4 = MutableLiveData<String>()
    val result5 = MutableLiveData<String>()

//    リスト型でできる？
    val resultList = mutableListOf<String>()
    val resultList2 = mutableListOf<String>()

    //    押したボタンの情報を保持するリスト
    val clickedButtonList = mutableListOf<Int>()
//問題画像リスト
    val queryList = mutableListOf<Bitmap>()
//    撮影画像リスト
    val takingList = mutableListOf<Bitmap>()

    val queryList2 = mutableListOf<Bitmap>()
    //    撮影画像リスト
    val takingList2 = mutableListOf<Bitmap>()

    val isRequestComplete = MutableLiveData<Int>()

    val isFirstPlaceComplete = MutableLiveData<Boolean>()
    val isSecondPlaceComplete = MutableLiveData<Boolean>()
    val isThirdPlaceComplete = MutableLiveData<Boolean>()

    val queryButtonTouchCount = MutableLiveData<Int>()






    init {
        // 初期値を設定
        visitCount.value = 0
        visitCount2.value = 0
        isRequestComplete.value =0
        isFirstPlaceComplete.value = false
        isSecondPlaceComplete.value = false
        isThirdPlaceComplete.value = false
        queryButtonTouchCount.value = 0
    }

    fun incrementVisitCount() {
        // LiveDataの値を更新
        visitCount.value = visitCount.value?.plus(1)
    }
    fun incrementVisitCount2() {
        // LiveDataの値を更新
        visitCount2.value = visitCount2.value?.plus(1)
    }
    // ViewModel内でLiveDataを宣言

}