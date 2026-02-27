package com.ls.petfunny.ui.home

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ls.petfunny.data.model.Mascots
import com.ls.petfunny.databinding.ItemMascotsBinding
import com.ls.petfunny.utils.AppLogger
import java.io.ByteArrayInputStream

class ActiveMascotAdapter(private val onItemClick: (Mascots) -> Unit) :
    ListAdapter<Mascots, ActiveMascotAdapter.MascotsViewHolder>(MascotsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotsViewHolder {
        val binding = ItemMascotsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MascotsViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MascotsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MascotsViewHolder(private val binding: ItemMascotsBinding, private val onItemClick: (Mascots) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentItem: Mascots? = null

        init {
            binding.ivDelete.setOnClickListener {
                currentItem?.let(onItemClick)
            }
        }

        fun bind(item: Mascots) {
            //binding.ivIcon.text = item.name ?: "Unknown"
            AppLogger.d("HIHI ---> ActiveMascotAdapter ---> bind: " + item.name)
            currentItem = item
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(item.bitmap))
            binding.ivMascots.setImageBitmap(bitmap)
        }
    }
}

class MascotsDiffCallback : DiffUtil.ItemCallback<Mascots>() {
    override fun areItemsTheSame(oldItem: Mascots, newItem: Mascots): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Mascots, newItem: Mascots): Boolean = oldItem == newItem
}