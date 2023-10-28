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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    var editTextEmail: EditText? = null
    var editTextPassword: EditText? = null
    var progressBar: ProgressBar? = null
    private lateinit var auth: FirebaseAuth



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login2, container, false)
        view.findViewById<TextView>(R.id.registerNow).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextEmail = view?.findViewById(R.id.email)
        editTextPassword = view?.findViewById(R.id.password)
        progressBar = view?.findViewById(R.id.progressBar)
        auth = Firebase.auth
        val context: Context? = requireActivity().applicationContext
        view.findViewById<Button>(R.id.btn_login).setOnClickListener {
            var email: String = editTextEmail?.getText().toString()
            var password: String = editTextPassword?.getText().toString()
            progressBar?.visibility = ProgressBar.VISIBLE

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)){
                Toast.makeText(context, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)//「signIn」
                .addOnCompleteListener{ task ->
                    progressBar?.visibility = ProgressBar.GONE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(context, "Login Successful!.", Toast.LENGTH_SHORT,).show()
                        findNavController().navigate(R.id.action_loginFragment_to_titleFragment)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                    }
                }
        }
    }
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_titleFragment)
        }
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}