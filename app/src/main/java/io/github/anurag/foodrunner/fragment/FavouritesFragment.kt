package com.anurag.foodrunner.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.anurag.foodrunner.R
import com.anurag.foodrunner.adapter.AllRestaurantsAdapter
import com.anurag.foodrunner.database.RestaurantDatabase
import com.anurag.foodrunner.database.RestaurantEntity
import com.anurag.foodrunner.model.Restaurants

class FavouritesFragment : Fragment() {

    private lateinit var recyclerRestaurant: RecyclerView
    private lateinit var allRestaurantsAdapter: AllRestaurantsAdapter
    private var restaurantList = arrayListOf<Restaurants>()
    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlFav: RelativeLayout
    private lateinit var rlNoFav: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)
        rlFav = view.findViewById(R.id.rlFavorites)
        rlNoFav = view.findViewById(R.id.rlNoFavorites)
        rlLoading = view.findViewById(R.id.rlLoading)
        rlLoading.visibility = View.VISIBLE
        setUpRecycler(view)
        return view
    }

    private fun setUpRecycler(view: View) {
        recyclerRestaurant = view.findViewById(R.id.recyclerRestaurants)

        val backgroundList = FavouritesAsync(activity as Context).execute().get()
        if (backgroundList.isEmpty()) {
            rlLoading.visibility = View.GONE
            rlFav.visibility = View.GONE
            rlNoFav.visibility = View.VISIBLE
        } else {
            rlFav.visibility = View.VISIBLE
            rlLoading.visibility = View.GONE
            rlNoFav.visibility = View.GONE
            for (i in backgroundList) {
                restaurantList.add(
                    Restaurants(
                        i.id,
                        i.name,
                        i.rating,
                        i.costForTwo.toInt(),
                        i.imageUrl
                    )
                )
            }

            allRestaurantsAdapter = AllRestaurantsAdapter(restaurantList, activity as Context)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerRestaurant.layoutManager = mLayoutManager
            recyclerRestaurant.itemAnimator = DefaultItemAnimator()
            recyclerRestaurant.adapter = allRestaurantsAdapter
            recyclerRestaurant.setHasFixedSize(true)
        }

    }

    class FavouritesAsync(context: Context) : AsyncTask<Void, Void, List<RestaurantEntity>>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {

            return db.restaurantDao().getAllRestaurants()
        }

    }

}
