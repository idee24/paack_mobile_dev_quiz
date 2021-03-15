package com.example.paack_mobile_dev_quiz.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.paack_mobile_dev_quiz.DeliveryRepository
import com.example.paack_mobile_dev_quiz.networking.ApiService
import com.example.paack_mobile_dev_quiz.networking.Delivery
import com.example.paack_mobile_dev_quiz.networking.Resource


/**
 *@Created by Yerimah on 3/14/2021.
 */

class MainViewModel(apiService: ApiService): ViewModel() {

    private val deliveryRepository = DeliveryRepository(apiService)

    fun getDeliveries(): LiveData<Resource<List<Delivery>>> {
        return deliveryRepository.getDeliveries()
    }

    fun getDeliveryDetails(deliveryId: Int): LiveData<Resource<Delivery>> {
        return deliveryRepository.getDeliveryDetails(deliveryId)
    }

}