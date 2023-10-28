package com.example.myprototype

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VisitCountViewModel : ViewModel() {
    val visitCountLiveData:MutableLiveData<Int> = MutableLiveData<Int>()
    //val rmBitmap:MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()

    fun getVisitCount(): LiveData<Int> {
        return visitCountLiveData
    }

    fun incrementVisitCount() {
        val count = visitCountLiveData.value ?: 0// "?:0" is null check
        Log.d(TAG,"visitcount:${count}")

        visitCountLiveData.value = count + 1
    }

    val TAG = "VisitViewModel"
}
