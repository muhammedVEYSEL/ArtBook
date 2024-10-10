package com.veys.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.veys.artbookkotlin.databinding.ActivityMainBinding
import com.veys.artbookkotlin.databinding.RecyclerowBinding

class RecycleAdapter(val artList:ArrayList<Art>):RecyclerView.Adapter<RecycleAdapter.ArtHolder>() {

    class ArtHolder (val binding: RecyclerowBinding):RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.artText.text = artList.get(position).name
        holder.itemView.setOnClickListener(){
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",artList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return artList.size
    }
}