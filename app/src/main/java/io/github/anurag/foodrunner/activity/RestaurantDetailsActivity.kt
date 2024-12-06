package com.anurag.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.anurag.foodrunner.R
import com.anurag.foodrunner.adapter.AllRestaurantsAdapter
import com.anurag.foodrunner.adapter.AllRestaurantsAdapter.GetAllFavAsyncTask
import com.anurag.foodrunner.adapter.ResDetailRecyclerAdapter
import com.anurag.foodrunner.database.OrderEntity
import com.anurag.foodrunner.database.RestaurantDatabase
import com.anurag.foodrunner.database.RestaurantEntity
import com.anurag.foodrunner.model.RestaurantsDetails
import com.anurag.foodrunner.util.ConnectionManager

class RestaurantDetailsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    lateinit var imgResIsFav: ImageView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var relativeLayout: RelativeLayout
    lateinit var btnGoToCart: Button

    private val dishInfoList = arrayListOf<RestaurantsDetails>()
    private val orderList = arrayListOf<RestaurantsDetails>()

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerAdapter: ResDetailRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var restaurantName: String

    var restaurantId: String = "100"

    lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_details)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        title = intent.getStringExtra("name").toString()

        init()
        setupToolbar()

        btnGoToCart.setOnClickListener {
            proceedToCart()
        }


        layoutManager = LinearLayoutManager(this@RestaurantDetailsActivity)

        if (intent != null) {
            restaurantId = intent.getIntExtra("id", 100).toString()
            restaurantName = intent.getStringExtra("name") as String

            val listOfFav = GetAllFavAsyncTask(this).execute().get()

            if (listOfFav.isNotEmpty() && listOfFav.contains(restaurantId)) {
                imgResIsFav.setImageResource(R.drawable.ic_fav_red_filled)
            } else {
                imgResIsFav.setImageResource(R.drawable.ic_fav_red_outline)
            }
        } else {
            finish()
            Toast.makeText(
                this@RestaurantDetailsActivity,
                "Some unexpected Error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (restaurantId == "100") {
            finish()
            Toast.makeText(
                this@RestaurantDetailsActivity,
                "Some unexpected Error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }

        imgResIsFav.setOnClickListener {
            if (intent != null) {

                val restaurantEntity = RestaurantEntity(
                    intent.getIntExtra("id", 100),
                    intent.getStringExtra("name")!!,
                    intent.getStringExtra("rating")!!,
                    intent.getStringExtra("cost")!!,
                    intent.getStringExtra("image_url")!!
                )


                if (!AllRestaurantsAdapter.DBAsyncTask(this, restaurantEntity, 1).execute().get()) {
                    val async =
                        AllRestaurantsAdapter.DBAsyncTask(this, restaurantEntity, 2).execute()
                    val result = async.get()
                    if (result) {
                        imgResIsFav.setImageResource(R.drawable.ic_fav_red_filled)
                    }
                } else {
                    val async =
                        AllRestaurantsAdapter.DBAsyncTask(this, restaurantEntity, 3).execute()
                    val result = async.get()

                    if (result) {
                        imgResIsFav.setImageResource(R.drawable.ic_fav_red_outline)
                    }
                }
            }
        }

        val queue = Volley.newRequestQueue(this@RestaurantDetailsActivity)

        if (ConnectionManager().isNetworkAvailable(this@RestaurantDetailsActivity)) {
            val jsonRequest = object : JsonObjectRequest(Method.GET,
                "http://13.235.250.119/v2/restaurants/fetch_result/$restaurantId",
                null,
                Response.Listener {

                    try {
                        val obj2 = it.getJSONObject("data")
                        val success = obj2.getBoolean("success")
                        if (success) {
                            orderList.clear()
                            val data = obj2.getJSONArray("data")
                            progressLayout.visibility = View.GONE
                            for (i in 0 until data.length()) {
                                val dishJsonObject = data.getJSONObject(i)
                                val dishObject = RestaurantsDetails(
                                    dishJsonObject.getString("id"),
                                    dishJsonObject.getString("name"),
                                    dishJsonObject.getString("cost_for_one")
                                )

                                dishInfoList.add(dishObject)
                                recyclerAdapter =
                                    ResDetailRecyclerAdapter(this@RestaurantDetailsActivity,
                                        dishInfoList,
                                        object : ResDetailRecyclerAdapter.OnItemClickListener {
                                            override fun onAddItemClick(dishObject: RestaurantsDetails) {
                                                orderList.add(dishObject)
                                                if (orderList.size > 0) {
                                                    btnGoToCart.visibility = View.VISIBLE
                                                    ResDetailRecyclerAdapter.isCartEmpty = false
                                                }
                                            }

                                            override fun onRemoveItemClick(dishObject: RestaurantsDetails) {
                                                orderList.remove(dishObject)
                                                if (orderList.isEmpty()) {
                                                    btnGoToCart.visibility = View.GONE
                                                    ResDetailRecyclerAdapter.isCartEmpty = true
                                                }
                                            }

                                        })

                                recyclerView.adapter = recyclerAdapter
                                recyclerView.itemAnimator = DefaultItemAnimator()
                                recyclerView.layoutManager = layoutManager
                            }
                        } else {
                            Toast.makeText(
                                this@RestaurantDetailsActivity,
                                "Some Error Occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@RestaurantDetailsActivity,
                            "Some Exception Occurred $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(
                        this@RestaurantDetailsActivity,
                        "Volley error",
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
            queue.add(jsonRequest)
        } else {
            val dialog: AlertDialog.Builder =
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()

            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@RestaurantDetailsActivity)
            }
            dialog.create()
            dialog.show()
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }


    private fun proceedToCart() {
        val gson = Gson()
        val foodItems = gson.toJson(orderList)
        val async = CartItems(
            this@RestaurantDetailsActivity,
            restaurantId.toString(),
            foodItems,
            1
        ).execute()
        val result = async.get()
        if (result) {

            val intent = Intent(this@RestaurantDetailsActivity, CartActivity::class.java)
            intent.putExtra("resId", restaurantId)
            intent.putExtra("resName", restaurantName)
            startActivity(intent)

        } else {
            Toast.makeText(
                this@RestaurantDetailsActivity,
                "Some unexpected error",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    class CartItems(
        context: Context,
        private val restaurantId: String,
        private val foodItems: String,
        val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {
        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    override fun onBackPressed() {


        if (orderList.size > 0) {
            val alterDialog: AlertDialog.Builder =
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
            alterDialog.setTitle("Alert!")
            alterDialog.setMessage("Going back will remove everything from cart")
            alterDialog.setPositiveButton("Okay") { text, listener ->
                super.onBackPressed()
            }
            alterDialog.setNegativeButton("No") { text, listener ->

            }
            alterDialog.show()
        } else {
            super.onBackPressed()
        }

    }

    private fun init() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerResMenu)
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE
        btnGoToCart = findViewById(R.id.btnCart)
        imgResIsFav = findViewById(R.id.imgResIsFav)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
    }


}