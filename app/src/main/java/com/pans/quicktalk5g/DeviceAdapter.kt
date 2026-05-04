package com.pans.quicktalk5g

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class DiscoveredDevice(val name: String, val host: String, val port: Int)

class DeviceAdapter(val items: MutableList<DiscoveredDevice>, private val onClick: (DiscoveredDevice) -> Unit) : RecyclerView.Adapter<DeviceAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.deviceName)
        val addr: TextView = v.findViewById(R.id.deviceAddr)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = items[position]
        holder.name.text = d.name
        holder.addr.text = "${d.host}:${d.port}"
        holder.itemView.setOnClickListener { onClick(d) }
    }

    override fun getItemCount(): Int = items.size

    fun addOrUpdate(d: DiscoveredDevice) {
        val idx = items.indexOfFirst { it.host == d.host && it.port == d.port }
        if (idx >= 0) {
            items[idx] = d
            notifyItemChanged(idx)
        } else {
            items.add(d)
            notifyItemInserted(items.size - 1)
        }
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    fun removeByHostPort(host: String, port: Int) {
        val idx = items.indexOfFirst { it.host == host && it.port == port }
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun isEmpty(): Boolean = items.isEmpty()

    fun removeByName(name: String) {
        // remove all items whose name equals given name
        val toRemove = items.withIndex().filter { it.value.name == name }.map { it.index }
        // remove from highest index to lowest to avoid shifting issues
        toRemove.sortedDescending().forEach { idx ->
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
