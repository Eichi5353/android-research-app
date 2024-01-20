package com.example.myprototype

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
//Cloud Runに接続
//Cloud RunでFirestoreの情報を取り出して，ソートしている　それを表示している
//Cloud Runのコードがまだ古いもの

class RankingFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyRecyclerViewAdapter
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    var user: FirebaseUser? = null
    var username:String?=""
    var time:Long = 0
    //var responseData: JSONArray? = null
    //flask local
    //private val url = "http://192.168.101.8:5000/"

    //cloud run flask
    private val POST = "POST"
    private val GET = "GET"
    //private val url = "https://myapp-run2-hxk7ud77sq-dt.a.run.app"
//    private val url = "https://myapp-run3-hxk7ud77sq-dt.a.run.app"

    //ais account - Research H
//    private val url = "https://first-test-vb65kt74iq-dt.a.run.app"
    //model-run
    private val url = "https://model-run-vb65kt74iq-an.a.run.app"


    //Cloud Runデバッグ用
    //private val url = "https://8080-cs-262355487553-default.cs-asia-east1-jnrc.cloudshell.dev"
    val TAG = "rankingFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)
        recyclerView = view.findViewById(R.id.recycler) // Viewをインフレートした後にrecyclerViewを初期化
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated")
        auth = Firebase.auth
        user = auth.currentUser
        username = user?.displayName
        //adapter = MyRecyclerViewAdapter(emptyList()) // 初期化されたアダプターを設定
        //recyclerView.adapter = adapter
        //recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //自分のTotalPointだけはここで計算してもいいかも
        sum_point()
        //get-dataのurlに正しく飛べていない，Or get-dataのurlにエラーが生じている
        sendRequest(POST,"get-data","collection_name","users")
        //sendRequest(GET,"user_data",null,null)

    }

    fun sendRequest(type: String, method: String, paramname1: String?, value1: String?) {
        time = measureTimeMillis {
            /* if url is of our get request, it should not have parameters according to our implementation.
            * But our post request should have 'name' parameter. */
            val fullURL = url + "/" + method //+ if (param == null) "" else "/$param"
            val request: Request
            val client: OkHttpClient = OkHttpClient().newBuilder()
                //add this block
                .hostnameVerifier { _, _ -> true } // ホスト名の検証を無効化
                .sslSocketFactory(
                    ResultFragment.TrustAllCertificates.sslSocketFactory(),
                    ResultFragment.TrustAllCertificates.trustManager() // すべての証明書を信頼
                )

                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS).build()

            /* If it is a post request, then we have to pass the parameters inside the request body*/request =
            if (type == POST) {
                val formBody: RequestBody = FormBody.Builder()
                    .add(paramname1!!, value1!!)//ここで値を渡すことができるparamnameをpython側で指定すればvalueを得られる
                    .build()
                Request.Builder()
                    .url(fullURL)
                    .addHeader("Accept", "application/json")
                    .post(formBody)
                    .build()
            } else {
                /*If it's our get request, it doen't require parameters, hence just sending with the url*/
                Request.Builder()
                    .url(fullURL)
                    .build()
            }
            /* this is how the callback get handled */
            client.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        Log.e(TAG, e.toString())

                    }

                    //@Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        Log.d(TAG,"onResponse")
                        // Jsonの形にしたいのですが．．．
                        val responseData = response.body?.string()
                        Log.d(TAG, "response: ${response.toString()}")
                        Log.d(TAG, "responseData: ${responseData.toString()}")

                        val list = parseJSONData(responseData.toString())
                        Log.d(TAG, "sortedList: ${list.toString()}")

                        //なぜかHTML全体のコードが出力される
                        //bodyであっている？

                        // Run view-related code back on the main thread.
                        // Here we display the response message in our text view
                        getActivity()?.runOnUiThread {
                            val adapter = MyRecyclerViewAdapter(list)
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(requireContext())
                            //progressBar?.visibility = ProgressBar.GONE
                        }
                    }
                })
        }
        Log.d(TAG,"処理時間2は${time}ミリ秒です")
    }
    fun parseJSONData(jsonData: String): List<UserInfo> {
        // JSONデータの解析処理をここに追加
        // 例: Gsonライブラリを使用してJSONデータをパースし、DocumentSnapshotのリストに変換
        Log.d(TAG, "Gson")
        val gson = Gson()
        val data: UserData = gson.fromJson(jsonData, UserData::class.java)
        // データをPointの高い順にソート
        val sortedData = data.user_data.sortedByDescending { it.point }
        //val itemType = object : TypeToken<List<String>>() {}.type
        //val stringList: List<String> = gson.fromJson(jsonData, itemType)
        return sortedData
    }



    fun sum_point() {
        val db = FirebaseFirestore.getInstance()

        Log.d(TAG, "getFireStore")

        val placeRef = db.collection("users").document(username.toString()).collection("Places")
        placeRef.get()
            .addOnSuccessListener {  placesSnapshot ->
                Log.d(TAG, "Place collection get: ${username}")
                var totalPoint = 0
                //場所毎に実行
                for (placeDocument in placesSnapshot.documents) {
                    if (placeDocument.exists()) {
                        if (placeDocument.contains("point") && placeDocument["point"] is Number) {
                            Log.d(TAG, "placename:${placeDocument.id}")
                            val point = placeDocument.getLong("point")?.toLong()?: 0
                            Log.d(TAG, "point:${point}")
                            totalPoint += point.toInt()
                        }else {
                            Log.d(TAG, "上　ドキュメントが存在しません。")
                        }
                    } else {
                        Log.d(TAG, "ドキュメントが存在しません。")
                    }
                }
                Log.d(TAG, "${username} Total Point: ${totalPoint}")
                // 合計ポイントをUsersドキュメントに保存
                var userRef = db.collection("users").document(username.toString())
                var point_data = hashMapOf(
                    "totalPoint" to totalPoint
                )
                userRef.update(point_data as Map<String, String>)
                Log.d(TAG, "set totalPoint in ${username}")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
}

class MyRecyclerViewAdapter(val list: List<UserInfo>) : RecyclerView.Adapter<MyViewHolder>() {
    val TAG = "MyRecyclerViewAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view, parent, false)
        return MyViewHolder(itemView)
    }

    /*
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val document = list[position]
        holder.positionText.text = (position+1).toString()
        holder.usernameText.text = document.id.toString()
        val totalPoint = document.getLong("totalPoint") ?: 0
        Log.d(TAG, "totalPoint:${totalPoint}")
        holder.userpointText.text = totalPoint.toString()

    }

     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val userInfo = list[position]
        Log.d(TAG,"userInfo:$userInfo")
        holder.positionText.text = (position + 1).toString()+"位"
        holder.usernameText.text = userInfo.name
        holder.answernumText.text = userInfo.ansNum.toString()
        Log.d(TAG,"Json ansNum : ${userInfo.ansNum.toString()}")
        val totalPoint = userInfo.point
        Log.d(TAG, "totalPoint: $totalPoint")
        holder.userpointText.text = totalPoint.toString()+"点" }

    override fun getItemCount(): Int = list.size

}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val positionText: TextView = itemView.findViewById(R.id.position)
    val usernameText: TextView = itemView.findViewById(R.id.p_username)
    val userpointText: TextView = itemView.findViewById(R.id.user_point)
    val answernumText:TextView = itemView.findViewById(R.id.user_ansNum)

}
// データクラス
data class UserData(val user_data: List<UserInfo>)
data class UserInfo(val name: String, val point: Int,val ansNum:Int)

