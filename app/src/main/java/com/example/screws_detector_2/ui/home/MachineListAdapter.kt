package com.example.screws_detector_2.ui.home

import com.example.screws_detector_2.data.model.Machine
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.screws_detector_2.databinding.ItemMachineBinding


class MachineListAdapter(
    private val onClick: (Machine) -> Unit
) : ListAdapter<Machine, MachineListAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMachineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    inner class VH(private val b: ItemMachineBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(m: Machine) {
            b.tvName.text = m.name
            b.tvStatus.text = "Status: ${m.status}"
            b.tvHeartbeat.text = "Last ping: ${m.last_heartbeat}"
            b.root.setOnClickListener { onClick(m) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Machine>() {
            override fun areItemsTheSame(a: Machine, b: Machine) = a.id == b.id
            override fun areContentsTheSame(a: Machine, b: Machine) = a == b
        }
    }
}
