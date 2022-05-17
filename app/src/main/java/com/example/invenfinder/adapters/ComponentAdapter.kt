package com.example.invenfinder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.invenfinder.R
import com.example.invenfinder.data.Component


class ComponentAdapter(
	private val _context: Context,
	private val _layout: Int,
	private val _components: Array<Component>
) :
	ArrayAdapter<Component>(_context, _layout, _components) {

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		return if (convertView != null) {
			convertView
		} else {
			val layout = LayoutInflater.from(_context).inflate(_layout, parent, false);
			layout.findViewById<TextView>(R.id.component_name).text = _components[position].name
			layout.findViewById<TextView>(R.id.component_description).text =
				_components[position].description
			layout
		}
	}
}
