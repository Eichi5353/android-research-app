package com.example.myprototype

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.remote.Datastore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage


class MainActivity : AppCompatActivity() {
    var range: IntRange? = null //乱数の範囲を取得するため
    var RefList: ArrayList<String>? = arrayListOf()//storage内の画像の場所を取得するための配列
    val storage = Firebase.storage
    var ONE_MEGABYTE: Long? = 1024 * 1024 * 10
    var gsReference: StorageReference? = null
    private lateinit var bitmapViewModel: BitmapViewModel
    private lateinit var auth: FirebaseAuth
    var loginFlag: Int = 0
    val mAuth = FirebaseAuth.getInstance()
    val TAG = "MainActivity"


    //Authenticationでログインしているかを確かめる
    /*val currentUser = auth.currentUser
        if (currentUser != null) {
            loginFlag = 1
        }\

         */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //非同期処理 何のため？
        val task = AsyncTaskClass()
        task.execute("")
        //auth = Firebase.auth
        //bitmapViewModel= ViewModelProvider(this).get(BitmapViewModel::class.java)
    }
}

object DatastoreSingleton {
}
