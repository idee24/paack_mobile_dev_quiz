package com.example.paack_mobile_dev_quiz.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by Yerimah on 3/14/2021.
 */

fun getFormattedDate(timeStamp: Long): String? {
    return try {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timeStamp))
    } catch (e: Exception){
        null
    }
}