package com.example.invenfinder.adapters

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.invenfinder.R
import com.example.invenfinder.activities.ComponentActivity
import com.example.invenfinder.data.Component


class ComponentAdapter(
	private val activity: Activity,
) :
	RecyclerView.Adapter<ComponentAdapter.ViewHolder>() {

	private var components = ArrayList<Component>()
	private val filtered = ArrayList(components)


	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val layout: ConstraintLayout = view.findViewById(R.id.component_layout)

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
		holder.layout.setOnClickListener {
			val intent = Intent(activity, ComponentActivity::class.java)
			intent.putExtra("component", filtered[position])

			activity.startActivity(intent)
		}

		holder.name.text = filtered[position].name
		holder.description.text = filtered[position].description
	}


	override fun getItemCount() = filtered.size


	// TODO: optimize
	fun filter(query: String?) {
		filtered.clear()

		if (query == null || query.isEmpty()) {
			filtered.addAll(components)
		} else {
			for (c in components) {
				if (c.name.lowercase().contains(query) || c.description!!.lowercase().contains(query)) {
					filtered.add(c);
				}
			}
		}

		notifyDataSetChanged()
	}


	fun setComponents(c: ArrayList<Component>) {
		components = c
	}
}
