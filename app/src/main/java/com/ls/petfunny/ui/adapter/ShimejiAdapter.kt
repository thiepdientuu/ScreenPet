package com.ls.petfunny.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ls.petfunny.R
import com.ls.petfunny.data.model.ShimejiGif
import com.ls.petfunny.databinding.ItemShimejiBinding

class ShimejiAdapter(private val onItemClick: (ShimejiGif) -> Unit) :
    ListAdapter<ShimejiGif, ShimejiAdapter.ShimejiViewHolder>(ShimejiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimejiViewHolder {
        val binding = ItemShimejiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShimejiViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ShimejiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ShimejiViewHolder(private val binding: ItemShimejiBinding, private val onItemClick: (ShimejiGif) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentItem: ShimejiGif? = null

        init {
            binding.root.setOnClickListener {
                currentItem?.let(onItemClick)
            }
        }

        fun bind(item: ShimejiGif) {
            binding.tvName.text = item.name ?: item.nick ?: "Unknown"
            currentItem = item
            // Sử dụng Glide để load ảnh/gif
            Glide.with(itemView.context)
                .load(item.thumb ?: item.shimejiGif)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.imgCharacter)
        }
    }
}

// DiffUtil giúp RecyclerView biết chính xác item nào thay đổi
class ShimejiDiffCallback : DiffUtil.ItemCallback<ShimejiGif>() {
    override fun areItemsTheSame(oldItem: ShimejiGif, newItem: ShimejiGif): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ShimejiGif, newItem: ShimejiGif): Boolean = oldItem == newItem
}