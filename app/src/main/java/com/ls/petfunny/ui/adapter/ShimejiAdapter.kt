package com.ls.petfunny.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ls.petfunny.data.ShimejiGif
import com.ls.petfunny.databinding.ItemShimejiBinding

class ShimejiAdapter : ListAdapter<ShimejiGif, ShimejiAdapter.ShimejiViewHolder>(ShimejiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimejiViewHolder {
        val binding = ItemShimejiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShimejiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShimejiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ShimejiViewHolder(private val binding: ItemShimejiBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShimejiGif) {
            binding.tvName.text = item.name ?: item.nick ?: "Unknown"

            // Sử dụng Glide để load ảnh/gif
            Glide.with(itemView.context)
                .load(item.thumb ?: item.shimejiGif) // Ảnh chờ
                .into(binding.imgCharacter)
        }
    }
}

// DiffUtil giúp RecyclerView biết chính xác item nào thay đổi
class ShimejiDiffCallback : DiffUtil.ItemCallback<ShimejiGif>() {
    override fun areItemsTheSame(oldItem: ShimejiGif, newItem: ShimejiGif): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ShimejiGif, newItem: ShimejiGif): Boolean = oldItem == newItem
}