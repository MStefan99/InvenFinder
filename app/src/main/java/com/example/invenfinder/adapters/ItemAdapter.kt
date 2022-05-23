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
import com.example.invenfinder.activities.ItemActivity
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.Location


class ItemAdapter(private val activity: Activity) :
	RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

	private var items = ArrayList<Item>()
	private val filtered = ArrayList(items)


	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val vLayout: ConstraintLayout = view.findViewById(R.id.component_layout)

		val vName: TextView = view.findViewById(R.id.component_name)
		val vDescription: TextView = view.findViewById(R.id.component_description)
		val vAmount: TextView = view.findViewById(R.id.component_amount)
		val vLocation: TextView = view.findViewById(R.id.component_location)
	}


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater
			.from(parent.context)
			.inflate(R.layout.element_item, parent, false)

		return ViewHolder(view)
	}


	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.vLayout.setOnClickListener {
			val intent = Intent(activity, ItemActivity::class.java)
			intent.putExtra("item", filtered[position])

			activity.startActivity(intent)
		}

		holder.vName.text = filtered[position].name
		holder.vDescription.text = filtered[position].description
		holder.vAmount.text = filtered[position].amount.toString()
		holder.vLocation.text = filtered[position].location.toString()
	}


	override fun getItemCount() = filtered.size


	// TODO: optimize
	fun filter(query: String?) {
		filtered.clear()

		if (query == null || query.isEmpty()) {
			filtered.addAll(items)
		} else {
			val q = query.trim().lowercase()
			val l = Location.parseLocation(query.trim().uppercase())

			for (c in items) {
				if (c.name.lowercase().contains(q)
					|| c.description!!.lowercase().contains(q)
					|| c.location == l) {
					filtered.add(c)
				}
			}
		}

		notifyDataSetChanged()
	}


	fun setComponents(c: ArrayList<Item>) {
		items = c
	}
}
