package com.example.myprototype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RuleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RuleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var TAG: String? = "RuleFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rule, container, false)
        view.findViewById<Button>(R.id.btn_toTitle).setOnClickListener{
            findNavController().navigate(R.id.action_ruleFragment_to_titleFragment)
        }
        return view
    }

}