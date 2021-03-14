package com.example.paack_mobile_dev_quiz.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.paack_mobile_dev_quiz.Constants
import com.example.paack_mobile_dev_quiz.MainActivity
import com.example.paack_mobile_dev_quiz.R
import com.example.paack_mobile_dev_quiz.networking.*
import com.example.paack_mobile_dev_quiz.utils.getFormattedDate
import com.example.paack_mobile_dev_quiz.viewmodels.MainViewModel
import com.example.paack_mobile_dev_quiz.viewmodels.MainViewModelFactory
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_delivery_details.*
import kotlinx.android.synthetic.main.fragment_delivery_details.constraintLayout
import kotlinx.android.synthetic.main.fragment_delivery_list.*

/**
 *@Created by Yerimah on 3/12/2021.
 */
class DeliveryDetailsFragment : Fragment(R.layout.fragment_delivery_details), OnMapReadyCallback {

    private lateinit var context: MainActivity
    private lateinit var viewModel: MainViewModel
    private lateinit var mapView: SupportMapFragment
    private lateinit var gMap: GoogleMap

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
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView.getMapAsync(this)
        backIcon.setOnClickListener { findNavController().popBackStack() }
        val deliveryId = arguments?.getInt(Constants.DELIVERY_ID_KEY)
        getDeliveryDetails(deliveryId ?: 0)
    }

    private fun getDeliveryDetails(deliveryId: Int) {
        viewModel.getDeliveryDetails(deliveryId).observe(viewLifecycleOwner, Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data.let { delivery ->
                            if (delivery != null) {
                                initViews(delivery)
                            }
                        }
                    }
                    Status.ERROR -> {
                        Snackbar.make(constraintLayout, resource.message ?: "A problem occurred",
                            BaseTransientBottomBar.LENGTH_SHORT).show()
                    }
                    Status.LOADING -> {

                    }
                }
            }
        })
    }

    private fun initViews(delivery: Delivery) {

        gMap.addMarker(MarkerOptions().position(LatLng(delivery.latitude ?: 0.0, delivery.longitude ?: 0.0))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_place_marker))).showInfoWindow()
        val cameraPosition = CameraPosition.Builder()
            .bearing(0.toFloat())
            .target(LatLng(delivery.latitude ?: 0.0, delivery.longitude ?: 0.0))
            .zoom(15.toFloat())
            .build()
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        customerNameTextView.text = delivery.customer_name
        instructionTextView.text = delivery.special_instructions


    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            gMap = googleMap
        }
        MapsInitializer.initialize(context.applicationContext)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
    }
}