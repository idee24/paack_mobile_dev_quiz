package com.example.paack_mobile_dev_quiz.networking

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *Created by Yerimah on 3/12/2021.
 */
interface ApiService {

    @GET(Routes.DELIVERIES_END_POINT)
    suspend fun getDeliveries(): List<Delivery>

    @GET(Routes.DELIVERY_DETAILS_END_POINT)
    suspend fun getDeliveryDetails(@Query("delivery_id") deliveryId: Int): Delivery

    @POST(Routes.TRACKING_END_POINT)
    fun updateDeliveryDetails(@Body payload: DeliveryUpdate): Call<StatusResponse>
}