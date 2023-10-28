package com.example.myprototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import androidx.lifecycle.ViewModel as ViewModel


//import androidx.lifecycle.ViewModelProviders



class ShowImgFragment : Fragment(){

    var range: IntRange? = null //乱数の範囲を取得するため
    var RefList: ArrayList<String>? = arrayListOf()//storage内の画像の場所を取得するための配列
    val storage = Firebase.storage
    private var storageReference: StorageReference? = null


    //ボタンやらたち
    var show_button:Button?=null
    var imgView:ImageView?=null
    //bitmap確認用
    var imageView3:ImageView?=null


    val TAG="showFragment"

    //表示した画像を保つためのやつ
    var image: Bitmap? = null
    var live_bitmap: MutableLiveData<Bitmap>? = null

    var picture_location: String? = null
    var gsReference: StorageReference? =null
    var ONE_MEGABYTE:Long?=null

    //ViewModelインスタンス
    // The ViewModelStore provides a new ViewModel or one previously created.
    private lateinit var bitmapViewModel: BitmapViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        Log.d(TAG,"fragment.onAttach")
    }
    //private var model:MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        //storageの画像があるノードまで行く
        val listRef = storage.reference.child("images")
        //storageReference = storage.reference

        //そのノードにある画像の場所をすべて取得してRefList配列に格納する
        //そのあと取得した数分の大きさの乱数を生成し，ランダムで画像を表示するようにする
        listRef.listAll()
            .addOnSuccessListener { (items, prefixes) ->
                prefixes.forEach { prefix ->
                    // All the prefixes under listRef.
                    // You may call listAll() recursively on them.
                    //これは枕詞みたいなもんですか？
                    Log.i("Storage","prefix is $prefix")
                }

                items.forEach { item ->
                    // All the items under listRef.
                    Log.i("Storage","item is $item")
                    RefList?.add("$item")
                    Log.i("Storage","items are $RefList")
                }

                //乱数生成（1から配列の大きさ)
                range = (1..RefList!!.size)
                val size =RefList!!.size
                Log.i("Storage","array_size is $size")

                //関数にしたほうが見やすいか？
                //ここを撮影画像のみ選べるようにする　or 直前の撮影画像を持ってくる
                val random_num = range?.random()
                //選ばれた配列
                val result= RefList!![random_num!!-1]
                Log.i("Storage","random_num is $random_num")
                Log.i("Storage","random_item is $result")

                gsReference = storage.getReferenceFromUrl(result)
                ONE_MEGABYTE = 1024 * 1024 *10 //最大ダウンロードできるバイトサイズ　小さいとエラーになる

            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
            }

         */

        /*
        //savedInstanceStateに表示した画像を保存したい
        //savedInstanceStateに何か保存されていれば，それを表示するように設定する．
        if (savedInstanceState != null) {
            image = savedInstanceState.getParcelable("BitmapImage");
            targetImage.setImageBitmap(image);
            textTargetUri.setText(savedInstanceState.getString("path_to_picture"));R.drawable.camera_button);
        }

         */
        /*
        //CoroutineRepository().execute()
        val model: MainViewModel by lazy{
            ViewModelProvider(
                this,
                ViewModelProvider.NewInstanceFactory()
            ).get(MainViewModel::class.java)
        }
        model.getBitmap().observe(this, Observer {  })
            Log.d(TAG,"fragment.onCreate")

         */
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //show_button= view?.findViewById(R.id.show_btn)

        val view = inflater.inflate(R.layout.fragment_show_img, container, false)
        view.findViewById<Button>(R.id.btn).setOnClickListener{
            showCustomDialog()
            //findNavController().navigate(R.id.action_showImgFragment_to_takeImgFragment)
        }
        activity?.run {
            bitmapViewModel = ViewModelProvider(this).get(BitmapViewModel::class.java)
            Log.i("ViewModel", "Called ViewModelProvider.get")
        }
        setRetainInstance(true)
        Log.d(TAG,"fragment.onCreateView")

        return view
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        //show_button=view.findViewById<Button>(R.id.show_btn)

        //view.findViewById<Button>(R.id.show_btn).setOnClickListener {
            Log.d("ShowImg", "問題表示ボタン押した")
            imgView = view?.findViewById<ImageView>(R.id.imageView)
            imageView3 = view?.findViewById<ImageView>(R.id.imageView3)
            //これをsetterでセットして，ViewModelに保存できるか？

            //bitmap= bitmapViewModel.mBitmap.value!!//.observe(viewLifecycleOwner,bitmap)
            val bitmap = bitmapViewModel.mBitmap.value
            //ViewModelに値をセット
            val bimapObserver = Observer<Bitmap> { newBitmap ->
                // Update the UI, in this case, a TextView.
                Log.d(TitleFragment.TAG,"Bitmap value become new: ${bitmap}")
                Glide.with(this)
                    .load(newBitmap)
                    .into(imgView!!)
            }
            bitmapViewModel.mBitmap.observe(viewLifecycleOwner, bimapObserver)
            Log.d(TAG,"ViewModelから取得したbitmapは$bitmap")
            Log.d(TAG,"ViewModelから取得したbitmapは${bitmapViewModel.mBitmap.value}")

            Glide.with(this)
                .load(bitmap)
                .into(imgView!!)
            /*
            gsReference?.getBytes(ONE_MEGABYTE!!)?.addOnSuccessListener {
                //bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

                //ここ追加したぞ！！
                //bitmapViewModel.mBitmap.setValue(BitmapFactory.decodeByteArray(it, 0, it.size))


                bitmapViewModel.setBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
                bitmap= bitmapViewModel.mBitmap.getValue()!!//.observe(viewLifecycleOwner,bitmap)
                Log.d(TAG,"ViewModelから取得したbitmapは$bitmap")
                Glide.with(this)
                    .load(bitmap)
                    .into(imgView!!)
            }?.addOnFailureListener {
                // Handle any errors
                Log.e("ShowImg", "False")
            }

             */
        //}
            /*
            if (bitmapViewModel.getBitmap() == null) {
                gsReference?.getBytes(ONE_MEGABYTE!!)?.addOnSuccessListener {

                    Log.d("ShowImg", "imgView読み込んだ")


                    val a = "null"
                    Log.d("ViewModel", "ViewModel.getは$a")
                    // If the start date is not defined, it's a new ViewModel so set it.
                    bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    Glide.with(this)
                        .load(bitmap!!)
                        .into(imageView3!!)

                    bitmapViewModel.setBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
                    GlideApp.with(this)
                        .load(gsReference)
                        .centerCrop()
                        .into(imgView!!)
                    Log.d("ShowImg", "画像表示できた")
                }?.addOnFailureListener {
                // Handle any errors
                Log.e("ShowImg", "False")
                }
            }else {
                    val a="exist"
                    Log.d("ViewModel","ViewModel.getは$a")

                    // Otherwise the ViewModel has been retained, set the chronometer's base to the original
                    // starting time.
                    live_bitmap=bitmapViewModel.getBitmap()
                    //bitmap = live_bitmap!!.value
                    Glide.with(this)
                        .load(live_bitmap!!.value)
                        .into(imgView!!)
                }

             */

                /*
                //表示できた！！
                Glide.with(this)
                    .load(bitmap)
                    .into(imageView3!!)
                 */


/*
            Log.d("ShowImg","問題表示ボタン押した")
            imgView= view?.findViewById<ImageView>(R.id.imageView)
            //ボタンを押すたびに新しい画像が出るように今表示してある画像を空にする
            //imgView?.setImageDrawable(null)

            //関数にしたほうが見やすいか？
            //ここを撮影画像のみ選べるようにする　or 直前の撮影画像を持ってくる
            var random_num = range?.random()
            //選ばれた配列
            val result= RefList!![random_num!!-1]
            Log.i("Storage","random_num is $random_num")
            Log.i("Storage","random_item is $result")

            val gsReference = storage.getReferenceFromUrl(result)
            val ONE_MEGABYTE: Long = 1024 * 1024 *10 //最大ダウンロードできるバイトサイズ　小さいとエラーになる

            gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {


                Log.d("ShowImg","imgView読み込んだ")
                GlideApp.with(this)
                    .load(gsReference)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(imgView!!)
                Log.d("ShowImg","画像表示できた")

            }.addOnFailureListener {
                // Handle any errors
                Log.e("ShowImg", "False")
            }



        }
        view.isClickable = false

        model = activity?.run {
            ViewModelProvider(this).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        //imgView = model.

             */
        Log.d(TAG,"fragment.onViewCreated")
    }
    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.showimg_dialog,null)
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
            findNavController().navigate(R.id.action_showImgFragment_to_takeImgFragment)
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // If there's a download in progress, save the reference so you can query it later
        outState.putString("reference", gsReference.toString())
    }

}
