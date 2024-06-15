package com.example.topsis_dss

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class TourismListAdapter(private val tourismList: ArrayList<Tourism>): RecyclerView.Adapter<TourismListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View, private val tourismList: ArrayList<Tourism>) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tourism_title)
        val description: TextView = itemView.findViewById(R.id.tourism_description)
        val rating: TextView = itemView.findViewById(R.id.tourism_rating)

        init {
            itemView.setOnClickListener {
                val position: Int = adapterPosition
                val intent = Intent(itemView.context, TourismDetail::class.java)
                intent.putExtra("tourism", tourismList[position])
                itemView.context.startActivity(intent)

            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_tourism, parent, false)
        return ViewHolder(view, tourismList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tourism = tourismList[position]
        holder.title.text = tourism.name
        holder.description.text = tourism.description
        holder.rating.text = tourism.rating_avarage.toString()

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTourismList(newTourismList: List<Tourism>) {
        tourismList.clear()
        tourismList.addAll(newTourismList)
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return tourismList.size
    }
}