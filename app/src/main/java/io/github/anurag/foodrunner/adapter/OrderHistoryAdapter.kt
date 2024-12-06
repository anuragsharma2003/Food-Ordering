package com.anurag.foodrunner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anurag.foodrunner.R
import com.anurag.foodrunner.model.OrderDetails
import com.anurag.foodrunner.model.RestaurantsDetails
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(val context: Context, private val orderList: ArrayList<OrderDetails>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    class OrderHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtResName: TextView = view.findViewById(R.id.txtResName)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtTotalPrice: TextView = view.findViewById(R.id.txtTotalPrice)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.recycler_orderhistory_single_row, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        var totalPrice: Int = 0
        val orderHistory = orderList[position]
        holder.txtResName.text = orderHistory.resName
        holder.txtDate.text = formatDate(orderHistory.orderDate)
        for (i in 0 until orderHistory.foodItem.length()) {
            val foodJson = orderHistory.foodItem.getJSONObject(i)
            totalPrice += foodJson.getString("cost").toInt()
        }
        holder.txtTotalPrice.text = "Total: \u20B9$totalPrice"
        println("Order History: $orderHistory, Order List: $orderList")
        setUpRecycler(holder.recyclerView, orderHistory)
    }

    private fun setUpRecycler(recyclerView: RecyclerView, orderList: OrderDetails) {
        val foodItems = ArrayList<RestaurantsDetails>()
        for (i in 0 until orderList.foodItem.length()) {
            val foodJson = orderList.foodItem.getJSONObject(i)
            foodItems.add(
                RestaurantsDetails(
                    foodJson.getString("food_item_id"),
                    foodJson.getString("name"),
                    foodJson.getString("cost")
                )
            )
        }
        val cartItemAdapter = CartRecyclerAdapter(foodItems, context)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = cartItemAdapter
    }

    fun formatDate(dateString: String): String? {
        val input = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.ENGLISH)
        val date: Date = input.parse(dateString) as Date
        val output = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return output.format(date)
    }
}
