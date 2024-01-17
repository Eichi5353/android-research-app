package com.example.myprototype

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
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
    var user: FirebaseUser? = null

    private lateinit var mapsCountViewModel: MapsCountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_result_all, container, false)
        auth = Firebase.auth
        user = auth.currentUser
        recyclerView = view.findViewById(R.id.recycler) // Viewをインフレートした後にrecyclerViewを初期化
        activity?.run {
            mapsCountViewModel = ViewModelProvider(this).get(MapsCountViewModel::class.java)
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
            if (user != null) {
                val username: String = user!!.displayName.toString()
                //データベースに追加する得点データ
                Log.d(TAG, "Point is ${resultInfo.score} Point")
                val point_data = hashMapOf(
                    "Point" to resultInfo.score.toString()
                )
                val subRef = db.collection("users").document(username)
                    .collection("Places")
                //.update(point_data as Map<String, String>)
                //.addOnSuccessListener {
                subRef.document(i.toString()+"_place")
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
        Log.d(TAG,"resultList(after loop) : ${resultList}")
        val adapter = ResultRecyclerViewAdapter(resultList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //progressBar?.visibility = ProgressBar.GONE

//for mapsCountViewModel.visitcount ....

//        imgTest = view.findViewById(R.id.imageView4)
//        imgTest2 = view.findViewById(R.id.imageView5)
//        scoreText = view.findViewById(R.id.score_is)
//
//        scoreText = view.findViewById(R.id.score)
//        progressBar = view?.findViewById(R.id.r_progressBar)
//
//        //問題画像
//        query_bitmap = mapsCountViewModel.queryList[0]
//        //撮影画像の取得
//        taking_bitmap = mapsCountViewModel.takingList[0]
//
//        //確認用（問題画像）
//        Glide.with(this)
//            .load(query_bitmap)
//            .into(imgTest!!)
//        //確認用（撮影画像）
//        Glide.with(this)
//            .load(taking_bitmap)
//            .into(imgTest2!!)
//        scoreText!!.text = mapsCountViewModel.resultList[0] + "点！！"
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
