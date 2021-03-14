package com.example.paack_mobile_dev_quiz.utils

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.text.SimpleDateFormat
import java.util.*


/**
 *Created by Yerimah on 3/14/2021.
 */

fun getBatteryLevel(context: Context): Int {
    val batLevel = context.getSystemService(BATTERY_SERVICE) as BatteryManager
    return batLevel.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}

fun getFormattedDate(timeStamp: Long): String? {
    return try {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timeStamp))
    } catch (e: Exception){
        null
    }
}

fun getFormattedTime(timeStamp: Long): String? {
    return try {
        return SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(Date(timeStamp))
    } catch (e: Exception){
        null
    }
}

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}