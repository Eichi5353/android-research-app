package com.example.myprototype.data

import com.google.android.gms.maps.model.LatLng

data class MapMakerInfo(
    val id: String, // マーカーのユニークなID
    val title: String?, // マーカーのタイトル（オプション）
    val snippet: String?, // マーカーの詳細情報（オプション）
    val position: LatLng // マーカーの座標
)
