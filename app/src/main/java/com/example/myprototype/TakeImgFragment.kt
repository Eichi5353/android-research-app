package com.example.myprototype

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TakeImgFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TakeImgFragment : Fragment(),LocationListener {
    val TAG = "TakeImg"
    private lateinit var bitmapViewModel: BitmapViewModel
    var values = null
    var ivCamera: ImageView? = null

    var progressBar: ProgressBar? = null

    //URI=URL+URN URNはその名前を表す
    public var _imageUri: Uri? = null
    lateinit var imgString: String
    var bitmap: Bitmap? = null

    //データ送信用
    val bundle = Bundle()

    //位置情報用
    private var mLManager: LocationManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_take_img, container, false)
        progressBar = view?.findViewById(R.id.t_progresBar)
        view.findViewById<Button>(R.id.btn).setOnClickListener {
            findNavController().navigate(R.id.action_takeImgFragment_to_showImgFragment)
        }
        view.findViewById<Button>(R.id.result_btn).setOnClickListener {
            progressBar?.visibility = ProgressBar.VISIBLE
            findNavController().navigate(R.id.action_takeImgFragment_to_resultFragment)
            /*
            bundle.putString("takeImg",imgString)
            // Fragmentに値をセットする
            val fragment = ResultFragment()
            fragment.arguments = bundle
             */
        }
        //ViewModel取得
        activity?.run {
            bitmapViewModel = ViewModelProvider(this).get(BitmapViewModel::class.java)
        }


        // GPSサービス取得
        mLManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager?;


        //カメラ
        CameraStrat(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_take_again).setOnClickListener {
            CameraStrat(view)
        }

/*
        // GPSサービス取得
        mLManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager?;


        view.findViewById<ImageView>(R.id.imageView2).setOnClickListener {
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
            val now = Date()
            val nowStr = dateFormat.format(now)

            val fileName = "Myapp1_test1.jpg"

            //valuesが画像データ情報　_imageUriが格納先+values
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, fileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")//jpeg?pngでもできる？

            val contentResolver = requireContext().contentResolver
            _imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)


            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//カメラ起動（端末のカメラ）
            //intentを保存すればいい？？
            //val image: String =MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri)
            startActivityForResult(intent, 200)

            //_imageUriの変更を監視
            val uriObserver = Observer<Uri> { newUri ->
                // Update the UI, in this case, a TextView.
                _imageUri = newUri
            }
            bitmapViewModel.tUri.observe(viewLifecycleOwner,uriObserver)

            //imageUriをViewModelに保存
            bitmapViewModel.tUri.value = _imageUri
            //val btn = findViewById<Button>(R.id.result_btn)
                //btn.visibility=View.VISIBLE
                //return intent

 */
    }



    //撮影が成功した時の処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //同名メソッド
        Log.d(TAG,"onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == AppCompatActivity.RESULT_OK) {
            //val bitmap = data?.getParcelableExtra<Bitmap>("data")//画像のビットマップデータの取得　getParcelableExtraは数値，文字列以外のデータ型のオブジェクトを取得


            ivCamera = view?.findViewById<ImageView>(R.id.imageView2)
            //ivCamera?.setImageURI(_imageUri)


            //imageUriをResultFragmentに送る．
            //Bitmapに変換したほうがいいかもしれない
            val stream: InputStream? = requireContext().contentResolver.openInputStream(_imageUri!!)
            bitmap = null
            ivCamera?.setImageBitmap(null)
            bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))
            Log.d(TAG,"Bitmap: ${bitmap}")
            Log.d(TAG,"_imageUri: ${_imageUri}")

            ivCamera?.setImageBitmap(bitmap)
            //bitmapの変化を監視する mBitmapに値をセットするために必要
            //値が変化した場合にViewModelの値にセットするように処理している
            val bimapObserver = Observer<Bitmap> { newBitmap ->
                // Update the UI, in this case, a TextView.
                bitmap = newBitmap
            }
            bitmapViewModel.tBitmap.observe(viewLifecycleOwner, bimapObserver)

            //ViewModelに保存
            bitmapViewModel.tBitmap.value = bitmap
            //imgString =getStringImage(bitmap)
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
        //val btn = findViewById<Button>(R.id.result_btn)
        //btn.visibility=View.VISIBLE
        //return intent
        //_imageUriの変更を監視
        val uriObserver = Observer<Uri> { newUri ->
            // Update the UI, in this case, a TextView.
            _imageUri = newUri
        }
        bitmapViewModel.tUri.observe(viewLifecycleOwner,uriObserver)

        //imageUriをViewModelに保存
        bitmapViewModel.tUri.value = _imageUri

    }

    private fun getStringImage(bitmap: Bitmap): String {//Bitmap? or Disposable? どっちでもいける？
        val baos = ByteArrayOutputStream() //byte型，string型変換のための川（通り道）
        //bitmapをcompressによって圧縮
        //第二引数は０~100でふつう100でおk　第三引数のbaosは上のコードと合わせて決まりみたいなもの？
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)//baosの中に画像データが入ってる？
        val imageBytes = baos.toByteArray()
        val encodeImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return encodeImage
    }

    override fun onPause() {
        // TODO Auto-generated method stub
        if (mLManager != null) {
            mLManager!!.removeUpdates(this);
            super.onPause()
        }
    }

    override fun onLocationChanged(location: Location) {
        // TODO Auto-generated method stub
        //if (mCameraView != null) {
            // 位置を保存
          //  mCameraView.setLocation(location); }
    }
/*
    override fun onResume() {
        // TODO Auto-generated method stub
        if (mLManager != null) {
            mLManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        super.onResume();
    }



 */
}
