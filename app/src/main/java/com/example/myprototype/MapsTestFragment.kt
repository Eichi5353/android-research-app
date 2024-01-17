package com.example.myprototype

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myprototype.data.MapMakerInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

//    val APIKey = "AIzaSyCprjoQjsq3IeA3cRmkjTsNl3ahzPbfpSA"
    val APIKey = "AIzaSyCNx31A-f_VyABih0-OETfZ6BicvbJJgFY"
    //    val response = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001 // 任意の値
    val TAG = "MapsFragment"
    var currentLatLng: LatLng? = null

    private var lastButtonClicked: ImageButton? = null

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps_test, container, false)

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
        view.findViewById<ImageButton>(R.id.btn1).setOnClickListener {
            val btn = view.findViewById<ImageButton>(R.id.btn1)
            showImageDialog("aed02.jpg",btn)
        }
        view.findViewById<ImageButton>(R.id.btn2).setOnClickListener {
            val btn2 = view.findViewById<ImageButton>(R.id.btn2)
            showImageDialog("atm03.jpg",btn2)
        }
        view.findViewById<ImageButton>(R.id.btn3).setOnClickListener {
            val btn3 = view.findViewById<ImageButton>(R.id.btn3)
            showImageDialog("crecore01.jpg",btn3)
        }
        view.findViewById<ImageButton>(R.id.btn4).setOnClickListener {
            val btn4 = view.findViewById<ImageButton>(R.id.btn4)
            showImageDialog("mama01.jpg",btn4)
        }
        view.findViewById<ImageButton>(R.id.btn5).setOnClickListener {
            val btn5 = view.findViewById<ImageButton>(R.id.btn5)
            showImageDialog("nitro01.jpg",btn5)
        }
        view.findViewById<ImageButton>(R.id.btn6).setOnClickListener {
            val btn6 = view.findViewById<ImageButton>(R.id.btn6)
            showImageDialog("obj21.jpg",btn6)
        }
        view.findViewById<ImageButton>(R.id.btn7).setOnClickListener {
            val btn7 = view.findViewById<ImageButton>(R.id.btn7)
            showImageDialog("stone01.jpg",btn7)
        }
        view.findViewById<ImageButton>(R.id.btn8).setOnClickListener {
            val btn8 = view.findViewById<ImageButton>(R.id.btn2)
            showImageDialog("water02.jpg",btn8)
        }


        view.findViewById<Button>(R.id.btn_finish).setOnClickListener {
            findNavController().navigate(R.id.action_mapsTestFragment_to_resultAllFragment)
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
        val zoomLevel = 14.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, zoomLevel));
        // 保存されたマーカーの位置情報を復元
        restoreMarkers()

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
        GlobalScope.launch(Dispatchers.IO) {
            // バックグラウンドで実行したい処理
            //ここでもうおかしい
            Log.d(TAG, "current location(beforeURL): ${currentLatLng}")
            val response = URL("https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=34.97948,135.96404" +
//                    "${currentLatLng?.latitude},${currentLatLng?.longitude}" +
                    "&destination=34.97948,135.96404" +
                    "&mode=walking" +
                    "&waypoints=34.97983,135.96478|34.98073,135.96170|34.97984,135.96502|34.98012,135.96488|34.98035,135.96494|34.98134,135.96472|34.98259,135.96450|34.98182,135.96345|34.98081,135.96292|34.98088,135.96204" +
                    "&key=${APIKey}")
                .readText()
//            Log.d(TAG,"response Json: ${response}")
            withContext(Dispatchers.Main) {
                // UIの更新などを行う
                //経路を作成できていない，どっかでミスしている
                var points: String? = null
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
                        points = route.getJSONObject("overview_polyline").getString("points")
//                        Log.d(TAG, "points : ${points}")

                    } else {
                        // エラー処理: "legs"が空の場合
                    }
                } else {
                    // エラー処理: "routes"が空の場合
                }
//                val route = routes.getJSONObject(0) // 最初の経路を取得
//                val points = route.getJSONObject("overview_polyline").getString("points")

                // メインスレッドで実行すること
                val polyline = PolylineOptions()
                    .addAll(PolyUtil.decode(points))
                    .color(Color.BLUE)
                    .width(5f)

                mMap.addPolyline(polyline)
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
                        Log.d(TAG,"current location: ${currentLatLng}")
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
            .load("file:///android_asset/BKC/$imageFileName")
            .into(dialogImageView)
        dialog.show()
        dialog.findViewById<Button>(R.id.btn_openCamera).setOnClickListener {
            Log.d(TAG,"dialog Button press")
            Log.d(TAG,"map visit Count(before): ${mapsCountViewModel.visitCount.value}")
            mapsCountViewModel.incrementVisitCount()
            Log.d(TAG,"map visit Count(after): ${mapsCountViewModel.visitCount.value}")
            val queryBundle = Bundle()
            queryBundle.putString("query_name",imageFileName)
//            この画面を消す
            dialog.dismiss()
//            押したボタンを消すとか
            // 今回クリックされたボタンを記録
//            lastButtonClicked = button
//            // 押されたボタン非表示にする
//            lastButtonClicked?.visibility = View.GONE

//            ！注意　この画面には戻れません　写真を覚えて！
            //            撮影画面へ
            findNavController().navigate(R.id.action_mapsTestFragment_to_takeImgFragment2,queryBundle)




        }

    }
}