package com.example.myprototype

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide

/**
 * A simple [Fragment] subclass.
 * Use the [ThreePlaceShowFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ThreePlaceShowFragment : Fragment() {
    var imgView:ImageView?=null
    var imgView2:ImageView?=null
    var imgView3:ImageView?=null

    private lateinit var bitmapViewModel: BitmapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_three_place_show, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //3つ画像を提示して，タッチした画像から撮影できる
        //次の画面で撮影をして結果をサーバーに送る　結果を保存する　でこの画面に戻る
        //サーバーに送られていれば撮影した画像は完了と示される
        //すべてが終わったら，結果の画面に行く，3枚合計の点数，個々の点数表示
        imgView = view?.findViewById<ImageView>(R.id.imageView)
        imgView2 = view?.findViewById<ImageView>(R.id.imageView2)
        imgView3 = view?.findViewById<ImageView>(R.id.imageView3)
        //これをsetterでセットして，ViewModelに保存できるか？

        //bitmap= bitmapViewModel.mBitmap.value!!//.observe(viewLifecycleOwner,bitmap)
        val bitmap = bitmapViewModel.mBitmap.value
        val bitmap2 = bitmapViewModel.mBitmap2.value
        val bitmap3 = bitmapViewModel.mBitmap3.value

        //ViewModelに値をセット
        /*
        val bimapObserver = Observer<Bitmap> { newBitmap ->
            // Update the UI, in this case, a TextView.
            Log.d(TitleFragment.TAG,"Bitmap value become new: ${bitmap}")
            Glide.with(this)
                .load(newBitmap)
                .into(imgView!!)
        }
         */
        //bitmapViewModel.mBitmap.observe(viewLifecycleOwner, bimapObserver)
        Log.d(TAG,"ViewModelから取得したbitmapは$bitmap")
        Log.d(TAG,"ViewModelから取得したbitmapは${bitmapViewModel.mBitmap.value}")

        Glide.with(this)
            .load(bitmap)
            .into(imgView!!)
        Glide.with(this)
            .load(bitmap2)
            .into(imgView2!!)
        Glide.with(this)
            .load(bitmap3)
            .into(imgView3!!)

    }

    companion object {
        val TAG = "ThreePlaceSHowFragment"
    }
}