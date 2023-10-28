package com.example.myprototype

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CoroutineRepository {
    // 非同期で3秒後にログを出力する
    fun execute() {
        Log.d("Coroutine", "execute start")
        var ayncString: String?
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                // 3秒スリープ（重たい処理）
                Thread.sleep(3000)
                ayncString = "123"
                Log.d("Coroutine", "ayncString:$ayncString")
            }
        }
        Log.d("Coroutine", "execute end")
    }
}