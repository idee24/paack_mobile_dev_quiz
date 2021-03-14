package com.example.paack_mobile_dev_quiz.utils

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.BatteryManager
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.paack_mobile_dev_quiz.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.snackbar.Snackbar
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

fun Fragment.showErrorMessage(context: Context,
                              message: String,
                              layout: View,
                              actionTitle: String,
                              action: View.OnClickListener) {

    val snackBar = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE)
    snackBar.setAction(actionTitle, action)
    snackBar.setActionTextColor(ContextCompat.getColor(context, android.R.color.white))
    val snackBarLayout = snackBar.view
    snackBarLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.logout_red))
    val textView = snackBarLayout.findViewById<TextView>(R.id.snackbar_text)
    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_outline_black_24dp, 0, 0, 0)
    textView.compoundDrawablePadding = context.resources.getDimensionPixelOffset(R.dimen.four_dp)
    snackBar.show()
}