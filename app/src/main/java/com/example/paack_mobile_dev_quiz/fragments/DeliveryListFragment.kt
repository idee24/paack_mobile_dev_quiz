package com.example.paack_mobile_dev_quiz.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paack_mobile_dev_quiz.Constants
import com.example.paack_mobile_dev_quiz.MainActivity
import com.example.paack_mobile_dev_quiz.R
import com.example.paack_mobile_dev_quiz.adapters.DeliveryListAdapter
import com.example.paack_mobile_dev_quiz.networking.*
import com.example.paack_mobile_dev_quiz.viewmodels.MainViewModel
import com.example.paack_mobile_dev_quiz.viewmodels.MainViewModelFactory
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_delivery_list.*

/**
 *@Created by Yerimah on 3/12/2021.
 */
class DeliveryListFragment : Fragment(R.layout.fragment_delivery_list) {

    private lateinit var context: MainActivity
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as MainActivity
        initViewModel()
    }

    private fun initViewModel() {
        val factory = MainViewModelFactory(ApiClient.apiClient(Routes.BASE_URL).create(ApiService::class.java))
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getDeliveries()
    }

    private fun getDeliveries() {
        viewModel.getDeliveries().observe(viewLifecycleOwner, Observer {
            it.let { resource ->
                when(resource.status) {
                    Status.SUCCESS -> {
                        if (!resource.data.isNullOrEmpty()) {
                            initRecyclerView(resource.data)
                        }
                    }
                    Status.ERROR -> {
                        Snackbar.make(constraintLayout, resource.message ?: "A problem occurred",
                            BaseTransientBottomBar.LENGTH_SHORT).show()
                    }
                    Status.LOADING -> {  }
                }
            }
        })
    }

    private fun onDeliveryItemSelected(deliveryId: Int) {
        val bundle = bundleOf(Constants.DELIVERY_ID_KEY to deliveryId)
        findNavController().navigate(R.id.action_deliveryListFragment_to_deliveryDetailsFragment, bundle)
    }

    private fun initRecyclerView(deliveries: List<Delivery>) {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        deliveryRecyclerView.layoutManager = layoutManager
        deliveryRecyclerView.adapter = DeliveryListAdapter(deliveries, ::onDeliveryItemSelected)
    }
}