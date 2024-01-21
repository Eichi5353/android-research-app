package com.example.myprototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage

// TODO: Rename parameter arguments, choose names that match
//タイトル画面の表示
//提示画像の選択
//現在のユーザー情報の取得

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TitleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TitleFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    var usertext: TextView? = null
    val db = Firebase.firestore

    //choseImg
    var range: IntRange? = null //乱数の範囲を取得するため
    var RefList: ArrayList<String>? = arrayListOf()//storage内の画像の場所を取得するための配列
    val storage = Firebase.storage
    var ONE_MEGABYTE: Long? = 1024 * 1024 * 10
    var gsReference: StorageReference? = null
    var gsReference2: StorageReference? = null
    var gsReference3: StorageReference? = null

    private lateinit var bitmapViewModel: BitmapViewModel
    var loginFlag: Int = 0
    val mAuth = FirebaseAuth.getInstance()
    var bitmap:Bitmap? = null
    var bitmap2:Bitmap? = null
    var bitmap3:Bitmap? = null



    //var size = 0
    var random_num = 0
    var result: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_title, container, false)
        activity?.run {
            bitmapViewModel = ViewModelProvider(this).get(BitmapViewModel::class.java)
            Log.i(TAG, "Called ViewModelProvider.get")
        }
        auth = Firebase.auth


        usertext = view?.findViewById(R.id.cuurent_usename)
        val user = auth.currentUser
        Log.d(TAG, "user: ${user}")
        view.findViewById<Button>(R.id.btn).setOnClickListener{
            findNavController().navigate(R.id.action_titleFragment_to_mapsTestFragment)
        }
        view.findViewById<Button>(R.id.btn_game2).setOnClickListener{
            findNavController().navigate(R.id.action_titleFragment_to_mapsFragment2)
        }
        view.findViewById<Button>(R.id.btn_go_ranking).setOnClickListener{
            findNavController().navigate(R.id.action_titleFragment_to_rankingFragment)
        }
        view.findViewById<Button>(R.id.btn_rule).setOnClickListener{
            findNavController().navigate(R.id.action_titleFragment_to_ruleFragment)
        }

//        view.findViewById<Button>(R.id.btn_test).setOnClickListener{
//            findNavController().navigate(R.id.action_titleFragment_to_mapsTestFragment)
//        }
        Log.d(TAG, "Hi")
        if(user != null){
            val username:String = user.displayName.toString()
            Log.d(TAG, "username: ${username}")
            val message:String = "さんようこそ！"
            val combine = username+message
            usertext?.setText(combine)


            //ここから下は，Registerでやってデータベースに追加すべき
            //ドキュメントIDは自分でわかりやすいものに設定しましょう
            /*val db_user = hashMapOf<String,Any>(
                "name" to username,
            )
            Log.d(TAG, "Hello")
            val ref = db.collection("users").document(username)
            Log.d(TAG, "Document ID: ${ref}")
            db.collection("users").document(username)
                .set(db_user)
                .addOnSuccessListener {
                    Log.d(TAG, "username added with ID: ${username}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }

             */
/*
            val user1 = hashMapOf<String,Any>(
                "totalPoint" to "142"
            )
            Log.d(TAG, "Hello")
            val ref1 = db.collection("users").document("kento")
            ref1.set(user1)
                .addOnSuccessListener {
                    Log.d(TAG, "username added with ID: ${username}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            val user2 = hashMapOf<String,Any>(
                "totalPoint" to "172"
            )
            Log.d(TAG, "Hello")
            val ref2 = db.collection("users").document("yuto")
            ref2.set(user2)
                .addOnSuccessListener {
                    Log.d(TAG, "username added with ID: ${username}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            val user3 = hashMapOf<String,Any>(
                "totalPoint" to "141"
            )
            Log.d(TAG, "Hello")
            val ref3 = db.collection("users").document("haruki")
            ref3.set(user3)
                .addOnSuccessListener {
                    Log.d(TAG, "username added with ID: ${username}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            val user4 = hashMapOf<String,Any>(
                "totalPoint" to "188"
            )
            Log.d(TAG, "Hello")
            val ref4 = db.collection("users").document("kotaro")
            ref4.set(user4)
                .addOnSuccessListener {
                    Log.d(TAG, "username added with ID: ${username}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            val user5 = hashMapOf<String,Any>(
                "totalPoint" to "190"
            )
            Log.d(TAG, "Hello")
            val ref5 = db.collection("users").document("taisei")
            ref5.set(user5)
                .addOnSuccessListener {
                    Log.d(TAG, "username added with ID: ${username}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }

 */

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //choseImg method
        chooseImg()
        view?.findViewById<Button>(R.id.btn_logout).setOnClickListener(){
            //ログアウトして，ログイン画面に戻る を書け
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_titleFragment_to_loginFragment)
        }
    }

    fun chooseImg(){
        //size = 0
        random_num = 0
        result = null
        val listRef = storage.reference.child("BKC")
        //val listRef = storage.reference.child("images")

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
                random_num = range?.random()!!
                //選ばれた配列
                result = RefList!!.getCircularIndex(random_num!! - 1)
                Log.i("Storage", "random_num is $random_num")
                Log.i("Storage", "random_item is $result")

                gsReference = storage.getReferenceFromUrl(result!!)
                ONE_MEGABYTE = 1024 * 1024 * 10 //最大ダウンロードできるバイトサイズ　小さいとエラーになる

                //URLからBitmapを得る変換1
                gsReference?.getBytes(ONE_MEGABYTE!!)?.addOnSuccessListener {
                    val new_bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    Log.d(TAG,"Bitmap value: ${new_bitmap}")

                    bitmap = new_bitmap


                    //ViewModelに保存
                    bitmapViewModel.mBitmap.value = new_bitmap
                    Log.d(TAG,"viewModel_Bitmap value: ${bitmapViewModel.mBitmap.value}")
                }?.addOnFailureListener {
                    // Handle any errors
                    Log.e(TAG, "False")
                }

                //2
                val result2 = RefList!!.getCircularIndex(random_num!! - 2)
                gsReference2 = storage.getReferenceFromUrl(result2!!)
                gsReference2?.getBytes(ONE_MEGABYTE!!)?.addOnSuccessListener {
                    val new_bitmap2 = BitmapFactory.decodeByteArray(it, 0, it.size)
                    Log.d(TAG,"Bitmap value2: ${new_bitmap2}")
                    bitmap2 = new_bitmap2
                    //ViewModelに保存
                    bitmapViewModel.mBitmap2.value = new_bitmap2
                    Log.d(TAG,"viewModel_Bitmap value: ${bitmapViewModel.mBitmap2.value}")
                }?.addOnFailureListener {
                    // Handle any errors
                    Log.e(TAG, "False")
                }

                //3
                val result3 = RefList!!.getCircularIndex(random_num!! - 3)
                gsReference3 = storage.getReferenceFromUrl(result3!!)
                gsReference3?.getBytes(ONE_MEGABYTE!!)?.addOnSuccessListener {
                    val new_bitmap3 = BitmapFactory.decodeByteArray(it, 0, it.size)
                    Log.d(TAG,"Bitmap value3: ${new_bitmap3}")
                    bitmap3 = new_bitmap3
                    //ViewModelに保存
                    bitmapViewModel.mBitmap3.value = new_bitmap3
                    Log.d(TAG,"viewModel_Bitmap value: ${bitmapViewModel.mBitmap3.value}")
                }?.addOnFailureListener {
                    // Handle any errors
                    Log.e(TAG, "False")
                }

                //URLからURIに変換
                val imageUri = Uri.parse(gsReference.toString())
                val imageUri2 = Uri.parse(gsReference2.toString())
                val imageUri3 = Uri.parse(gsReference3.toString())

                Log.d(TAG, "storageのURIは${imageUri}")
                Log.d(TAG, "storageのURIは${imageUri2}")
                Log.d(TAG, "storageのURIは${imageUri3}")

                //値をセット
                bitmapViewModel.mUri.value = imageUri.toString()
                bitmapViewModel.mUri2.value = imageUri2.toString()
                bitmapViewModel.mUri3.value = imageUri3.toString()


                //bitmapViewModel.mUrl.value = gsReference//??


            }
            .addOnFailureListener {
                Log.e(TAG, "False")// Uh-oh, an error occurred!
            }
    }

    companion object {
        val TAG ="TitleFragment"
    }
    fun <T> List<T>.getCircularIndex(index: Int): T {
        if (isEmpty()) {
            throw NoSuchElementException("List is empty")
        }

        val size = size
        var normalizedIndex = index % size

        if (normalizedIndex < 0) {
            normalizedIndex += size
        }

        return get(normalizedIndex)
    }

}