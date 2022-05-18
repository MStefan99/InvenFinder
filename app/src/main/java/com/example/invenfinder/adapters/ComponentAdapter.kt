package com.example.invenfinder.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.invenfinder.R
import com.example.invenfinder.activities.ComponentActivity
import com.example.invenfinder.data.Component


class ComponentAdapter(private val activity: Activity) :
	RecyclerView.Adapter<ComponentAdapter.ViewHolder>() {

	private var components = ArrayList<Component>()
	private val filtered = ArrayList(components)


	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val vLayout: ConstraintLayout = view.findViewById(R.id.component_layout)

		val vName: TextView = view.findViewById(R.id.component_name)
		val vDescription: TextView = view.findViewById(R.id.component_description)
		val vAmount: TextView = view.findViewById(R.id.component_amount)
	}


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater
			.from(parent.context)
			.inflate(R.layout.component, parent, false)

		return ViewHolder(view)
	}


	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.vLayout.setOnClickListener {
			val intent = Intent(activity, ComponentActivity::class.java)
			intent.putExtra("component", filtered[position])

			activity.startActivity(intent)
		}

		holder.vName.text = filtered[position].name
		holder.vDescription.text = filtered[position].description
		holder.vAmount.text = filtered[position].amount.toString()
	}


	override fun getItemCount() = filtered.size


	// TODO: optimize
	fun filter(query: String?) {
		filtered.clear()

		if (query == null || query.isEmpty()) {
			filtered.addAll(components)
		} else {
			val q = query.trim()

			for (c in components) {
				if (c.name.lowercase().contains(q) || c.description!!.lowercase().contains(q)) {
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
