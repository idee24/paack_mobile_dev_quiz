package com.example.paack_mobile_dev_quiz.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.paack_mobile_dev_quiz.networking.ApiService
import com.example.paack_mobile_dev_quiz.networking.Resource
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher
import java.lang.Exception

/**
 *@Created by Yerimah on 3/14/2021.
 */

class MainViewModel(private val apiService: ApiService): ViewModel() {

    fun getDeliveries() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            Resource.success(data = apiService.getDeliveries())
        }
        catch (e: Exception) {
            emit(Resource.error(data = null, message = e.message ?: "A problem occurred"))
        }
    }

    fun getDeliveryDetails(deliveryId: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            Resource.success(data = apiService.getDeliveryDetails(deliveryId))
        }
        catch (e: Exception) {
            emit(Resource.error(data = null, message = e.message ?: "A problem occurred"))
        }
    }
}