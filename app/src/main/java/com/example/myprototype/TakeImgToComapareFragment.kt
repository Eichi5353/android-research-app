package com.example.myprototype

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.delay


class TakeImgToComapareFragment : Fragment() {
    val TAG = "TakeImg2Fragment"
    private lateinit var mapsCountViewModel: MapsCountViewModel

    var ivCamera: ImageView? = null
    var ivQuery: ImageView? = null


    var scoretxt: TextView? = null

    //URI=URL+URN URNはその名前を表す
    public var _imageUri: Uri? = null
    lateinit var imgString: String
    var bitmap: Bitmap? = null
    var q_bitmap: Bitmap? = null

    var query_bitmap: Bitmap? = null
    //    撮影画像GPS
    var take_gps: LatLng? = null
    var query_gps: LatLng? =null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // 任意の値

    public var job: Job? = null




    //位置情報用
    private var mLManager: LocationManager? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_take_img_to_comapare, container, false)
        Log.d(TAG,"TakeImg2 start")
        //ViewModel取得
        activity?.run {
            mapsCountViewModel = ViewModelProvider(this).get(MapsCountViewModel::class.java)
        }
        Log.d(TAG,"arguments : ${arguments?.getString("query_name")}")
        //カメラ
        CameraStrat(view!!)
//        問題画像取得

        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated queryButtonCount: ${mapsCountViewModel.queryButtonTouchCount.value}")
        view?.findViewById<Button>(R.id.result_btn)?.setOnClickListener(){
            findNavController().navigate(R.id.action_takeImgToComapareFragment_to_mapsFragment2)
        }


    }
    //撮影が成功した時の処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //同名メソッド
        Log.d(TAG, "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == AppCompatActivity.RESULT_OK) {
            //val bitmap = data?.getParcelableExtra<Bitmap>("data")//画像のビットマップデータの取得　getParcelableExtraは数値，文字列以外のデータ型のオブジェクトを取得

            GlobalScope.launch(Dispatchers.Main) {
                startLocationUpdates()

                // take_gpsの更新を待つ
                while (take_gps == null) {
                    delay(500) // 100ミリ秒ごとにチェック
                }

                // ここでtake_gpsが利用可能
                Log.d(TAG, "take_gps: $take_gps")
                ivCamera = view?.findViewById<ImageView>(R.id.iv)
                ivQuery = view?.findViewById<ImageView>(R.id.iv_query)

                //ivCamera?.setImageURI(_imageUri)
                //imageUriをResultFragmentに送る．
                //撮影画像
//            たまにエラーが起こる　何かと競合している？？？ほかの画像ファイルやGPS取得
                if (_imageUri != null) {
                    val stream: InputStream? =
                        requireContext().contentResolver.openInputStream(_imageUri!!)
                    bitmap = null
                    ivCamera?.setImageBitmap(null)
                    bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))
                    Log.d(TAG, "Bitmap: ${bitmap}")
                    Log.d(TAG, "_imageUri: ${_imageUri}")

                    ivCamera?.setImageBitmap(bitmap)

                    val query_file: String? = arguments?.getString("query_name")
                    Log.d(TAG, "query_fileName : $query_file")

//                変えるところ！！
                    if (query_file == "ichi") {
                        mapsCountViewModel.isFirstPlaceComplete.value = true
                        Log.d(TAG, "isFirst True")
                        mapsCountViewModel.queryButtonTouchCount.value =
                            mapsCountViewModel.queryButtonTouchCount.value!! + 1
                        Log.d(
                            TAG,
                            "queryButtonCount: ${mapsCountViewModel.queryButtonTouchCount.value}"
                        )
                    } else if (query_file == "nitro") {
                        mapsCountViewModel.isSecondPlaceComplete.value = true
                        Log.d(TAG, "isSecond True")
                        mapsCountViewModel.queryButtonTouchCount.value =
                            mapsCountViewModel.queryButtonTouchCount.value!! + 1
                        Log.d(
                            TAG,
                            "queryButtonCount: ${mapsCountViewModel.queryButtonTouchCount.value}"
                        )

                    } else if (query_file == "crecore") {
                        mapsCountViewModel.isThirdPlaceComplete.value = true
                        mapsCountViewModel.queryButtonTouchCount.value =
                            mapsCountViewModel.queryButtonTouchCount.value!! + 1
                        Log.d(
                            TAG,
                            "queryButtonCount: ${mapsCountViewModel.queryButtonTouchCount.value}"
                        )

                    }

//問題画像の位置情報をjsonファイルから取得
                    val assetManager = context!!.assets
                    val jsonInputStream = assetManager.open("BKC_data/$query_file.json")
                    val jsonString = jsonInputStream.bufferedReader().use { it.readText() }

// JSON データを解析して利用
                    val jsonObject = JSONObject(jsonString)
                    val locationObject = jsonObject.getJSONObject("location")
                    Log.d(TAG, "location from json: ${locationObject}")
                    query_gps = LatLng(
                        locationObject.getDouble("latitude"),
                        locationObject.getDouble("longitude")
                    )
                    Log.d(TAG, "query_gps: ${query_gps}")

                    query_bitmap = getBitmapFromAsset(requireContext(), query_file!!)
                    Log.d(TAG, "query bitmap : ${query_bitmap}")
                    ivQuery?.setImageBitmap(null)
                    ivQuery?.setImageBitmap(query_bitmap)


                    val distance = calculateDistance(
                        take_gps!!.latitude, take_gps!!.longitude, query_gps!!.latitude,
                        query_gps!!.longitude
                    )
                    Log.d(TAG, "query and taking gps distance: $distance km")
                    Log.d(TAG, "add to queryList: ${query_bitmap}")
                    mapsCountViewModel.queryList2.add(query_bitmap!!)
                    Log.d(TAG, "mapscount.query: ${mapsCountViewModel.queryList}")
                    mapsCountViewModel.takingList2.add(bitmap!!)
                    Log.d(TAG, "add to takingList: ${bitmap}")
                    Log.d(TAG, "mapscount.taking: ${mapsCountViewModel.takingList}")
                    scoretxt = view?.findViewById<TextView>(R.id.txt_score)
                    if (distance <= 0.05) {
                        val onGPS: Boolean = true
                        scoretxt?.setText("OK!")
                        val response = 1
                        mapsCountViewModel.resultList2.add(response.toString())
                    } else {
                        Log.d(TAG, "GPS no")
                        scoretxt?.setText("だめ！違う")
                        val response = -1
                        mapsCountViewModel.resultList2.add(response.toString())
                    }
                } else {
                    Log.e(TAG, "Failed to open InputStream for $_imageUri \ncamera again")
                    CameraStrat(view!!)
                }
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
        val inputStream: InputStream = assetManager.open("BKC/$fileName.jpg")
        Log.d(TAG,"from assets bitmap? is ${inputStream}")
        q_bitmap = BitmapFactory.decodeStream(inputStream)
//        Log.d(TAG,"from assets bitmap? is ${q_bitmap}")
//        val string_img = getStringImage(q_bitmap)
//        Log.d(TAG,"from assets String is ${string_img}")
        return q_bitmap
    }

    fun calculateDistance(a: Double, b: Double, c: Double, d: Double): Double {
        val earthRadius = 6371.0 // 地球の半径（キロメートル）

        val distance = 2 * earthRadius * asin(
            sqrt(
                sin((Math.toRadians(c - a)) / 2).pow(2) +
                        cos(Math.toRadians(a)) * cos(Math.toRadians(c)) * sin((Math.toRadians(d - b)) / 2).pow(2)
            )
        )

        return distance
    }
    private fun checkLocationPermission() {
        // パーミッションがすでに許可されているか確認
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // パーミッションが許可されていない場合、ユーザーにリクエスト
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // パーミッションがすでに許可されている場合、GPSを使用する処理を続行
            // ここにGPSを使用するための処理を追加
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // 現在地を取得できた場合の処理
                    location?.let {
                        take_gps =
                            com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                        Log.d(TAG,"current location: ${take_gps}")
//                        makeApiRequest()
                    }
                }
                .addOnFailureListener { exception ->
                    // 現在地の取得に失敗した場合の処理
                    Log.e("Location", "Error getting location", exception)
                }

        }
    }

    private fun startLocationUpdates() {
        // パーミッションがすでに許可されているか確認
        Log.d(TAG,"startLocationUpdates")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // パーミッションが許可されていない場合、ユーザーにリクエスト
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // パーミッションがすでに許可されている場合、GPSを使用する処理を続行
            // ここにGPSを使用するための処理を追加
            Log.d(TAG,"location permission granted!")
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

            fusedLocationClient.requestLocationUpdates(
                getLocationRequest(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        // 位置情報が更新されたときの処理
                        val location = locationResult.lastLocation
                        if (location != null) {
                            take_gps = com.google.android.gms.maps.model.LatLng(
                                location.latitude,
                                location.longitude
                            )
                            Log.d(TAG,"location take_gps: $take_gps")

                        }
                    }
                },
                Looper.getMainLooper()
            )
                .addOnFailureListener { exception ->
                    // 現在地の取得に失敗した場合の処理
                    Log.e("Location", "Error getting location", exception)
                }
        }
    }
    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .setInterval(1000)  // 1000ミリ秒ごとに更新
    }




}