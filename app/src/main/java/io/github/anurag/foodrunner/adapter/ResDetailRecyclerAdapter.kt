package com.anurag.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anurag.foodrunner.R
import com.anurag.foodrunner.model.RestaurantsDetails

class ResDetailRecyclerAdapter(
    val context: Context,
    val itemList: ArrayList<RestaurantsDetails>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ResDetailRecyclerAdapter.RestaurantsDetailsViewHolder>() {

    companion object {
        var isCartEmpty = true
    }


    class RestaurantsDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtCount: TextView = view.findViewById(R.id.txtResID)
        val txtDishName: TextView = view.findViewById(R.id.txtDishName)
        val txtPrice: TextView = view.findViewById(R.id.txtDishPrice)
        val imgAddToCart: ImageView = view.findViewById(R.id.imgAddToCart)
        val imgRemoveFromCart: ImageView = view.findViewById(R.id.imgRemoveFromCart)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RestaurantsDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_res_details_single_row, parent, false)
        return RestaurantsDetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    interface OnItemClickListener {
        fun onAddItemClick(dishObject: RestaurantsDetails)
        fun onRemoveItemClick(dishObject: RestaurantsDetails)
    }


    override fun onBindViewHolder(holder: RestaurantsDetailsViewHolder, position: Int) {
        val restaurantsDetails = itemList[position]
        holder.txtDishName.text = restaurantsDetails.dishName
        val price = "\u20B9${restaurantsDetails.dishPrice}"
        holder.txtPrice.text = price
        holder.txtCount.text = (position + 1).toString()

        holder.imgAddToCart.setOnClickListener {
            holder.imgRemoveFromCart.visibility = View.VISIBLE
            holder.imgAddToCart.visibility = View.GONE
            listener.onAddItemClick(restaurantsDetails)
        }

        holder.imgRemoveFromCart.setOnClickListener {
            holder.imgRemoveFromCart.visibility = View.GONE
            holder.imgAddToCart.visibility = View.VISIBLE
            listener.onRemoveItemClick(restaurantsDetails)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}

