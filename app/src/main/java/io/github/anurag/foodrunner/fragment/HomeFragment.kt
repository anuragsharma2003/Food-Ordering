package com.anurag.foodrunner.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anurag.foodrunner.R
import com.anurag.foodrunner.adapter.AllRestaurantsAdapter
import com.anurag.foodrunner.model.Restaurants
import com.anurag.foodrunner.util.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class HomeFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var homeAdapter: AllRestaurantsAdapter
    lateinit var etSearch: EditText
    lateinit var radioButtonView: View
    lateinit var progressDialog: RelativeLayout
    lateinit var rlNoRestaurantFound: RelativeLayout

    var restaurantInfoList = arrayListOf<Restaurants>()
    var ratingComparator = Comparator<Restaurants>
    { rest1, rest2 ->

        if (rest1.rating.compareTo(rest2.rating, true) == 0) {
            rest1.name.compareTo(rest2.name, true)
        } else {
            rest1.rating.compareTo(rest2.rating, true)
        }

    }

    var costComparator = Comparator<Restaurants>
    { rest1, rest2 ->
        rest1.costForOne.toString().compareTo(rest2.costForOne.toString(), true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerRestaurants)
        etSearch = view.findViewById(R.id.etSearch)
        progressDialog = view.findViewById(R.id.rlLoading)
        rlNoRestaurantFound = view.findViewById(R.id.noRestaurantFound)

        rlNoRestaurantFound.visibility = View.INVISIBLE

        fun filterFun(strTyped: String) {
            rlNoRestaurantFound.visibility = View.INVISIBLE
            val filteredList = arrayListOf<Restaurants>()
            for (item in restaurantInfoList) {
                if (item.name.lowercase(Locale.ROOT)
                        .contains(strTyped.lowercase(Locale.ROOT))
                ) {
                    filteredList.add(item)
                }
            }
            if (filteredList.size == 0) {
                rlNoRestaurantFound.visibility = View.VISIBLE
            }
            homeAdapter.filterList(filteredList)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            //as the user types the search filter is applied
            override fun afterTextChanged(strTyped: Editable?) {
                filterFun(strTyped.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        )
        return view

    }

    fun fetchData() {
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            progressDialog.visibility = View.VISIBLE
            try {
                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener
                    {
                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {
                            val data = responseJsonObjectData.getJSONArray("data")

                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = Restaurants(
                                    restaurantJsonObject.getString("id").toInt(),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one").toInt(),
                                    restaurantJsonObject.getString("image_url")
                                )
                                restaurantInfoList.add(restaurantObject)

                                homeAdapter = AllRestaurantsAdapter(
                                    restaurantInfoList,
                                    activity as Context
                                )
                                recyclerView.adapter = homeAdapter
                                recyclerView.layoutManager = layoutManager
                            }
                        }
                        progressDialog.visibility = View.GONE
                    },
                    Response.ErrorListener
                    {
                        progressDialog.visibility = View.GONE
                        Toast.makeText(
                            activity as Context,
                            "Some Error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "13714ab03e5a4d"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    activity as Context,
                    "Some Unexpected error occurred!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            val alterDialog: AlertDialog.Builder =
                AlertDialog.Builder(activity as Context, R.style.AlertDialogStyle)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be established!")
            alterDialog.setPositiveButton("Open Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit")
            { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {

            R.id.sort -> {
                radioButtonView = View.inflate(
                    contextParam,
                    R.layout.sort_radio_button,
                    null
                )     //radiobutton view for sorting display
                val alterDialog: AlertDialog.Builder =
                    AlertDialog.Builder(activity, R.style.AlertDialogStyle)
                alterDialog.setTitle("Sort By?")
                alterDialog.setView(radioButtonView)
                alterDialog.setPositiveButton("OK")
                { _, _ ->
                    if (radioButtonView.radioHighToLow.isChecked) {
                        Collections.sort(restaurantInfoList, costComparator)
                        homeAdapter.notifyDataSetChanged()
                    }
                    if (radioButtonView.radioLowToHigh.isChecked) {
                        Collections.sort(restaurantInfoList, costComparator)
                        restaurantInfoList.reverse()
                        homeAdapter.notifyDataSetChanged()
                    }
                    if (radioButtonView.radioRatingHighToLow.isChecked) {
                        Collections.sort(restaurantInfoList, ratingComparator)
                        restaurantInfoList.reverse()
                        homeAdapter.notifyDataSetChanged()
                    }
                    if (radioButtonView.radioRatingLowToHigh.isChecked) {
                        Collections.sort(restaurantInfoList, ratingComparator)
                        homeAdapter.notifyDataSetChanged()
                    }
                }
                alterDialog.setNegativeButton("CANCEL")
                { _, _ ->
                    //do nothing
                }
                alterDialog.create()
                alterDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            if (restaurantInfoList.isEmpty())
                fetchData()
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