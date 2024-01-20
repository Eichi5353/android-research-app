package com.example.myprototype

import android.graphics.Bitmap
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResultAllFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultAllFragment : Fragment() {
    val TAG = "R_AllFragment"
    var scoreText: TextView? =null
    var imgTest: ImageView? =null
    var imgTest2: ImageView? =null
    var live_bitmap: MutableLiveData<Bitmap>? =null
    var query_bitmap: Bitmap?=null
    var taking_bitmap: Bitmap?=null
    var progressBar: ProgressBar? = null
    private lateinit var recyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    var current_user: FirebaseUser? = null

    private lateinit var mapsCountViewModel: MapsCountViewModel

    var totalPoint:Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_result_all, container, false)
        auth = Firebase.auth
        current_user = auth.currentUser
        Log.d(TAG,"current user : $current_user")
        recyclerView = view.findViewById(R.id.recycler) // Viewをインフレートした後にrecyclerViewを初期化
        activity?.run {
            mapsCountViewModel = ViewModelProvider(this).get(MapsCountViewModel::class.java)
        }
        view.findViewById<Button>(R.id.btn_toRanking).setOnClickListener {
            findNavController().navigate(R.id.action_resultAllFragment_to_rankingFragment)
        }
        view.findViewById<Button>(R.id.btn_title).setOnClickListener {
            findNavController().navigate(R.id.action_resultAllFragment_to_titleFragment)
        }
        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var resultList = mutableListOf<ResultInfo>()
        Log.d(TAG,"visitCount: ${mapsCountViewModel.visitCount.value!!}")
        Log.d(TAG,"queryBitmapCount: ${mapsCountViewModel.queryList.indices!!}")

        for(i in 0 until mapsCountViewModel.visitCount.value!!){
            Log.d(TAG,"roop count : $i")
            val resultInfo = ResultInfo(mapsCountViewModel.queryList[i],
                mapsCountViewModel.takingList[i],mapsCountViewModel.resultList[i])
            resultList.add(resultInfo)
            Log.d(TAG,"resultList : ${resultList}")

//            得点をそのユーザに加算していく処理
            if (current_user != null) {
                val username: String = current_user!!.displayName.toString()
                //データベースに追加する得点データ
                Log.d(TAG, "Point is ${resultInfo.score} Point")
                val point_data = hashMapOf(
                    "point" to resultInfo.score.toString()
                )
                Log.d(TAG, "ansNum is :${mapsCountViewModel.visitCount.value.toString()!!}")
                val ansNum_data = hashMapOf(
                    "answerNumber" to mapsCountViewModel.visitCount.value.toString()!!
                )
                val p_numRef = db.collection("users").document(username)
                p_numRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.contains("answerNumber")) {
                            // "answerNumber"フィールドが存在する場合は、既存のデータを更新
                            p_numRef
                                .set(ansNum_data, SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d(TAG, "既存のデータを更新しました: $username: ${mapsCountViewModel.visitCount.value.toString()}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "データ更新中にエラーが発生しました", e)
                                }
                        } else {
                            // "answerNumber"フィールドが存在しない場合は新規にデータをセット
                            p_numRef
                                .set(ansNum_data)
                                .addOnSuccessListener {
                                    Log.d(TAG, "新しいデータをセットしました: $username: ${mapsCountViewModel.visitCount.value.toString()}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "データセット中にエラーが発生しました", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "データ取得中にエラーが発生しました", e)
                    }

                val subRef = db.collection("users").document(username)
                    .collection("Places")
                //.update(point_data as Map<String, String>)
                //.addOnSuccessListener {
                subRef.document("place"+i.toString())
                    .set(point_data)
                    .addOnSuccessListener {
                        Log.d(TAG, "サブコレクションに格納できた: ${username}:${resultInfo.score}")
                    }
                    .addOnFailureListener{ e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            } else
                Log.d(TAG, "Could not find user")
        }
        sum_point()
        Log.d(TAG,"resultList(after loop) : ${resultList}")
        val adapter = ResultRecyclerViewAdapter(resultList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //progressBar?.visibility = ProgressBar.GONE
        val numText:TextView = view.findViewById<TextView>(R.id.txt_num)
        val tPointText:TextView = view.findViewById<TextView>(R.id.txt_tpoint)
        numText.setText("${mapsCountViewModel.visitCount.value}問")
        tPointText.setText("${totalPoint}点")
    }

    fun sum_point() {
        val db = FirebaseFirestore.getInstance()

        Log.d(TAG, "getFireStore")

        val placeRef = db.collection("users").document(current_user.toString()).collection("Places")
        placeRef.get()
            .addOnSuccessListener {  placesSnapshot ->
                Log.d(TAG, "Place collection get: ${current_user}")
                totalPoint = 0
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
                Log.d(TAG, "${current_user} Total Point: ${totalPoint}")
                // 合計ポイントをUsersドキュメントに保存
                var userRef = db.collection("users").document(current_user.toString())
                var point_data = hashMapOf(
                    "totalPoint" to totalPoint
                )
                userRef.get()
                    .addOnSuccessListener { documentSnapshot ->
//                        これ場合分けする意味ない？？？置換じゃダメな時はいつ？
                        if (documentSnapshot.contains("totalPoint")) {
                            // "answerNumber"フィールドが存在する場合は、既存のデータを更新
                            userRef
                                .set(point_data, SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d(TAG, "既存のデータを更新しました: $current_user: ${totalPoint}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "データ更新中にエラーが発生しました", e)
                                }
                        } else {
                            // "answerNumber"フィールドが存在しない場合は新規にデータをセット
                            userRef
                                .set(point_data)
                                .addOnSuccessListener {
                                    Log.d(TAG, "新しいデータをセットしました: $current_user: ${totalPoint}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "データセット中にエラーが発生しました", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "データ取得中にエラーが発生しました", e)
                    }
                userRef.update(point_data as Map<String, String>)
                Log.d(TAG, "set totalPoint in ${current_user}")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }
}



class ResultRecyclerViewAdapter(val list: List<ResultInfo>) : RecyclerView.Adapter<ResultViewHolder>() {
    val TAG = "MyRecyclerViewAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.result_item_recycler, parent, false)
        return ResultViewHolder(itemView)
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
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val resultInfo = list[position]
        Glide.with(holder.itemView.context)
            .load(resultInfo.query)
            .into(holder.queryImageView)
        Glide.with(holder.itemView.context)
            .load(resultInfo.taking)
            .into(holder.takingImageView)
        holder.scoretText.text = resultInfo.score + "点"
        Log.d(TAG, "score: ${resultInfo.score}")
    }

    override fun getItemCount(): Int = list.size

}

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var queryImageView: ImageView = itemView.findViewById(R.id.imageView4)
    var takingImageView: ImageView = itemView.findViewById(R.id.imageView5)
    var scoretText: TextView = itemView.findViewById(R.id.score_is)

}

data class ResultData(val result_data: List<ResultInfo>)
data class ResultInfo(val query:Bitmap, val taking: Bitmap,val score:String)
