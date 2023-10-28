package com.example.myprototype

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

class AsyncTaskClass {
    lateinit var strParams: String

    inner class AsyncRunnable : Runnable {
        private lateinit var result: String
        var handler = Handler(Looper.getMainLooper())
        override fun run() {
            // ここにバックグラウンド処理を書く
            result = doInBackground()
            handler.post { onPostExecute(result) }
        }
    }

    fun execute(vararg params: String) {
        strParams = params[0]
        val executorService = Executors.newSingleThreadExecutor()
        executorService.submit(AsyncRunnable())
    }

    fun doInBackground(): String {
        return ""
    }

    fun onPostExecute(result: String) {

    }
}