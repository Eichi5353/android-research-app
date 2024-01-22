package com.example.myprototype

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myprototype.data.MapMakerInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.UUID


//ゴールまで行くと強制終了する？？
class MapsTestFragment : Fragment(),OnMapReadyCallback {
    private lateinit var mapsCountViewModel: MapsCountViewModel
    private lateinit var mMap: GoogleMap
    //    private lateinit var mapView: MapView
    private val markerList = mutableListOf<MapMakerInfo>()
    val a = 0
    //    val APIKey = "AIzaSyCprjoQjsq3IeA3cRmkjTsNl3ahzPbfpSA"
    val APIKey = "AIzaSyCNx31A-f_VyABih0-OETfZ6BicvbJJgFY"
    //    val response = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // 任意の値
    val TAG = "MapsFragment"
    var currentLatLng: LatLng? = null
    //    今の経路の線
    private var currentPolyline: Polyline? = null
    private var currentMaker: Marker? = null

    private var lastButtonClicked: ImageButton? = null
    lateinit var queryBundle: Bundle

    private var response: String? = null

    var frag =0
    var ismakeApiFrag = false

    var imageResourceId: Int? = null
    var query_iButton: ImageButton? = null

    private lateinit var tripleTapDetector: GestureDetector
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps_test, container, false)
        query_iButton = view?.findViewById(R.id.ibtn_query)
        checkLocationPermission()
        // MapViewを取得
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapView = view.findViewById(R.id.map)
//        mapView.onCreate(savedInstanceState)
//        mapView?.getMapAsync(callback)
        mapFragment?.getMapAsync(this)

        //ViewModel取得
        activity?.run {
            mapsCountViewModel = ViewModelProvider(this).get(MapsCountViewModel::class.java)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onCreated queryButtonCount: ${mapsCountViewModel.queryButtonTouchCount.value}")

//        view.findViewById<ImageButton>(R.id.btn1).setOnClickListener {
//            val btn = view.findViewById<ImageButton>(R.id.btn1)
//            showImageDialog("aed",btn)
//        }
        view.findViewById<ImageButton>(R.id.btn2).setOnClickListener {
            val btn2 = view.findViewById<ImageButton>(R.id.btn2)
            showImageDialog("atm",btn2)
        }
//        view.findViewById<ImageButton>(R.id.btn3).setOnClickListener {
//            val btn3 = view.findViewById<ImageButton>(R.id.btn3)
//            showImageDialog("crecore",btn3)
//        }
        view.findViewById<ImageButton>(R.id.btn4).setOnClickListener {
            val btn4 = view.findViewById<ImageButton>(R.id.btn4)
            showImageDialog("mama",btn4)
        }
//        view.findViewById<ImageButton>(R.id.btn5).setOnClickListener {
//            val btn5 = view.findViewById<ImageButton>(R.id.btn5)
//            showImageDialog("nitro",btn5)
//        }
//        view.findViewById<ImageButton>(R.id.btn6).setOnClickListener {
//            val btn6 = view.findViewById<ImageButton>(R.id.btn6)
//            showImageDialog("obj",btn6)
//        }
        view.findViewById<ImageButton>(R.id.btn7).setOnClickListener {
            val btn7 = view.findViewById<ImageButton>(R.id.btn7)
            showImageDialog("stone",btn7)
        }
//        view.findViewById<ImageButton>(R.id.btn8).setOnClickListener {
//            val btn8 = view.findViewById<ImageButton>(R.id.btn8)
//            showImageDialog("water",btn8)
//        }
//        view.findViewById<ImageButton>(R.id.btn9).setOnClickListener {
//            val btn9 = view.findViewById<ImageButton>(R.id.btn9)
//            showImageDialog("arrow",btn9)
//        }
        view.findViewById<ImageButton>(R.id.btn10).setOnClickListener {
            val btn10 = view.findViewById<ImageButton>(R.id.btn10)
            showImageDialog("door",btn10)
        }
//        view.findViewById<ImageButton>(R.id.btn11).setOnClickListener {
//            val btn11 = view.findViewById<ImageButton>(R.id.btn11)
//            showImageDialog("ichi",btn11)
//        }
        view.findViewById<ImageButton>(R.id.btn12).setOnClickListener {
            val btn12 = view.findViewById<ImageButton>(R.id.btn12)
            showImageDialog("phone",btn12)
        }


        view.findViewById<Button>(R.id.btn_finish).setOnClickListener {
//            mapsCountViewModel.queryButtonTouchCount.valueがバグりがち
            Log.d(TAG,"Toast no bubunb で：${mapsCountViewModel.isRequestComplete.value} ==?? ${mapsCountViewModel.queryButtonTouchCount.value}")
            if (mapsCountViewModel.isRequestComplete.value==mapsCountViewModel.queryButtonTouchCount.value)
                findNavController().navigate(R.id.action_mapsTestFragment_to_resultAllFragment)
            else
                Toast.makeText(context, "Calculate now, wait a moment and click again", Toast.LENGTH_LONG).show()


//            runBlocking {
//                // 非同期処理が完了するまで待つ
//                val instanceTake = TakeImgFragment2()
//                val job = instanceTake.useJob()
//                // jobが非nullになるまで待つ
//                runBlocking {
//                    instanceTake.waitForJob()
//                }
//                if(job!=null){
//                    findNavController().navigate(R.id.action_mapsTestFragment_to_resultAllFragment)
//                }else{
//                    Toast.makeText(context, "Calculate now, wait a moment", Toast.LENGTH_SHORT).show()
////                    Log.e(TAG, "Job is null. Handle the error or provide alternative logic.")
//                }
//            }
        }


//        // パーミッションがすでに許可されている場合、GPSを使用する処理を続行
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                // 現在地を取得できた場合の処理
//                location?.let {
//                    currentLatLng = LatLng(it.latitude, it.longitude)//現在地
//                    Log.d(TAG,"current location: ${currentLatLng}")
//                    mMap.addMarker(MarkerOptions().position(currentLatLng!!).title("Current Location"))
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f))
//
//                    makeApiRequest()
//                }
//            }
    }
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        // マップの初期設定などを行う
//        初期位置
        val initialLatLng:LatLng =LatLng(34.97948,135.96404)
        val zoomLevel = 21.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, zoomLevel));
        // 保存されたマーカーの位置情報を復元
//        restoreMarkers()
        startLocationUpdates()
        // マップがクリックされたときのリスナーを設定
//        mMap.setOnMapClickListener { latLng ->
        // 新しい MapMarkerInfo インスタンスを作成
//            val markerInfo = MapMakerInfo(
//                id = UUID.randomUUID().toString(), // 一意なIDを生成
//                title = "Test marker",
//                snippet = "Marker Snippet",
////                position = latLng
//                position = LatLng(34.97948,135.96404)
//            )
//            // マーカーを地図に追加
//            val marker = mMap.addMarker(
//                MarkerOptions()
//                    .position(markerInfo.position)
//                    .title(markerInfo.title)
//                    .snippet(markerInfo.snippet)
//            )
//            // マーカーを地図に追加
//            addMarkerToMap(markerInfo)
//
//            // マーカーの情報を保存
//            saveMarkers()
//        }

    }

    private fun makeApiRequest() {
        if (!ismakeApiFrag) {
            ismakeApiFrag = true
            GlobalScope.launch(Dispatchers.IO) {
                // バックグラウンドで実行したい処理
                //ここでもうおかしい
                //            Log.d(TAG, "current location(beforeURL): ${currentLatLng}")
                Log.d(TAG, "isFirst: ${mapsCountViewModel.isFirstPlaceComplete.value}")
                Log.d(TAG, "isSecond: ${mapsCountViewModel.isSecondPlaceComplete.value}")
                Log.d(TAG, "isThird: ${mapsCountViewModel.isThirdPlaceComplete.value}")
                Log.d(TAG, "frag: ${frag}")
                Log.d(TAG, "cuurent Gps: ${currentLatLng}")


                if (mapsCountViewModel.isFirstPlaceComplete.value == true && mapsCountViewModel.isSecondPlaceComplete.value == false
                ) {
                    // リソースIDを指定して画像を設定
                    imageResourceId = R.drawable.arrow
                    getActivity()?.runOnUiThread {
                        query_iButton?.setImageResource(imageResourceId!!)
                        query_iButton?.setOnClickListener {
//                            mapsCountViewModel.queryButtonTouchCount.value=
//                                mapsCountViewModel.queryButtonTouchCount.value!! +1
                            showImageDialog("arrow", query_iButton!!)
                        }
                    }
                    response = URL(
                        "https://maps.googleapis.com/maps/api/directions/json?" +
                                "origin=${currentLatLng?.latitude},${currentLatLng?.longitude}" +
                                //                    "${currentLatLng?.latitude},${currentLatLng?.longitude}" +
                                "&destination=34.98098,135.96139" +//near arrow
                                //                   crecore "34.97948,135.96404" +
                                "&mode=walking" +
                                //なくてよい？                                            Nitro                ８０前            アーク前
                                "&waypoints=34.98123,135.96262|34.98091,135.96198|34.98011,135.96197" +
//                                "34.98182,135.96356|34.98094,135.96348" +
                                "&key=${APIKey}"
                    )
                        .readText()
                    frag = 1
                }else if (mapsCountViewModel.isFirstPlaceComplete.value == true && mapsCountViewModel.isSecondPlaceComplete.value == true) {
                    imageResourceId = R.drawable.finish
                    getActivity()?.runOnUiThread {
                        query_iButton?.setImageResource(imageResourceId!!)
                        var finish_frag = 0
                        query_iButton?.setOnClickListener {
                            if (finish_frag ==3){
                                findNavController().navigate(R.id.action_mapsTestFragment_to_resultAllFragment)
                            }else if(finish_frag <3){
                                finish_frag += 1
                                Log.d(TAG,"finish_frag:$finish_frag")
                            }
                        }
                    }
                    response = null
                    frag = 2
                } else {
                    imageResourceId = R.drawable.aed
                    getActivity()?.runOnUiThread {
                        query_iButton?.setImageResource(imageResourceId!!)
                        query_iButton?.setOnClickListener {
//                            mapsCountViewModel.queryButtonTouchCount.value=
//                                mapsCountViewModel.queryButtonTouchCount.value!! +1
                            showImageDialog("aed", query_iButton!!)
                        }
                    }
                    response = URL(
                        "https://maps.googleapis.com/maps/api/directions/json?" +
                                "origin=34.97948,135.96404" +
                                //                    "${currentLatLng?.latitude},${currentLatLng?.longitude}" +
                                "&destination=34.98125,135.96258" +//near aed
                                //                   crecore "34.97948,135.96404" +
                                "&mode=walking" +
                                //なくてよい？                                            Nitro                ８０前            アーク前
                                "&waypoints=34.98040,135.96367|34.98046,135.96470|34.98089,135.96474" +
//
                                //                    "34.97983,135.96478|34.97984,135.96502|34.98012,135.96488|34.98035,135.96494|34.98046,135.96381|34.98090,135.96371|34.98134,135.96472|34.98255,135.96457|34.98181,135.96349|" +
                                //AED
                                //                    "34.98085,135.96305|34.98122,135.96253|34.98053,135.96241" +
                                //                    "34.98259,135.96450|34.98182,135.96345|34.98081,135.96292|34.98088,135.96204" +
                                "&key=${APIKey}"
                    )
                        .readText()
                }


                //            Log.d(TAG,"response Json: ${response}")
                withContext(Dispatchers.Main) {
                    // UIの更新などを行う
                    var points: String? = null
                    if(response== null)
                        currentPolyline = null
                    else {
                        val jsonResponse = JSONObject(response)
                        //                Log.d(TAG,"response Json: ${jsonResponse}")
                        val routes = jsonResponse.getJSONArray("routes")
                        //                Log.d(TAG,"routes: ${routes}")

                        if (routes.length() > 0) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            if (legs.length() > 0) {
                                val steps = legs.getJSONObject(0).getJSONArray("steps")
                                // ここで取得した情報を使用する処理を行う
                                val route = routes.getJSONObject(0) // 最初の経路を取得
                                points =
                                    route.getJSONObject("overview_polyline").getString("points")
                                //                        Log.d(TAG, "points : ${points}")

                            } else {
                                Log.e(TAG, "error legs is empty")
                                // エラー処理: "legs"が空の場合
                            }
                        } else {
                            Log.e(TAG, "error routes is empty")

                            // エラー処理: "routes"が空の場合
                        }
                        //                val route = routes.getJSONObject(0) // 最初の経路を取得
                        //                val points = route.getJSONObject("overview_polyline").getString("points")

                        // メインスレッドで実行すること
                        //                経路の色設定
                        Log.d(TAG, "points: ${points}")
                        if (points != null) {
                            val polyline = PolylineOptions()
                                .addAll(PolyUtil.decode(points))
                                .color(Color.RED)
                                .width(10f)

                            currentPolyline = mMap.addPolyline(polyline)
                        }
                    }
                }
            }
        }
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
            val fusedLocationClient:FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // 現在地を取得できた場合の処理
                    location?.let {
                        currentLatLng = LatLng(it.latitude, it.longitude)
//                        Log.d(TAG,"current location: ${currentLatLng}")
                        mMap.addMarker(MarkerOptions().position(currentLatLng!!).title("Current Location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f))

                        makeApiRequest()
                    }
                }
                .addOnFailureListener { exception ->
                    // 現在地の取得に失敗した場合の処理
                    Log.e("Location", "Error getting location", exception)
                }

        }
    }

    // ユーザーのパーミッションリクエストの結果を処理
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "permission request granted")

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // パーミッションリクエストの結果を確認
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // パーミッションが許可された場合、GPSを使用する処理を続行
                    // ここにGPSを使用するための処理を追加
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        mMap.setMyLocationEnabled(true);
                    } else {
                        // パーミッションが拒否された場合、ユーザーにメッセージを表示するなどの処理を追加
                    }
                }
            }
        }
    }

    private fun addMarkerToMap(markerInfo: MapMakerInfo) {
        // GoogleMap にマーカーを追加
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(markerInfo.position)
                .title(markerInfo.title)
                .snippet(markerInfo.snippet)
        )

        // マーカー情報をリストに追加
        markerList.add(markerInfo.copy(id = marker!!.id))
    }
    private fun saveMarkers() {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        for ((index, marker) in markerList.withIndex()) {
            editor.putFloat("marker${index}_lat", marker.position.latitude.toFloat())
            editor.putFloat("marker${index}_lng", marker.position.longitude.toFloat())
        }

        editor.apply()
    }

    private fun restoreMarkers() {
        val newMarkerList = mutableListOf<MapMakerInfo>()
        // マーカー情報を取得し、リストに追加
        for (markerInfo in markerList) {
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(markerInfo.position)
                    .title(markerInfo.title)
                    .snippet(markerInfo.snippet)
            )

            // Marker オブジェクトを MapMarkerInfo に変換してリストに追加
            val updatedMarkerInfo = markerInfo.copy(id = marker!!.id)
            newMarkerList.add(updatedMarkerInfo)
        }
        // 新しいリストを markerList にセット
        markerList.clear()
        markerList.addAll(newMarkerList)
    }
    private fun showImageDialog(imageFileName: String, button: ImageButton) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image)

        val dialogImageView: ImageView = dialog.findViewById(R.id.dialogImageView)
        // Glideを使用してAssetsから画像を読み込んでImageViewに表示
        Glide.with(requireContext())
            .load("file:///android_asset/BKC/$imageFileName.jpg")
            .into(dialogImageView)
        dialog.show()
        dialog.findViewById<Button>(R.id.btn_openCamera).setOnClickListener {
            Log.d(TAG,"dialog Button press")
            Log.d(TAG,"map visit Count(before): ${mapsCountViewModel.visitCount.value}")
            mapsCountViewModel.incrementVisitCount()
            Log.d(TAG,"map visit Count(after): ${mapsCountViewModel.visitCount.value}")
            queryBundle = Bundle()
            queryBundle.putString("query_name",imageFileName)
//            この画面を消す
            dialog.dismiss()
//            押したボタンを消すとか
            // 今回クリックされたボタンを記録
//            lastButtonClicked = button
//            // 押されたボタン非表示にする
//            lastButtonClicked?.visibility = View.GONE

//            ！注意　この画面には戻れません　写真を覚えて！
            showCustomDialog()




        }

    }
    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.to_takefragment_dialog,null)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)

        // ダイアログビルダーを作成
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        // ダイアログを表示
        val dialog = builder.create()
        dialog.show()

        // OKボタンのクリックイベントを設定
        okButton.setOnClickListener {
            // ダイアログを閉じる
            dialog.dismiss()
            //            撮影画面へ
            findNavController().navigate(R.id.action_mapsTestFragment_to_takeImgFragment2,queryBundle)
        }
    }

    //    位置情報を更新するためのメソッド
    private fun startLocationUpdates() {
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

            fusedLocationClient.requestLocationUpdates(
                getLocationRequest(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        // 位置情報が更新されたときの処理
                        val location = locationResult.lastLocation
                        if (location != null) {
                            currentLatLng = LatLng(location.latitude, location.longitude)
                        }
//                        Log.d(TAG,"currentLocation: $currentLatLng")
                        currentMaker?.remove()
                        currentMaker = mMap.addMarker(MarkerOptions().position(currentLatLng!!).title("Current Location"))
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng!!))
                        makeApiRequest()  // 位置情報が更新されるたびに経路を再取得
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
            .setInterval(1000)  // 1000ミリ秒ごとに更新
    }

}
//
//package com.example.myprototype
//
//import android.Manifest
//import android.R
//import android.app.Dialog
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.location.Location
//import androidx.fragment.app.Fragment
//import android.os.Bundle
//import android.os.Looper
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.example.myprototype.data.MapMakerInfo
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationServices
//
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.android.gms.maps.model.Polyline
//import com.google.android.gms.maps.model.PolylineOptions
//import com.google.maps.android.PolyUtil
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//import org.json.JSONObject
//import java.net.URL
//import java.util.UUID
//import android.R.drawable as drawable1
//
//
//
////ゴールまで行くと強制終了する？？
//class MapsTestFragment : Fragment(),OnMapReadyCallback {
//    private lateinit var mapsCountViewModel: MapsCountViewModel
//    private lateinit var mMap: GoogleMap
////    private lateinit var mapView: MapView
//    private val markerList = mutableListOf<MapMakerInfo>()
//    val a = 0
////    val APIKey = "AIzaSyCprjoQjsq3IeA3cRmkjTsNl3ahzPbfpSA"
//    val APIKey = "AIzaSyCNx31A-f_VyABih0-OETfZ6BicvbJJgFY"
//    //    val response = null
//    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // 任意の値
//    val TAG = "MapsFragment"
//    var currentLatLng: LatLng? = null
////    今の経路の線
//    private var currentPolyline: Polyline? = null
//    private var currentMaker: Marker? = null
//
//    private var lastButtonClicked: ImageButton? = null
//    lateinit var queryBundle: Bundle
//
//    private var response: String? = null
//
//    var frag =0
//    var ismakeApiFrag = false
//
//    var imageResourceId = null
//    var query_iButton: ImageButton? = null
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val context = requireContext()
//        context.let {
//            val o = drawable
//        }
//        val view = inflater.inflate(R.layout.fragment_maps_test, container, false)
//
//        checkLocationPermission()
//        // MapViewを取得
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
////        mapView = view.findViewById(R.id.map)
////        mapView.onCreate(savedInstanceState)
////        mapView?.getMapAsync(callback)
//        mapFragment?.getMapAsync(this)
//
//        //ViewModel取得
//        activity?.run {
//            mapsCountViewModel = ViewModelProvider(this).get(MapsCountViewModel::class.java)
//        }
//        return view
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        view.findViewById<ImageButton>(R.id.btn1).setOnClickListener {
//            val btn = view.findViewById<ImageButton>(R.id.btn1)
//            showImageDialog("aed",btn)
//        }
//        view.findViewById<ImageButton>(R.id.btn2).setOnClickListener {
//            val btn2 = view.findViewById<ImageButton>(R.id.btn2)
//            showImageDialog("atm",btn2)
//        }
//        view.findViewById<ImageButton>(R.id.btn3).setOnClickListener {
//            val btn3 = view.findViewById<ImageButton>(R.id.btn3)
//            showImageDialog("crecore",btn3)
//        }
//        view.findViewById<ImageButton>(R.id.btn4).setOnClickListener {
//            val btn4 = view.findViewById<ImageButton>(R.id.btn4)
//            showImageDialog("mama",btn4)
//        }
//        view.findViewById<ImageButton>(R.id.btn5).setOnClickListener {
//            val btn5 = view.findViewById<ImageButton>(R.id.btn5)
//            showImageDialog("nitro",btn5)
//        }
//        view.findViewById<ImageButton>(R.id.btn6).setOnClickListener {
//            val btn6 = view.findViewById<ImageButton>(R.id.btn6)
//            showImageDialog("obj",btn6)
//        }
//        view.findViewById<ImageButton>(R.id.btn7).setOnClickListener {
//            val btn7 = view.findViewById<ImageButton>(R.id.btn7)
//            showImageDialog("stone",btn7)
//        }
//        view.findViewById<ImageButton>(R.id.btn8).setOnClickListener {
//            val btn8 = view.findViewById<ImageButton>(R.id.btn8)
//            showImageDialog("water",btn8)
//        }
//        view.findViewById<ImageButton>(R.id.btn9).setOnClickListener {
//            val btn9 = view.findViewById<ImageButton>(R.id.btn9)
//            showImageDialog("arrow",btn9)
//        }
//        view.findViewById<ImageButton>(R.id.btn10).setOnClickListener {
//            val btn10 = view.findViewById<ImageButton>(R.id.btn10)
//            showImageDialog("door",btn10)
//        }
//        view.findViewById<ImageButton>(R.id.btn11).setOnClickListener {
//            val btn11 = view.findViewById<ImageButton>(R.id.btn11)
//            showImageDialog("ichi",btn11)
//        }
//        view.findViewById<ImageButton>(R.id.btn12).setOnClickListener {
//            val btn12 = view.findViewById<ImageButton>(R.id.btn12)
//            showImageDialog("phone",btn12)
//        }
//
//
//        view.findViewById<Button>(R.id.btn_finish).setOnClickListener {
//            if (mapsCountViewModel.isRequestComplete.value==mapsCountViewModel.visitCount.value)
//                findNavController().navigate(R.id.action_mapsTestFragment_to_resultAllFragment)
//            else
//                Toast.makeText(context, "Calculate now, wait a moment and click again", Toast.LENGTH_LONG).show()
//        }
//    }
//    override fun onMapReady(map: GoogleMap) {
//        mMap = map
//
//        // マップの初期設定などを行う
////        初期位置
//        val initialLatLng:LatLng =LatLng(34.97948,135.96404)
//        val zoomLevel = 21.0f
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, zoomLevel));
//        // 保存されたマーカーの位置情報を復元
////        restoreMarkers()
//        startLocationUpdates()
//        // マップがクリックされたときのリスナーを設定
////        mMap.setOnMapClickListener { latLng ->
//            // 新しい MapMarkerInfo インスタンスを作成
////            val markerInfo = MapMakerInfo(
////                id = UUID.randomUUID().toString(), // 一意なIDを生成
////                title = "Test marker",
////                snippet = "Marker Snippet",
//////                position = latLng
////                position = LatLng(34.97948,135.96404)
////            )
////            // マーカーを地図に追加
////            val marker = mMap.addMarker(
////                MarkerOptions()
////                    .position(markerInfo.position)
////                    .title(markerInfo.title)
////                    .snippet(markerInfo.snippet)
////            )
////            // マーカーを地図に追加
////            addMarkerToMap(markerInfo)
////
////            // マーカーの情報を保存
////            saveMarkers()
////        }
//
//    }
//
//    private fun makeApiRequest() {
//        if (!ismakeApiFrag) {
//            ismakeApiFrag = true
//            GlobalScope.launch(Dispatchers.IO) {
//                // バックグラウンドで実行したい処理
//                //ここでもうおかしい
//                //            Log.d(TAG, "current location(beforeURL): ${currentLatLng}")
//                Log.d(TAG, "isFirst: ${mapsCountViewModel.isFirstPlaceComplete.value}")
//                Log.d(TAG, "isSecond: ${mapsCountViewModel.isSecondPlaceComplete.value}")
//                Log.d(TAG, "isThird: ${mapsCountViewModel.isThirdPlaceComplete.value}")
//                Log.d(TAG, "frag: ${frag}")
//                Log.d(TAG, "cuurent Gps: ${currentLatLng}")
//
//
//                if (mapsCountViewModel.isFirstPlaceComplete.value == true && mapsCountViewModel.isSecondPlaceComplete.value == false
//                    && mapsCountViewModel.isThirdPlaceComplete.value == false
//                ) {
//                    response = URL(
//                        "https://maps.googleapis.com/maps/api/directions/json?" +
//                                "origin=34.98252,135.96468" +
//                                //                    "${currentLatLng?.latitude},${currentLatLng?.longitude}" +
//                                "&destination=34.98125,135.96258" +//near aed
//                                //                   crecore "34.97948,135.96404" +
//                                "&mode=walking" +
//                                //なくてよい？                                            Nitro                ８０前            アーク前
//                                "&waypoints=34.98182,135.96356|34.98094,135.96348" +
//                                "&key=${APIKey}"
//                    )
//                        .readText()
//                    frag = 1
//                } else if (mapsCountViewModel.isFirstPlaceComplete.value == true && mapsCountViewModel.isSecondPlaceComplete.value == true
//                ) {
//                    response = URL(
//                        "https://maps.googleapis.com/maps/api/directions/json?" +
//                                "origin=${currentLatLng?.latitude},${currentLatLng?.longitude}" +//34.98125,135.96258"+
//                                //                    "${currentLatLng?.latitude},${currentLatLng?.longitude}" +
//                                "&destination=34.97948,135.96404" +//crecore
//                                //                   crecore "34.97948,135.96404" +
//                                "&mode=walking" +
//                                //なくてよい？                                            Nitro                ８０前            アーク前
//                                "&waypoints=34.98090,135.96208|34.97979,135.96216|34.97863,135.96352" +
//                                "&key=${APIKey}"
//                    )
//                        .readText()
//                    frag = 2
//                } else {
//                    response = URL(
//                        "https://maps.googleapis.com/maps/api/directions/json?" +
//                                "origin=34.97948,135.96404" +
//                                //                    "${currentLatLng?.latitude},${currentLatLng?.longitude}" +
//                                "&destination=34.98252,135.96468" +//obj
//                                //                   crecore "34.97948,135.96404" +
//                                "&mode=walking" +
//                                //なくてよい？                                            Nitro                ８０前            アーク前
//                                "&waypoints=34.98087,135.96370|34.98089,135.96474" +
//                                //                    "34.97983,135.96478|34.97984,135.96502|34.98012,135.96488|34.98035,135.96494|34.98046,135.96381|34.98090,135.96371|34.98134,135.96472|34.98255,135.96457|34.98181,135.96349|" +
//                                //AED
//                                //                    "34.98085,135.96305|34.98122,135.96253|34.98053,135.96241" +
//                                //                    "34.98259,135.96450|34.98182,135.96345|34.98081,135.96292|34.98088,135.96204" +
//                                "&key=${APIKey}"
//                    )
//                        .readText()
//
//                }
//
//                //            Log.d(TAG,"response Json: ${response}")
//                withContext(Dispatchers.Main) {
//                    // UIの更新などを行う
//                    var points: String? = null
//                    val jsonResponse = JSONObject(response)
//                    //                Log.d(TAG,"response Json: ${jsonResponse}")
//                    val routes = jsonResponse.getJSONArray("routes")
//                    //                Log.d(TAG,"routes: ${routes}")
//
//                    if (routes.length() > 0) {
//                        val legs = routes.getJSONObject(0).getJSONArray("legs")
//                        if (legs.length() > 0) {
//                            val steps = legs.getJSONObject(0).getJSONArray("steps")
//                            // ここで取得した情報を使用する処理を行う
//                            val route = routes.getJSONObject(0) // 最初の経路を取得
//                            points = route.getJSONObject("overview_polyline").getString("points")
//                            //                        Log.d(TAG, "points : ${points}")
//
//                        } else {
//                            Log.e(TAG, "error legs is empty")
//                            // エラー処理: "legs"が空の場合
//                        }
//                    } else {
//                        Log.e(TAG, "error routes is empty")
//
//                        // エラー処理: "routes"が空の場合
//                    }
//                    //                val route = routes.getJSONObject(0) // 最初の経路を取得
//                    //                val points = route.getJSONObject("overview_polyline").getString("points")
//
//                    // メインスレッドで実行すること
//                    //                経路の色設定
//                    Log.d(TAG, "points: ${points}")
//                    if (points != null) {
//                        currentPolyline?.remove()
//                        val polyline = PolylineOptions()
//                            .addAll(PolyUtil.decode(points))
//                            .color(Color.RED)
//                            .width(10f)
//
//                        currentPolyline = mMap.addPolyline(polyline)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun checkLocationPermission() {
//        // パーミッションがすでに許可されているか確認
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // パーミッションが許可されていない場合、ユーザーにリクエスト
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//        } else {
//            // パーミッションがすでに許可されている場合、GPSを使用する処理を続行
//            // ここにGPSを使用するための処理を追加
//            val fusedLocationClient:FusedLocationProviderClient =
//                LocationServices.getFusedLocationProviderClient(requireContext())
//
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { location: Location? ->
//                    // 現在地を取得できた場合の処理
//                    location?.let {
//                        currentLatLng = LatLng(it.latitude, it.longitude)
//                        Log.d(TAG,"current location: ${currentLatLng}")
//                        mMap.addMarker(MarkerOptions().position(currentLatLng!!).title("Current Location"))
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f))
//
//                        makeApiRequest()
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    // 現在地の取得に失敗した場合の処理
//                    Log.e("Location", "Error getting location", exception)
//                }
//
//        }
//    }
//
//    // ユーザーのパーミッションリクエストの結果を処理
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        Log.d(TAG, "permission request granted")
//
//        when (requestCode) {
//            LOCATION_PERMISSION_REQUEST_CODE -> {
//                // パーミッションリクエストの結果を確認
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // パーミッションが許可された場合、GPSを使用する処理を続行
//                    // ここにGPSを使用するための処理を追加
//                    if (ContextCompat.checkSelfPermission(
//                            requireContext(),
//                            Manifest.permission.ACCESS_FINE_LOCATION
//                        ) == PackageManager.PERMISSION_GRANTED
//                    ) {
//                        mMap.setMyLocationEnabled(true);
//                    } else {
//                        // パーミッションが拒否された場合、ユーザーにメッセージを表示するなどの処理を追加
//                    }
//                }
//            }
//        }
//    }
//
//    private fun addMarkerToMap(markerInfo: MapMakerInfo) {
//        // GoogleMap にマーカーを追加
//        val marker = mMap.addMarker(
//            MarkerOptions()
//                .position(markerInfo.position)
//                .title(markerInfo.title)
//                .snippet(markerInfo.snippet)
//        )
//
//        // マーカー情報をリストに追加
//        markerList.add(markerInfo.copy(id = marker!!.id))
//    }
//    private fun saveMarkers() {
//        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//
//        for ((index, marker) in markerList.withIndex()) {
//            editor.putFloat("marker${index}_lat", marker.position.latitude.toFloat())
//            editor.putFloat("marker${index}_lng", marker.position.longitude.toFloat())
//        }
//
//        editor.apply()
//    }
//
//    private fun restoreMarkers() {
//        val newMarkerList = mutableListOf<MapMakerInfo>()
//        // マーカー情報を取得し、リストに追加
//        for (markerInfo in markerList) {
//            val marker = mMap.addMarker(
//                MarkerOptions()
//                    .position(markerInfo.position)
//                    .title(markerInfo.title)
//                    .snippet(markerInfo.snippet)
//            )
//
//            // Marker オブジェクトを MapMarkerInfo に変換してリストに追加
//            val updatedMarkerInfo = markerInfo.copy(id = marker!!.id)
//            newMarkerList.add(updatedMarkerInfo)
//        }
//        // 新しいリストを markerList にセット
//        markerList.clear()
//        markerList.addAll(newMarkerList)
//    }
//    private fun showImageDialog(imageFileName: String, button: ImageButton) {
//        val dialog = Dialog(requireContext())
//        dialog.setContentView(R.layout.dialog_image)
//
//        val dialogImageView: ImageView = dialog.findViewById(R.id.dialogImageView)
//        // Glideを使用してAssetsから画像を読み込んでImageViewに表示
//        Glide.with(requireContext())
//            .load("file:///android_asset/BKC/$imageFileName.jpg")
//            .into(dialogImageView)
//        dialog.show()
//        dialog.findViewById<Button>(R.id.btn_openCamera).setOnClickListener {
//            Log.d(TAG,"dialog Button press")
//            Log.d(TAG,"map visit Count(before): ${mapsCountViewModel.visitCount.value}")
//            mapsCountViewModel.incrementVisitCount()
//            Log.d(TAG,"map visit Count(after): ${mapsCountViewModel.visitCount.value}")
//            queryBundle = Bundle()
//            queryBundle.putString("query_name",imageFileName)
////            この画面を消す
//            dialog.dismiss()
////            押したボタンを消すとか
//            // 今回クリックされたボタンを記録
////            lastButtonClicked = button
////            // 押されたボタン非表示にする
////            lastButtonClicked?.visibility = View.GONE
//
////            ！注意　この画面には戻れません　写真を覚えて！
//            showCustomDialog()
//
//
//
//
//        }
//
//    }
//    private fun showCustomDialog() {
//        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.to_takefragment_dialog,null)
//        val okButton = dialogView.findViewById<Button>(R.id.okButton)
//
//        // ダイアログビルダーを作成
//        val builder = AlertDialog.Builder(requireContext())
//            .setView(dialogView)
//
//        // ダイアログを表示
//        val dialog = builder.create()
//        dialog.show()
//
//        // OKボタンのクリックイベントを設定
//        okButton.setOnClickListener {
//            // ダイアログを閉じる
//            dialog.dismiss()
//            //            撮影画面へ
//            findNavController().navigate(R.id.action_mapsTestFragment_to_takeImgFragment2,queryBundle)
//        }
//    }
//
////    位置情報を更新するためのメソッド
//    private fun startLocationUpdates() {
//    // パーミッションがすでに許可されているか確認
//    if (ContextCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED
//    ) {
//        // パーミッションが許可されていない場合、ユーザーにリクエスト
//        ActivityCompat.requestPermissions(
//            requireActivity(),
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//            LOCATION_PERMISSION_REQUEST_CODE
//        )
//    } else {
//        // パーミッションがすでに許可されている場合、GPSを使用する処理を続行
//        // ここにGPSを使用するための処理を追加
//        val fusedLocationClient: FusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(requireContext())
//
//        fusedLocationClient.requestLocationUpdates(
//            getLocationRequest(),
//            object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult) {
//                    super.onLocationResult(locationResult)
//                    // 位置情報が更新されたときの処理
//                    val location = locationResult.lastLocation
//                    if (location != null) {
//                        currentLatLng = LatLng(location.latitude, location.longitude)
//                    }
////                    mMap.clear()
//                    Log.d(TAG,"currentLocation: $currentLatLng")
//                    currentMaker?.remove()
//                    currentMaker = mMap.addMarker(MarkerOptions().position(currentLatLng!!).title("Current Location"))
////                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng!!))
//                    makeApiRequest()  // 位置情報が更新されるたびに経路を再取得
//                }
//            },
//            Looper.getMainLooper()
//        )
//            .addOnFailureListener { exception ->
//                // 現在地の取得に失敗した場合の処理
//                Log.e("Location", "Error getting location", exception)
//            }
//        }
//    }
//
//
//    private fun getLocationRequest(): LocationRequest {
//        return LocationRequest.create()
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .setInterval(1000)  // 1000ミリ秒ごとに更新
//    }
//
//}