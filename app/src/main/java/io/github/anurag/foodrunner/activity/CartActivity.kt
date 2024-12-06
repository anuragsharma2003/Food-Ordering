package com.anurag.foodrunner.activity

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.anurag.foodrunner.R
import com.anurag.foodrunner.adapter.CartRecyclerAdapter
import com.anurag.foodrunner.adapter.ResDetailRecyclerAdapter
import com.anurag.foodrunner.database.OrderEntity
import com.anurag.foodrunner.database.RestaurantDatabase
import com.anurag.foodrunner.fragment.OrderHistoryFragment
import com.anurag.foodrunner.model.RestaurantsDetails
import com.anurag.foodrunner.util.ConnectionManager
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var coordinateLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    val orderList = ArrayList<RestaurantsDetails>()
    lateinit var progressBarCart: ProgressBar
    lateinit var rlMyCart: RelativeLayout
    lateinit var txtResName: TextView
    private lateinit var recyclerAdapter: CartRecyclerAdapter
    lateinit var frameLayout: FrameLayout
    lateinit var btnOrder: Button
    lateinit var resId: String
    lateinit var resName: String
    var sum = 0
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this@CartActivity)
        coordinateLayout = findViewById(R.id.coordinateLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frameLayout)
        txtResName = findViewById(R.id.txtResName)

        progressBarCart = findViewById(R.id.progressBarCart)
        rlMyCart = findViewById(R.id.rlMyCart)
        btnOrder = findViewById(R.id.btnOrder)

        setUpToolbar()

        if (intent != null) {
            resId = intent.getStringExtra("resId").toString()
            resName = intent.getStringExtra("resName") as String
            txtResName.text = resName
        } else {
            finish()
            Toast.makeText(
                this@CartActivity,
                "Some unexpected Error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (resId == "0") {
            finish()
            Toast.makeText(
                this@CartActivity,
                "Some unexpected Error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }

        cartList()
        placeOrder()
    }


    class GetItemsDBAsync(context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDao().getAllOrders()
        }
    }

    private fun cartList() {

        val list = GetItemsDBAsync(applicationContext).execute().get()
        for (element in list) {

            orderList.addAll(
                Gson().fromJson(
                    element.foodItems,
                    Array<RestaurantsDetails>::class.java
                ).asList()
            )
        }
        if (orderList.isEmpty()) {
            rlMyCart.visibility = View.GONE
        } else {
            rlMyCart.visibility = View.VISIBLE
        }

        recyclerAdapter = CartRecyclerAdapter(orderList, this@CartActivity)
        layoutManager = LinearLayoutManager(this@CartActivity)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = recyclerAdapter
    }

    private fun placeOrder() {
        for (i in 0 until orderList.size) {
            sum += orderList[i].dishPrice.toInt()
        }
        val total = "Place Order(Total: Rs. $sum)"
        btnOrder.text = total
        btnOrder.setOnClickListener {
            progressBarCart.visibility = View.VISIBLE
            sendRequest()
        }
    }

    private fun setUpToolbar() {

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun sendRequest() {

        val queue = Volley.newRequestQueue(this@CartActivity)
        val url = "http://13.235.250.119/v2/place_order/fetch_result/"

        if (ConnectionManager().isNetworkAvailable(this)) {
            try {
                val jsonParams = JSONObject()
                jsonParams.put(
                    "user_id",
                    this@CartActivity.getSharedPreferences(
                        getString(R.string.preference_file_name),
                        Context.MODE_PRIVATE
                    ).getString(
                        "user_id", "0"
                    ) as String
                )

                jsonParams.put("restaurant_id", resId.toString())
                var total = 0
                for (i in 0 until orderList.size) {
                    total += orderList[i].dishPrice.toInt()
                }
                jsonParams.put("total_cost", total.toString())
                val dishArray = JSONArray()
                for (i in 0 until orderList.size) {
                    val dish_id = JSONObject()
                    dish_id.put("food_item_id", orderList[i].dishId)
                    dishArray.put(i, dish_id)
                }
                jsonParams.put("food", dishArray)
                val jsonObjectRequest =
                    object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                        val obj = it.getJSONObject("data")
                        val success = obj.getBoolean("success")
                        if (success) {
                            ClearDBAsync(applicationContext, resId.toString()).execute().get()
                            ResDetailRecyclerAdapter.isCartEmpty = true

                            createNotification()
                            val intent = Intent(this, PlaceOrderActivity::class.java)
                            startActivity(intent)
                            finishAffinity()

                        } else {
                            progressBarCart.visibility = View.GONE
                            rlMyCart.visibility = View.VISIBLE
                            val responseMessageServer =
                                obj.getString("errorMessage")
                            Toast.makeText(
                                this@CartActivity,
                                responseMessageServer,
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }, Response.ErrorListener {
                        rlMyCart.visibility = View.VISIBLE
                        Toast.makeText(
                            this@CartActivity,
                            "Volley Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "9bf534118365f1"
                            return headers
                        }
                    }
                queue.add(jsonObjectRequest)
            } catch (e: Exception) {
                progressBarCart.visibility = View.GONE
                rlMyCart.visibility = View.VISIBLE
                e.printStackTrace()
            }
        } else {
            progressBarCart.visibility = View.GONE
            val alterDialog: AlertDialog.Builder =
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit") { text, listener ->
                finishAffinity()//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()
        }
    }

    class ClearDBAsync(context: Context, val resId: String) : AsyncTask<Void, Void, Boolean>() {
        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onBackPressed() {
        val alterDialog: AlertDialog.Builder =
            AlertDialog.Builder(this, R.style.AlertDialogStyle)
        alterDialog.setTitle("Alert!")
        alterDialog.setMessage("Going back will remove everything from cart")
        alterDialog.setPositiveButton("Okay") { text, listener ->
            ClearDBAsync(applicationContext, resId.toString()).execute().get()
            ResDetailRecyclerAdapter.isCartEmpty = true
            super.onBackPressed()
        }
        alterDialog.setNegativeButton("No") { text, listener ->

        }
        alterDialog.show()
    }

    override fun onStop() {
        ClearDBAsync(applicationContext, resId.toString()).execute().get()
        ResDetailRecyclerAdapter.isCartEmpty = true
        super.onStop()
    }

    fun createNotification() {
        val notificationId = sharedPreferences.getInt("notification_id", 1)
        val GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL"
        sharedPreferences.edit().putInt("notification_id", notificationId + 1).apply()

        val channelId = "personal_notification"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Order Placed")
            .setContentText("Your order has been placed successfully!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Ordered from $resName and amounting to Rs.$sum .Thank you for ordering from FoodRunner. Stay Safe!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(GROUP_KEY_WORK_EMAIL)
            .setAutoCancel(true)

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, notificationBuilder.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = "Order Placed"
            val description = "Your order has been placed!"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description
            val notificationManager =
                (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


}
