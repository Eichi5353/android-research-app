package com.example.myprototype

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
//account1: abc@gmail.com -> Pass:abcdef
//エラーメッセージを書くTextViewを追加して具体的なエラー内容を表示するようにする
//ユーザー名がかぶった時にエラーを出すようにしたいが，現状Registerしてからエラーが出てしまう
//画面遷移を遅らせれば行けるのでは？

class RegisterFragment : Fragment() {
    var editTextEmail: EditText? = null
    var editTextPassword: EditText? = null
    var editTextName:EditText? = null
    var buttonReg:Button? = null
    var progressBar: ProgressBar? = null
    //var textView:TextView? = null
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        view.findViewById<TextView>(R.id.loginNow).setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)///あれ？？？？
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextEmail = view?.findViewById(R.id.email)
        editTextPassword = view?.findViewById(R.id.password)
        editTextName = view?.findViewById(R.id.username)
        progressBar = view?.findViewById(R.id.progressBar)

        val context: Context? = requireActivity().applicationContext
        auth = Firebase.auth
        view.findViewById<Button>(R.id.btn_register).setOnClickListener {
            var email: String = editTextEmail?.getText().toString()
            var password: String = editTextPassword?.getText().toString()
            val username: String = editTextName?.getText().toString()

            progressBar?.visibility = ProgressBar.VISIBLE
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(context, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(context, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val ref = db.collection("users").document(username)
            val query = db.collection("users").whereEqualTo("name", username)
            Log.d(TAG, "query = ${query}")
            Log.d(TAG, "username = ${username}")
            /*
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    // querySnapshotに検索結果が含まれるので、必要な処理を行う
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                } else {
                    // エラーハンドリング
                }
            }


             */
            /*
            //非同期処理しないとNullにならないから間違っている
            if(query != null) {
                Toast.makeText(context, "Username already exists.\nPlease enter a different name ", Toast.LENGTH_LONG)
                    .show()
                Log.d(TAG, "Username already exists.")
                progressBar?.visibility = ProgressBar.GONE
                return@setOnClickListener
            }

             */


            GlobalScope.launch(Dispatchers.Main) {
                try {
                    // ユーザー名が既に存在する場合はユーザーに再入力を促す
                    if (isUsernameExists(username)) {
                        Toast.makeText(
                            context,
                            "Username already exists. Please choose a different username.",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressBar?.visibility = ProgressBar.GONE
                        //findNavController().navigate(R.id.action_loginFragment_self)
                        return@launch
                    }

                    // ここにユーザー名が既に存在しない場合の登録処理を書く

                    val db_user = hashMapOf<String, Any>(
                        "name" to username,
                    )
                    /*
                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                                //dbにuser名を追加する処理
                                val db_user = hashMapOf<String, Any>(
                                    "name" to username,
                                )
                                Log.d(TAG, "Hello")
                                val ref = db.collection("users").document(username)


                     */
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            progressBar?.visibility = ProgressBar.GONE
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")

                                //transactionはFirebaseのオブジェクト　DBを読み書きしたりするのが便利になるらしい
                                db.runTransaction { transaction ->
                                    // ドキュメントが既に存在する場合はエラーをスロー
                                    if (transaction.get(ref).exists()) {
                                        throw FirebaseFirestoreException(
                                            "ドキュメント名が重複しています",
                                            FirebaseFirestoreException.Code.ABORTED
                                        )
                                    }

                                    //transaction.set(ref, db_user)
                                    Log.d(TAG, "Document ID: ${ref}")
                                    null
                                }.addOnSuccessListener {
                                    // 成功時の処理
                                    Log.d(
                                        TAG,
                                        "username added with ID: ${username}"
                                    )
                                    val user = auth.currentUser
                                    val profileUpdates =
                                        userProfileChangeRequest {
                                            displayName = username
                                        }
                                    user!!.updateProfile(profileUpdates)
                                        .addOnCompleteListener { updateTask ->
                                            if (updateTask.isSuccessful) {
                                                Log.d(TAG, "User profile updated.")
                                                //dbにuser名を追加する処理
                                                Log.d(TAG, "Hello")
                                            }else
                                                Log.d(TAG, "User profile could not update.")
                                        }
                                    Toast.makeText(context, "Account create.", Toast.LENGTH_SHORT,)
                                        .show()
                                    //画面遷移を遅らせれば行けるのでは？
                                    findNavController().navigate(R.id.action_registerFragment_to_titleFragment)
                                }.addOnFailureListener { e ->
                                    // エラー時の処理
                                    Log.w(TAG, "Error adding document", e)
                                    Log.w(
                                        TAG,
                                        "createUsername:failure",
                                        task.exception
                                    )
                                    Toast.makeText(
                                        context,
                                        "Authentication username failed.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(
                                    TAG,
                                    "createUserWithEmail:failure",
                                    task.exception
                                )
                                Toast.makeText(
                                    context,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                //updateUI(null)
                            }
                        }
                    //} else
                    //  Log.d(TAG, "User profile could not update.")
                    //}

                    progressBar?.visibility = ProgressBar.GONE
                } catch (e: Exception) {
                    // エラーハンドリング
                    Toast.makeText(context, "Error occurred.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error occurred: ${e.message}")
                    e.printStackTrace()
                    progressBar?.visibility = ProgressBar.GONE
                }
            }
        }
    }



    private suspend fun isUsernameExists(username: String): Boolean = suspendCoroutine { continuation ->
        val docRef = db.collection("users").document(username)
        docRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    continuation.resume(documentSnapshot.exists())
                } else {
                    val exception = task.exception
                    if (exception != null) {
                        continuation.resumeWithException(exception)
                    } else {
                        continuation.resume(false) // ユーザー名が存在しないときもfalseを返す
                    }
                }
            }
    }


    companion object {
        val TAG ="RegisterFragment"
    }
}