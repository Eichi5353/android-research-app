package com.example.myprototype

import android.graphics.Bitmap
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
 * Use the [ResultAllFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultAllFragment2 : Fragment() {
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
    var displayName:String =""

    private lateinit var mapsCountViewModel: MapsCountViewModel

    var totalPoint:Int = 0

    var numText: TextView? = null
    var tPointText: TextView? =  null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_result_all2, container, false)
        auth = Firebase.auth
        current_user = auth.currentUser
        displayName = current_user?.displayName.toString()

        numText = view!!.findViewById<TextView>(R.id.txt_num)
        tPointText =view!!.findViewById<TextView>(R.id.txt_tpoint)

        Log.d(TAG,"current user : $displayName")
        recyclerView = view.findViewById(R.id.recycler) // Viewをインフレートした後にrecyclerViewを初期化
        activity?.run {
            mapsCountViewModel = ViewModelProvider(this).get(MapsCountViewModel::class.java)
        }
        view.findViewById<Button>(R.id.btn_title).setOnClickListener {
            findNavController().navigate(R.id.action_resultAllFragment2_to_titleFragment)
        }
        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var resultList = mutableListOf<ResultInfo>()
        Log.d(TAG,"visitCount: ${mapsCountViewModel.visitCount2.value!!}")
        Log.d(TAG,"queryBitmapCount: ${mapsCountViewModel.queryList2.indices!!}")

        for(i in 0 until mapsCountViewModel.visitCount2.value!!) {
            Log.d(TAG, "roop count : $i")
            val resultInfo = ResultInfo(
                mapsCountViewModel.queryList2[i],
                mapsCountViewModel.takingList2[i], mapsCountViewModel.resultList2[i]
            )
            resultList.add(resultInfo)
        }
        Log.d(TAG,"resultList(after loop) : ${resultList}")
        val adapter = ResultRecyclerViewAdapter2(resultList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        numText?.setText("${mapsCountViewModel.visitCount2.value}問")
        //progressBar?.visibility = ProgressBar.GONE
    }
}
class ResultRecyclerViewAdapter2(val list: List<ResultInfo>) : RecyclerView.Adapter<ResultViewHolder>() {
    val TAG = "MyRecyclerViewAdapter2"
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
        val ok = -1
        if (resultInfo.score.toInt() == 1){
            holder.scoretText.text = "OK!"
        }else if (resultInfo.score.toInt() == -1){
            holder.scoretText.text = "だめ!違う場所"
        }
    }

    override fun getItemCount(): Int = list.size

}

class ResultViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var queryImageView: ImageView = itemView.findViewById(R.id.imageView4)
    var takingImageView: ImageView = itemView.findViewById(R.id.imageView5)
    var scoretText: TextView = itemView.findViewById(R.id.score_is)

}

data class ResultData2(val result_data: List<ResultInfo>)
data class ResultInfo2(val query:Bitmap, val taking: Bitmap,val score:String)



