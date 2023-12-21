package com.example.myprototype

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.cos
import kotlin.system.measureTimeMillis



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment() {

    private lateinit var bitmapViewModel: BitmapViewModel
    private lateinit var visitViewModel: VisitCountViewModel

    var scoreText: TextView? =null
    var imgTest: ImageView? =null
    var imgTest2: ImageView? =null
    var live_bitmap: MutableLiveData<Bitmap>? =null
    var bitmap1:Bitmap?=null
    var bitmap2:Bitmap?=null
    var num:TextView?=null
    var progressBar: ProgressBar? = null
    var responseData: String = "1"//結果の類似度
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    var user: FirebaseUser? = null
    var mUri:String? = null
    var extractedString:String? = null
    //M_Project内の「flask_server.py」を実行したうえで結果を表示する
    //一応このプロジェクトのPythonファイル群の中にも入っている
    //Flask local
    //private val url = "http://192.168.101.8:5000/"
    //for fastAPI local
    //private var url = "http://127.0.0.1:8000/"

    //Cloud Run url  flask
    //private val url = "https://myapp-run2-hxk7ud77sq-dt.a.run.app"
    //private val url = "https://myapp-run3-hxk7ud77sq-dt.a.run.app"
    //akaze???
//    private val url = "https://myapp-run4-akaze-hxk7ud77sq-dt.a.run.app"

    //ais account - Research H
    private val url = "https://first-test-vb65kt74iq-dt.a.run.app"

    //local
    //private val url = "https://8080-cs-262355487553-default.cs-asia-east1-jnrc.cloudshell.dev"

    private val POST = "POST"
    private val GET = "GET"

    var time2:Long = 0
    // putXXXXに対応するgetXXXXで値を取得 //TakeImgFragmentからImgのString取得
    //val stringImg2 = arguments?.getString("takeImg")
    val TAG = "ResultFragment"


    //gps用
    var gpsjudge:Boolean = false
    //半径30m以内をOkとする
    val distance_range = 0.03
    var downloadedmUri: Uri? = null
    var downloadedtUri: Uri? = null
    var mlat:Float = 0.0f
    var mlon :Float = 0.0f
    var tlat:Float = 0.0f
    var tlon :Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitViewModel = ViewModelProvider(this).get(VisitCountViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        auth = Firebase.auth
        user = auth.currentUser

        activity?.run {
            bitmapViewModel = ViewModelProvider(this).get(BitmapViewModel::class.java)
            Log.i(TAG, "Called ViewModelProvider.get")
        }
        view.findViewById<Button>(R.id.btn_title).setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_titleFragment)
        }
        view.findViewById<Button>(R.id.btn_toRanking).setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_rankingFragment)
        }
        //問題画像と撮影画像のURIを取得したい　Jpegを取得できるならそちらの方が良い？
        //FirebaseのストレージからURI取得できる？
        //-> URLから画像をダウンロードして，URIを入手して．．．

        //撮影のフラグメントでURIをViewModelに保存してここで使えばいいかなあ？
        var count = visitViewModel.visitCountLiveData.value
        Log.i(TAG, "visitViewModel =visitCount:${count}")
        visitViewModel.incrementVisitCount()
        //count = visitViewModel.getVisitCount().value
        count = visitViewModel.visitCountLiveData.value
        Log.i(TAG, "visitViewModel =visitCount:${count}")
        //値が変化した場合にViewModelの値にセットするように処理している
        val visitCountObserver = Observer<Int> { newVisitCount ->
            // Update the UI, in this case, a TextView.
            count = newVisitCount
        }
        visitViewModel.visitCountLiveData.observe(viewLifecycleOwner, visitCountObserver)
        visitViewModel.visitCountLiveData.value = count
        Log.i(TAG, "visitViewModel =visitCount:${visitViewModel.getVisitCount().value}")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgTest=view.findViewById(R.id.imageView4)
        imgTest2=view.findViewById(R.id.imageView5)

        scoreText = view.findViewById(R.id.score)
        progressBar = view?.findViewById(R.id.r_progressBar)

        //問題画像
        bitmap1 = bitmapViewModel.mBitmap.value
        val stringImg1 =getStringImage(bitmap1)
        //Log.d(TAG,"stringImg2の値は$stringImg1")
        //撮影画像の取得
        bitmap2 = bitmapViewModel.tBitmap.value//getValue()
        val stringImg2 =getStringImage(bitmap2)

        mUri = bitmapViewModel.mUri.value
        Log.d(TAG, "FireStorage gsName: ${mUri}")
        //Log.d(TAG,"stringImg2の値は$stringImg2")

        //確認用（問題画像）
        Glide.with(this)
            .load(bitmap1)
            .into(imgTest!!)
        //確認用（撮影画像）
        Glide.with(this)
            .load(bitmap2)
            .into(imgTest2!!)





        //位置が合っていれば，得点計算をする
        progressBar?.visibility = ProgressBar.VISIBLE
        val time1 = measureTimeMillis {
            if (stringImg1?.isEmpty() == true && stringImg2?.isEmpty() == true) {
                Log.e(TAG, "cannot send")
                //Log.d(TAG,stImg1)
                scoreText?.setError("This cannot be empty for post request")
                progressBar?.visibility = ProgressBar.GONE
            } else {
                /*if name text is not empty,then call the function to make the post request*/
                Log.d(TAG, "sendPost")
                //Log.d(TAG,stImg1!!)//ちゃんとした値が存在
                //ここが問題　値がサーバー側へしっかり与えられていない
                sendRequest(POST, "/calculate-similarity", "img1", stringImg1, "img2", stringImg2)
                //sendRequest_test(GET, "/")

                //sendRequest(POST, "img/post", )
                // textView_response2?.setText("POST img!")
                //sendRequest(POST, "getimg", "img2", stImg2)
            }
        }
        Log.d(TAG,"処理時間は${time1}ミリ秒です")
        Log.d(TAG,"処理時間合計は${time1+time2}ミリ秒です")



    }

    private fun getStringImage(bitmap: Bitmap?):String?{//Bitmap? or Disposable? どっちでもいける？
        val baos = ByteArrayOutputStream() //byte型，string型変換のための川（通り道）
        //bitmapをcompressによって圧縮
        //第二引数は０~100でふつう100でおk　第三引数のbaosは上のコードと合わせて決まりみたいなもの？
        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,baos)//baosの中に画像データが入ってる？
        val imageBytes =baos.toByteArray()
        val encodeImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return encodeImage
    }

    fun sendRequest(type: String, method: String, paramname1: String?, value1: String?,paramname2: String?, value2: String?) {
        time2 = measureTimeMillis {
            /* if url is of our get request, it should not have parameters according to our implementation.
            * But our post request should have 'name' parameter. */
            val fullURL = url + "/" + method //+ if (param == null) "" else "/$param"
            val request: Request
            val client: OkHttpClient = OkHttpClient().newBuilder()
                    //add this block
                .hostnameVerifier { _, _ -> true } // ホスト名の検証を無効化
                .sslSocketFactory(
                    TrustAllCertificates.sslSocketFactory(),
                    TrustAllCertificates.trustManager() // すべての証明書を信頼
                )

                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS).build()

            /* If it is a post request, then we have to pass the parameters inside the request body*/request =
            if (type == POST) {
                val formBody: RequestBody = FormBody.Builder()
                    .add(paramname1!!, value1!!)//ここで値を渡すことができるparamnameをpython側で指定すればvalueを得られる
                    .add(paramname2!!, value2!!)//ここで値を渡すことができるparamnameをpython側で指定すればvalueを得られる
                    .build()
                Request.Builder()
                    .url(fullURL)
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

                        // Read data on the worker thread
                        responseData = response.body!!.string()
                        Log.d(TAG, responseData)


                        getActivity()?.runOnUiThread {
                            //全部の得点を表示させたい！！
                            scoreText!!.text = responseData + "点！！"
                            progressBar?.visibility = ProgressBar.INVISIBLE
                        }
                        //正規表現で名前抽出
                        val pattern = ".*\\/([a-zA-Z0-9_]+)\\.jpg\$".toRegex()
                        val matchResult = pattern.find(mUri.toString())
                        extractedString = matchResult?.groupValues?.get(1)
                        Log.d(TAG, "Locate Name: ${extractedString}")
                        Log.d(TAG, "place0: ${matchResult?.groupValues?.get(0)}")
                        if (user != null) {
                            val username: String = user!!.displayName.toString()
                            //データベースに追加する得点データ
                            Log.d(TAG, "Point is ${responseData} point")
                            val point_data = hashMapOf(
                                "point" to responseData.toDoubleOrNull()
                            )
                            val subRef = db.collection("users").document(username)
                                .collection("Places")
                            //.update(point_data as Map<String, String>)
                            //.addOnSuccessListener {
                            subRef.document(extractedString.toString())
                                .set(point_data)
                                .addOnSuccessListener {
                                    Log.d(TAG, "サブコレクションに格納できた: ${username}:${responseData}")
                                }
                                .addOnFailureListener{ e ->
                                    Log.w(TAG, "Error adding document", e)
                                }
                        } else
                            Log.d(TAG, "Could not find user")


/*
                        //3問やらせる場合
                        // Run view-related code back on the main thread.
                        // Here we display the response message in our text view
                        if (visitViewModel.getVisitCount().value!! == 1){
                            //ViewModelにresult１　result1に結果を格納
                            bitmapViewModel.result1.postValue(responseData.toString())
                            getActivity()?.runOnUiThread {
                                scoreText!!.text = "もう2問やれ"
                                progressBar?.visibility = ProgressBar.GONE
                            }
                            //add point to database
                            //get image_name from url
                            Log.d(TAG, "FireStorage gsName2: ${mUri}")
                            //正規表現で名前抽出
                            val pattern = ".*\\/([a-zA-Z0-9_]+)\\.jpg\$".toRegex()
                            val matchResult = pattern.find(mUri.toString())
                            extractedString = matchResult?.groupValues?.get(1)
                            Log.d(TAG, "Locate Name: ${extractedString}")
                            Log.d(TAG, "place0: ${matchResult?.groupValues?.get(0)}")
                            bitmapViewModel.rmBitmap.postValue(bitmap1)
                            bitmapViewModel.rtBitmap.postValue(bitmap2)


                            if (user != null) {
                                val username: String = user!!.displayName.toString()
                                //データベースに追加する得点データ
                                Log.d(TAG, "Point is ${responseData} point")
                                val point_data = hashMapOf(
                                    "point" to responseData.toString()
                                )
                                val subRef = db.collection("users").document(username)
                                    .collection("Places")
                                //.update(point_data as Map<String, String>)
                                //.addOnSuccessListener {
                                subRef.document(extractedString.toString())
                                    .set(point_data)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "サブコレクションに格納できた: ${username}:${responseData}")
                                    }
                                    .addOnFailureListener{ e ->
                                        Log.w(TAG, "Error adding document", e)
                                    }
                            } else
                                Log.d(TAG, "Could not find user")

                        }else if(visitViewModel.getVisitCount().value!! == 2){
                            //result2に格納
                            bitmapViewModel.result2.postValue(responseData.toString())
                            getActivity()?.runOnUiThread {
                                scoreText!!.text = "もう1問やれ"
                                progressBar?.visibility = ProgressBar.GONE
                            }
                            //add point to database
                            //get image_name from url
                            Log.d(TAG, "FireStorage gsName2: ${mUri}")
                            //正規表現で名前抽出
                            val pattern = ".*\\/([a-zA-Z0-9_]+)\\.jpg\$".toRegex()
                            val matchResult = pattern.find(mUri.toString())
                            extractedString = matchResult?.groupValues?.get(1)
                            Log.d(TAG, "Locate Name: ${extractedString}")
                            Log.d(TAG, "place0: ${matchResult?.groupValues?.get(0)}")
                            bitmapViewModel.rmBitmap2.postValue(bitmap1)
                            bitmapViewModel.rtBitmap2.postValue(bitmap2)


                            if (user != null) {
                                val username: String = user!!.displayName.toString()
                                //データベースに追加する得点データ
                                Log.d(TAG, "Point is ${responseData} point")
                                val point_data = hashMapOf(
                                    "point" to responseData.toString()
                                )
                                val subRef = db.collection("users").document(username)
                                    .collection("Places")
                                //.update(point_data as Map<String, String>)
                                //.addOnSuccessListener {
                                subRef.document(extractedString.toString())
                                    .set(point_data)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "サブコレクションに格納できた: ${username}:${responseData}")
                                    }
                                    .addOnFailureListener{ e ->
                                        Log.w(TAG, "Error adding document", e)
                                    }
                            } else
                                Log.d(TAG, "Could not find user")
                        }else if(visitViewModel.getVisitCount().value!! == 3){
                            getActivity()?.runOnUiThread {
                                bitmapViewModel.result3.postValue(responseData.toString())
                                //全部の得点を表示させたい！！
                                scoreText!!.text = "OK!  画面遷移します　そのままで"
                                progressBar?.visibility = ProgressBar.GONE

                                val fragmentTransaction = fragmentManager?.beginTransaction()
                                val targetFragment = ResultAllFragment()

                                fragmentTransaction?.replace(R.id.container, targetFragment)
                                //fragmentTransaction?.addToBackStack(null) // バックスタックに追加する場合
                                fragmentTransaction?.commit()

                            }
                            //add point to database
                            //get image_name from url
                            Log.d(TAG, "FireStorage gsName2: ${mUri}")
                            //正規表現で名前抽出
                            val pattern = ".*\\/([a-zA-Z0-9_]+)\\.jpg\$".toRegex()
                            val matchResult = pattern.find(mUri.toString())
                            extractedString = matchResult?.groupValues?.get(1)
                            Log.d(TAG, "Locate Name: ${extractedString}")
                            Log.d(TAG, "place0: ${matchResult?.groupValues?.get(0)}")
                            bitmapViewModel.rmBitmap3.postValue(bitmap1)
                            bitmapViewModel.rtBitmap3.postValue(bitmap2)


                            if (user != null) {
                                val username: String = user!!.displayName.toString()
                                //データベースに追加する得点データ
                                Log.d(TAG, "Point is ${responseData} point")
                                val point_data = hashMapOf(
                                    "point" to responseData.toString()
                                )
                                val subRef = db.collection("users").document(username)
                                    .collection("Places")
                                //.update(point_data as Map<String, String>)
                                //.addOnSuccessListener {
                                subRef.document(extractedString.toString())
                                    .set(point_data)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "サブコレクションに格納できた: ${username}:${responseData}")
                                    }
                                    .addOnFailureListener{ e ->
                                        Log.w(TAG, "Error adding document", e)
                                    }
                            } else
                                Log.d(TAG, "Could not find user")
                        }else{
                            //error処理いりますか？
                        }

 */




                    }
                })
        }
        Log.d(TAG,"処理時間2は${time2}ミリ秒です")
    }

    private fun getImageLocation(uri: Uri): Pair<Float, Float> {
        val contentResolver = requireContext().contentResolver
        val inst = contentResolver.openInputStream(uri)
        val exif = ExifInterface(inst!!)

        //クリコアの場合(北半球，緯度，東経，経度）
        //-> N | 34/1,97/1,97/1 | E | 135/1,96/1,37/1
        val latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)//N or S
        Log.d(TAG, "N or S -> ${latitudeRef.toString()}")
        val latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)//緯度【度 (degrees): 分 (minutes): 秒 (seconds)】
        Log.d(TAG, "latitude -> ${latitude.toString()}")//60進数なので，10進数の変える必要あり！
        val longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)//E　or W
        Log.d(TAG, "E or W -> ${longitudeRef.toString()}")
        val longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)//経度
        Log.d(TAG, "longitude -> ${longitude.toString()}")


        val latDegrees = convertToDegree(latitude!!)
        Log.d(TAG, "latitude -> ${latDegrees.toString()}")
        val lonDegrees = convertToDegree(longitude!!)
        Log.d(TAG, "longitude -> ${lonDegrees.toString()}")

        //どっちが緯度か経度かわかんなくなったから適当に合わせた　もっと分かりやすく書ければなお良し
        val lon = if (latitudeRef == "N") latDegrees else -latDegrees//経度
        val lat = if (longitudeRef == "E") lonDegrees else -lonDegrees//緯度

        return Pair(lat,lon)
    }

    private fun convertToDegree(coordinate: String): Float {
        val parts = coordinate.split("/",",").toTypedArray()//.replace(",", "").
        val degrees = parts[0].toDouble()
        val minutes = parts[2].toDouble()/60
        val seconds = parts[4].toDouble() / parts[5].toDouble()/3600
        Log.d(TAG, "minetes = ${parts[1].toString()}")
        Log.d(TAG, "minetes = ${minutes.toString()}")
        return (degrees + minutes + seconds).toFloat()

    }

    object TrustAllCertificates {
        fun sslSocketFactory(): SSLSocketFactory {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            return sslContext.socketFactory
        }

        fun trustManager(): X509TrustManager {
            return object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        }
    }

    fun sendRequest_test(type: String, method: String) {
        time2 = measureTimeMillis {
            /* if url is of our get request, it should not have parameters according to our implementation.
            * But our post request should have 'name' parameter. */
            val fullURL = url + "/" + method //+ if (param == null) "" else "/$param"
            val request: Request
            Log.d(TAG, "request: ${fullURL}")
            val client: OkHttpClient = OkHttpClient().newBuilder()
                //add this block
                .hostnameVerifier { _, _ -> true } // ホスト名の検証を無効化
                .sslSocketFactory(
                    TrustAllCertificates.sslSocketFactory(),
                    TrustAllCertificates.trustManager() // すべての証明書を信頼
                )

                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS).build()

            /* If it is a post request, then we have to pass the parameters inside the request body*/request =
            if (type == POST) {
                val formBody: RequestBody = FormBody.Builder()
                    .build()
                Request.Builder()
                    .url(fullURL)
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
                        Log.d(TAG, "onResponse")
                        // Read data on the worker thread
                        responseData = response.body!!.string()
                        Log.d(TAG, responseData)
                        //なぜかHTML全体のコードが出力される
                        //bodyであっている？

                        // Run view-related code back on the main thread.
                        // Here we display the response message in our text view
                        getActivity()?.runOnUiThread {
                            scoreText!!.text = responseData + "点！！"
                            progressBar?.visibility = ProgressBar.GONE
                        }


                        //add point to database
                        //get image_name from url
                        //mUri = bitmapViewModel.mUri.value
                        Log.d(TAG, "FireStorage gsName: ${mUri}")
                        //正規表現で名前抽出
                        val pattern = ".*\\/([a-zA-Z0-9_]+)\\.jpg\$".toRegex()
                        val matchResult = pattern.find(mUri.toString())
                        extractedString = matchResult?.groupValues?.get(1)
                        Log.d(TAG, "Locate Name: ${extractedString}")
                        Log.d(TAG, "place0: ${matchResult?.groupValues?.get(0)}")


                        if (user != null) {
                            val username: String = user!!.displayName.toString()
                            //データベースに追加する得点データ
                            Log.d(TAG, "Point is ${responseData} Point")
                            val point_data = hashMapOf(
                                "Point" to responseData.toString()
                            )
                            val subRef = db.collection("users").document(username)
                                .collection("Places")
                            //.update(point_data as Map<String, String>)
                            //.addOnSuccessListener {
                            subRef.document(extractedString.toString())
                                .set(point_data)
                                .addOnSuccessListener {
                                    Log.d(TAG, "サブコレクションに格納できた: ${username}:${responseData}")
                                }
                                .addOnFailureListener{ e ->
                                    Log.w(TAG, "Error adding document", e)
                                }
                        } else
                            Log.d(TAG, "Could not find user")
                    }
                })
        }
        Log.d(TAG,"処理時間2は${time2}ミリ秒です")
    }

}
/*
view.findViewById<Button>(R.id.btn_test).setOnClickListener {

    if (bitmapViewModel.getBitmap() != null) {
        val a = true
        Log.d(TAG, "ViewModel.getは$a")
        Log.d(TAG, "${bitmapViewModel.getBitmap()}")
        live_bitmap = bitmapViewModel.getBitmap()
        bitmap1 = bitmapViewModel.mBitmap.getValue()
        Log.d(TAG,"bitmapはーーー$bitmap1")
        //ViewModelの中身がないから表示ができていない
        Glide.with(this)
            .load(bitmap1)
            .into(imgTest!!)
    } else {
        //中身ないってよ
        val a = false
        Log.d(TAG, "ViewModel.getは$a")
    }
}

 */