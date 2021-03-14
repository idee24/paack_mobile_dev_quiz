package com.example.paack_mobile_dev_quiz.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.paack_mobile_dev_quiz.R
import com.example.paack_mobile_dev_quiz.networking.Delivery
import kotlinx.android.synthetic.main.delivery_item.view.*

/**
 *@Created by Yerimah on 3/12/2021.
 */

class DeliveryListAdapter(private val deliveries: List<Delivery>, private val callback: (Int) -> Unit):
    RecyclerView.Adapter<DeliveryListAdapter.DeliveryListVewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryListVewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.delivery_item, parent, false)
        return DeliveryListVewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryListVewHolder, position: Int) {
        deliveries[position].let { delivery ->
            holder.nameTextView.text = delivery.customer_name
            holder.addressTextView.text = delivery.address
            holder.idTextView.text = delivery.id.toString()
            holder.itemView.setOnClickListener { callback(delivery.id ?: 0) }
        }
    }

    override fun getItemCount() = deliveries.size

    inner class DeliveryListVewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.customerNameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
    }
}