package com.example.paack_mobile_dev_quiz.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.paack_mobile_dev_quiz.MainActivity
import com.example.paack_mobile_dev_quiz.R

/**
 *@Created by Yerimah on 3/12/2021.
 */
class DeliveryListFragment : Fragment(R.layout.fragment_delivery_list) {

    private lateinit var context: MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as MainActivity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}