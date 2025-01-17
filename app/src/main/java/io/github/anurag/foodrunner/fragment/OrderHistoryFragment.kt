package com.anurag.foodrunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anurag.foodrunner.R
import com.anurag.foodrunner.adapter.OrderHistoryAdapter
import com.anurag.foodrunner.model.OrderDetails
import com.anurag.foodrunner.util.ConnectionManager

class OrderHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private var orderHistoryList = ArrayList<OrderDetails>()
    private lateinit var llOrders: LinearLayout
    private lateinit var rlNoOrder: RelativeLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressLayout: RelativeLayout
    private var userId = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        llOrders = view.findViewById(R.id.llOrders)
        rlNoOrder = view.findViewById(R.id.rlNoOrder)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressLayout = view?.findViewById(R.id.progressLayout) as RelativeLayout
        progressLayout.visibility = View.VISIBLE
        sharedPreferences =
            (activity as Context).getSharedPreferences(
                getString(R.string.preference_file_name),
                Context.MODE_PRIVATE
            )
        userId = sharedPreferences.getString("user_id", null).toString()

        return view
    }

    private fun sendRequest(userId: String) {
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"

        val jsonObjectRequest = object :
            JsonObjectRequest(Method.GET, url, null, Response.Listener {
                try {
                    progressLayout.visibility = View.GONE
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val resArray = data.getJSONArray("data")
                        if (resArray.length() == 0) {
                            llOrders.visibility = View.GONE
                            rlNoOrder.visibility = View.VISIBLE
                        } else {
                            for (i in 0 until resArray.length()) {
                                val orderObject = resArray.getJSONObject(i)
                                val foodItems = orderObject.getJSONArray("food_items")
                                val orderDetails = OrderDetails(
                                    orderObject.getString("order_id"),
                                    orderObject.getString("restaurant_name"),
                                    orderObject.getString("order_placed_at"),
                                    foodItems
                                )
                                orderHistoryList.add(orderDetails)
                                if (orderHistoryList.isEmpty()) {
                                    llOrders.visibility = View.GONE
                                    rlNoOrder.visibility = View.VISIBLE
                                } else {
                                    llOrders.visibility = View.VISIBLE
                                    rlNoOrder.visibility = View.GONE
                                    if (activity != null) {
                                        orderHistoryAdapter = OrderHistoryAdapter(
                                            activity as Context,
                                            orderHistoryList
                                        )
                                        val layoutManager =
                                            LinearLayoutManager(activity as Context)
                                        recyclerView.layoutManager = layoutManager
                                        recyclerView.itemAnimator = DefaultItemAnimator()
                                        recyclerView.adapter = orderHistoryAdapter
                                    } else {
                                        queue.cancelAll(this::class.java.simpleName)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "9bf534118365f1"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    override fun onResume() {
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            sendRequest(userId)
        } else {
            val alterDialog: AlertDialog.Builder =
                AlertDialog.Builder(activity, R.style.AlertDialogStyle)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be established!")
            alterDialog.setPositiveButton("Open Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNeutralButton("Retry")
            { _, _ ->
                onResume()
            }
            alterDialog.setNegativeButton("Exit")
            { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }
}