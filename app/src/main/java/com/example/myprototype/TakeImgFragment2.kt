package com.example.myprototype

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TakeImgFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
//写真撮影をし，計算もしてしまうFragment
//写真撮影，撮影した写真表示，
// if OK:
//  類似度計算（Httpリクエスト　Coroutine処理)ー＞結果をViewModelに保存 and 画面遷移


class TakeImgFragment2 : Fragment() {
    val TAG = "TakeImg2Fragment"
    private lateinit var mapsCountViewModel: MapsCountViewModel

    var values = null
    var ivCamera: ImageView? = null

    var progressBar: ProgressBar? = null

    //URI=URL+URN URNはその名前を表す
    public var _imageUri: Uri? = null
    lateinit var imgString: String
    var bitmap: Bitmap? = null
    var q_bitmap: Bitmap? = null

    var query_bitmap:Bitmap? = null


    //データ送信用
    val bundle = Bundle()
    public var job: Job? = null
    private val deferredJob = CompletableDeferred<Job?>()

    //位置情報用
    private var mLManager: LocationManager? = null


//    HTTP用
    var responseData: String = ""//結果の類似度
    private val url = "https://model-run-vb65kt74iq-an.a.run.app"
    private val POST = "POST"
    private val GET = "GET"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_take_img2, container, false)
        Log.d(TAG,"TakeImg2 start")
        //ViewModel取得
        activity?.run {
            mapsCountViewModel = ViewModelProvider(this).get(MapsCountViewModel::class.java)
        }
        Log.d(TAG,"arguments : ${arguments?.getString("query_name")}")
        //カメラ
        CameraStrat(view)
//        問題画像取得

        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    //撮影が成功した時の処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //同名メソッド
        Log.d(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == AppCompatActivity.RESULT_OK) {
            //val bitmap = data?.getParcelableExtra<Bitmap>("data")//画像のビットマップデータの取得　getParcelableExtraは数値，文字列以外のデータ型のオブジェクトを取得


            ivCamera = view?.findViewById<ImageView>(R.id.iv)
            //ivCamera?.setImageURI(_imageUri)


            //imageUriをResultFragmentに送る．
            //撮影画像
            val stream: InputStream? = requireContext().contentResolver.openInputStream(_imageUri!!)
            bitmap = null
            ivCamera?.setImageBitmap(null)
            bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))
            Log.d(TAG, "Bitmap: ${bitmap}")
            Log.d(TAG, "_imageUri: ${_imageUri}")

            ivCamera?.setImageBitmap(bitmap)

            val query_file:String? = arguments?.getString("query_name")
//        計算する形に変換する　bitmapー＞Base64
            query_bitmap = getBitmapFromAsset(requireContext(), query_file!!)
            Log.d(TAG,"query bitmap : ${query_bitmap}")
            val string_query = getStringImage(query_bitmap)


            val string_taking = getStringImage(bitmap)
//            Log.d(TAG,"string_query: ${string_query}")
//            Log.d(TAG,"string_taking: ${string_taking}")

            if (string_query != null && string_taking != null) {
                view?.findViewById<Button>(R.id.result_btn)?.setOnClickListener(){
            //            coroutineで類似度計算処理　Httpリクエスト
                    // Coroutineを使用して非同期処理を開始
                    job = CoroutineScope(Dispatchers.Main).launch {
                        // バックグラウンドで実行する処理（HTTPリクエストなど）
                        sendRequest(POST, "/calculate-siamese", "img1", string_query, "img2", string_taking)
                    }
                    findNavController().navigate(R.id.action_takeImgFragment2_to_mapsTestFragment)
                }
            } else {
                Log.d(TAG,"query bitmap is null")
            }
        } else {
            Log.e(TAG, "Failed to request code or result code, bitmap(${bitmap}) is null")
        }
    }


    fun CameraStrat(view: View) {//この引数viewって何なの？　これがないとカメラが開かない
        /*画質が悪いほう
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//カメラ起動のおまじないみたいなもんですか？

        startActivityForResult(intent, 200)
         */


        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val now = Date()
        val nowStr = dateFormat.format(now)

        val fileName = "Myapp1_test1.jpg"

        //valuesが画像データ情報　_imageUriが格納先+values
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")//jpeg?pngでもできる？

        val contentResolver = requireContext().contentResolver
        _imageUri = null
        _imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //intentを保存すればいい？？
        //val image: String =MediaStore.ACTION_IMAGE_CAPTURE
        intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri)
        startActivityForResult(intent, 200)
    }
    fun getBitmapFromAsset(context: Context, fileName: String): Bitmap? {
        val assetManager: AssetManager = context.assets
        val inputStream: InputStream = assetManager.open("BKC/$fileName")
        Log.d(TAG,"from assets bitmap? is ${inputStream}")
        q_bitmap = BitmapFactory.decodeStream(inputStream)
//        Log.d(TAG,"from assets bitmap? is ${q_bitmap}")
//        val string_img = getStringImage(q_bitmap)
//        Log.d(TAG,"from assets String is ${string_img}")
        return q_bitmap
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
        /* if url is of our get request, it should not have parameters according to our implementation.
        * But our post request should have 'name' parameter. */
        val fullURL = url + "/" + method //+ if (param == null) "" else "/$param"
        Log.d(TAG, "request full URL : ${fullURL}")
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
                    Log.d(TAG, "http responseData: ${responseData}")

                    if (mapsCountViewModel.visitCount.value == 0) {
                        Log.e(TAG, "count is 0")
                    } else{
                        getActivity()?.runOnUiThread {
                            Log.d(TAG, "add to resultList ")
                            mapsCountViewModel.resultList.add(responseData)
                            Log.d(TAG, "add to queryList: ${query_bitmap}")
                            mapsCountViewModel.queryList.add(query_bitmap!!)
                            Log.d(TAG, "mapscount.query: ${mapsCountViewModel.queryList}")
                            mapsCountViewModel.takingList.add(bitmap!!)
                            Log.d(TAG, "add to takingList: ${bitmap}")
                            Log.d(TAG, "mapscount.taking: ${mapsCountViewModel.takingList}")
                            mapsCountViewModel.isRequestComplete.value = mapsCountViewModel.isRequestComplete.value!! + 1
                            Log.d(TAG, "mapscount.isRequest: ${mapsCountViewModel.isRequestComplete.value}")

                        }

                    }
//                        if (mapsCountViewModel.visitCount.value == 1) {
//                        Log.d(TAG, "count is 1")
//                        mapsCountViewModel.result1.postValue(responseData)
//                    } else if (mapsCountViewModel.visitCount.value == 2) {
//                        Log.d(TAG, "count is 2")
//                        mapsCountViewModel.result2.postValue(responseData)
//                    } else if (mapsCountViewModel.visitCount.value == 3) {
//                        Log.d(TAG, "count is 3")
//                        mapsCountViewModel.result3.postValue(responseData)
//                    } else if (mapsCountViewModel.visitCount.value == 4) {
//                        Log.d(TAG, "count is 4")
//                        mapsCountViewModel.result4.postValue(responseData)
//                    }
                    Log.d(TAG,"result = ${responseData}")
                }
            })
        }

    val maxCount=20
    var count =0

    public fun useJob(): Job? {

        if(job!=null) {
            return job
        }else if(count<=maxCount){
            count++
            CoroutineScope(Dispatchers.Default).launch {
                delay(2000)
                // このブロック内で非同期処理を行うことも可能
            }
            Log.d(TAG, count.toString())
            return useJob()
        }
        return null
    }
    suspend fun waitForJob() {
        job = deferredJob.await()
    }


}