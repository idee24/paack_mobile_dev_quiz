package com.example.paack_mobile_dev_quiz.networking

import java.util.*

/**
 *Created by Yerimah on 3/13/2021.
 */

data class Delivery(
        var id: Int?,
        var address: String?,
        var longitude: Double?,
        var latitude: Double?,
        var customer_name: String?,
        var requires_signature: Boolean?,
        var special_instructions: String?,
        var timestamp: Long?
)

data class DeliveryUpdate(
        var latitude: Double?,
        var longitude: Double?,
        var battery_level: Int?,
        var timestamp: Long?
)

data class DeliveryUpdatePayload(
        var driver_id: Int? = 0,
        var tracking_data: List<DeliveryUpdate>? = LinkedList()
)

data class StatusResponse(
        var status: String?
)
