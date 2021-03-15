package com.example.paack_mobile_dev_quiz

import androidx.lifecycle.liveData
import com.example.paack_mobile_dev_quiz.networking.ApiService
import com.example.paack_mobile_dev_quiz.networking.Resource
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

/**
 *@Created by Yerimah on 3/15/2021.
 */

class DeliveryRepository(private val apiService: ApiService) {

    fun getDeliveries() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiService.getDeliveries()))
        }
        catch (e: Exception) {
            emit(Resource.error(data = null, message = e.message ?: "A problem occurred"))
        }
    }

    fun getDeliveryDetails(deliveryId: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiService.getDeliveryDetails(deliveryId)))
        }
        catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.error(data = null, message = e.message ?: "A problem occurred"))
        }
    }

}