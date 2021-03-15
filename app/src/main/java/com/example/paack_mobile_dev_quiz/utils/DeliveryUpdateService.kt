package com.example.paack_mobile_dev_quiz.utils

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.paack_mobile_dev_quiz.Constants
import com.example.paack_mobile_dev_quiz.networking.*
import com.google.android.gms.location.*
import com.pixplicity.easyprefs.library.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


/**
 *@Created by Yerimah on 3/12/2021.
 */
class DeliveryUpdateService : Service() {

    private val tag = DeliveryUpdateService::class.java.simpleName
    private val updateIntervalMS: Long = 1000
    private val fastInterval = updateIntervalMS / 2
    private var changingConfiguration = false
    private val binder = LocalBinder()
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    lateinit var  serviceHandler: Handler
    lateinit var broadcast: Intent
    val trackingList = ArrayList<DeliveryUpdate>()

    override fun onCreate() {
        super.onCreate()
        broadcast = Intent()
        val handlerThread = HandlerThread(tag)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                updateDelivery(locationResult.lastLocation)
            }
        }
        locationRequest = LocationRequest()
        locationRequest.interval = updateIntervalMS
        locationRequest.fastestInterval = fastInterval
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        scheduleUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun removeLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            stopSelf()
        } catch (e: SecurityException) {}
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changingConfiguration = true
    }

    override fun onBind(intent: Intent?): IBinder {
        stopForeground(true)
        changingConfiguration = false
        return binder
    }

    override fun onRebind(intent: Intent?) {
        stopForeground(true)
        changingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    override fun onDestroy() {
        removeLocationUpdates()
        updateTimer.cancel()
        updateTimer.purge()
        serviceHandler.removeCallbacksAndMessages(null)
    }


    class LocalBinder : Binder() {
        val service: DeliveryUpdateService
            get() = DeliveryUpdateService()
    }

    private fun updateDelivery(location: Location) {
        trackingList.add(DeliveryUpdate(location.latitude, location.longitude, getBatteryLevel(baseContext), Date().time))
    }

    var updateTimer = Timer()
    private fun scheduleUpdates() {
        updateTimer.schedule(object : TimerTask() {
            override fun run() {
                sendUpdate()
            }
        }, 10000, 10000)
    }

    private fun sendUpdate() {

        val payload = DeliveryUpdatePayload(Prefs.getInt(Constants.DELIVERY_ID_KEY, 0), trackingList)
        val call = ApiClient.apiClient(Routes.BASE_URL).create(ApiService::class.java).updateDeliveryDetails(payload)
        call.enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                trackingList.clear()
                val handler = Handler(Looper.getMainLooper())
                handler.post { Toast.makeText(this@DeliveryUpdateService,
                        "Delivery Updated", Toast.LENGTH_SHORT).show() }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}