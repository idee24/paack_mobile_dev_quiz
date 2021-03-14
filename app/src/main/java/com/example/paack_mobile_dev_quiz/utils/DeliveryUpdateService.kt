package com.example.paack_mobile_dev_quiz.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.paack_mobile_dev_quiz.R
import com.example.paack_mobile_dev_quiz.networking.*
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 *@Created by Yerimah on 3/12/2021.
 */
class DeliveryUpdateService: Service() {

    private val servicePackageName = "com.google.android.gms.location.sample.locationupdatesforegroundservice"
    private val channelId = "location_channel"
    private val tag = DeliveryUpdateService::class.java.simpleName
    private val actionBroadcast: String = "$servicePackageName.broadcast"
    private val extraLocation: String = "$servicePackageName.location"
    private val extraStartedFromNotification: String = "$servicePackageName.started_from_notification"
    private val updateIntervalMS: Long = 10000
    private val fastIntervalMS = updateIntervalMS / 2

    private val notificationId = 12345678
    private var changingConfiguration = false
    private lateinit var notificationManager: NotificationManager
    private val binder = LocalBinder()

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    lateinit var  serviceHandler: Handler
    lateinit var broadcast: Intent


    override fun onCreate() {
        super.onCreate()
        broadcast = Intent()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                updateDelivery(locationResult.lastLocation)
            }
        }
        println("DDLS Service Started")
        locationRequest = LocationRequest()
        locationRequest.interval = updateIntervalMS
        locationRequest.fastestInterval = fastIntervalMS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val handlerThread = HandlerThread(tag)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                updateDelivery(it)
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startedFromNotification = intent!!.getBooleanExtra(extraStartedFromNotification, false)
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    fun removeLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            stopSelf()
        } catch (e: SecurityException) {}
    }

    fun requestLocationUpdates() {
        startService(Intent(applicationContext, DeliveryUpdateService::class.java))
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
        if (changingConfiguration) {
            startForeground(notificationId, getNotification())
        }
        return true
    }

    override fun onDestroy() {
        removeLocationUpdates()
        serviceHandler.removeCallbacksAndMessages(null)
    }

    private fun getNotification(): Notification? {
        val intent = Intent(this, DeliveryUpdateService::class.java)
        intent.putExtra(extraStartedFromNotification, true)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setContentText("Delivery Service Running")
        builder.setContentTitle("Location")
        builder.setOngoing(true)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setTicker("Delivery Service Running")
        builder.setWhen(System.currentTimeMillis())
        return builder.build()
    }


    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    class LocalBinder : Binder() {
        val service: DeliveryUpdateService
            get() = DeliveryUpdateService()
    }

    private fun updateDelivery(location: Location) {
        val intent = Intent(actionBroadcast)
        intent.putExtra(extraLocation, location)
        notificationManager.notify(notificationId, getNotification())


        println("DDLS Service Update Fired")
        val updatePayload = DeliveryUpdate(location.latitude, location.longitude, getBatteryLevel(baseContext), Date().time)
        val call = ApiClient.apiClient(Routes.BASE_URL).create(ApiService::class.java).updateDeliveryDetails(updatePayload)
        call.enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) { }
            override fun onFailure(call: Call<StatusResponse>, t: Throwable) { t.printStackTrace() }
        })


    }
}