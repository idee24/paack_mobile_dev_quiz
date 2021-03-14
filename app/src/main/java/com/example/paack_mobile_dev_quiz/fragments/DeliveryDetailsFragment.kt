package com.example.paack_mobile_dev_quiz.fragments

import android.Manifest
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.paack_mobile_dev_quiz.Constants
import com.example.paack_mobile_dev_quiz.MainActivity
import com.example.paack_mobile_dev_quiz.R
import com.example.paack_mobile_dev_quiz.networking.*
import com.example.paack_mobile_dev_quiz.utils.*
import com.example.paack_mobile_dev_quiz.viewmodels.MainViewModel
import com.example.paack_mobile_dev_quiz.viewmodels.MainViewModelFactory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_delivery_details.*
import kotlinx.android.synthetic.main.fragment_delivery_details.constraintLayout

/**
 *@Created by Yerimah on 3/12/2021.
 */
class DeliveryDetailsFragment : Fragment(R.layout.fragment_delivery_details), OnMapReadyCallback {

    private lateinit var context: MainActivity
    private lateinit var viewModel: MainViewModel
    private lateinit var mapView: SupportMapFragment
    private lateinit var gMap: GoogleMap
    private var deliveryService: DeliveryUpdateService? = null
    private var isBound = false

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
        val deliveryId = arguments?.getInt(Constants.DELIVERY_ID_KEY)
        getDeliveryDetails(deliveryId ?: 0)
    }

    override fun onResume() {
        super.onResume()
        displayLocationSettingsRequest()
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
            .icon(bitmapDescriptorFromVector(context, R.drawable.red_place_marker))).showInfoWindow()

        val cameraPosition = CameraPosition.Builder()
            .bearing(0.toFloat())
            .target(LatLng(delivery.latitude ?: 0.0, delivery.longitude ?: 0.0))
            .zoom(15.toFloat())
            .build()
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        dateTextView.text = getFormattedDate(delivery.timestamp ?: 0)
        customerNameTextView.text = delivery.customer_name
        instructionTextView.text = delivery.special_instructions

        timeTextView.text = getFormattedTime(delivery.timestamp ?: 0)
        addressTextView.text = delivery.address

        activeButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startDeliveryService()
            }
            else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(requestCode == Constants.LOCATION_REQUEST_CODE) {
                 mapView.getMapAsync(this)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        stopService()
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
        else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: DeliveryUpdateService.LocalBinder = service as DeliveryUpdateService.LocalBinder
            deliveryService = binder.service
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            deliveryService = null
            isBound = false
        }

    }

    private fun startDeliveryService() {
        context.bindService(Intent(context, DeliveryUpdateService::class.java),
            serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
        deliveryService?.removeLocationUpdates()
        val manager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    private fun displayLocationSettingsRequest() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 500
        locationRequest.fastestInterval = 100
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { locationException: java.lang.Exception? ->
            if (locationException is ResolvableApiException) {
                try {
                    locationException.startResolutionForResult(context, Constants.LOCATION_REQUEST_CODE)
                } catch (senderException: IntentSender.SendIntentException) {
                    senderException.printStackTrace()
                    showErrorMessage(context, "Please enable location setting to use your current address.",
                            constraintLayout, "RETRY") {displayLocationSettingsRequest()}
                }
            }
        }
    }
}