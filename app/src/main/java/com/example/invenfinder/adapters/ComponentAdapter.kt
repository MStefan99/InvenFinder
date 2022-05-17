package com.example.invenfinder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.invenfinder.R
import com.example.invenfinder.data.Component


class ComponentAdapter(private val components: ArrayList<Component>) :
	RecyclerView.Adapter<ComponentAdapter.ViewHolder>() {

	private val filtered = ArrayList(components)


	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val name: TextView = view.findViewById(R.id.component_name);
		val description: TextView = view.findViewById(R.id.component_description);
	}


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater
			.from(parent.context)
			.inflate(R.layout.component, parent, false)

		return ViewHolder(view)
	}


	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.name.text = filtered[position].name
		holder.description.text = filtered[position].description
	}


	override fun getItemCount() = filtered.size


	fun filter(query: String) {
		filtered.clear()

		if (query.isEmpty()) {
			filtered.addAll(components)
			return
		}

		for (c in components) {
			if (c.name.lowercase().contains(query) || c.description!!.lowercase().contains(query)) {
				filtered.add(c);
			}
		}

		notifyDataSetChanged()
	}
}
