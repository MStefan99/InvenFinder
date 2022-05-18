package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toolbar
import com.example.invenfinder.R
import com.example.invenfinder.data.Component


class ComponentActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_component)

		val component = intent.getParcelableExtra<Component>("component")

		val vName = findViewById<TextView>(R.id.component_name)
		val vDescription = findViewById<TextView>(R.id.component_description)
		val vLocation = findViewById<TextView>(R.id.component_location)
		val vAmount = findViewById<TextView>(R.id.component_amount)
		val vToolbar = findViewById<Toolbar>(R.id.toolbar)

		if (component != null) {
			vName.text = component.name
			vDescription.text = component.description
			vLocation.text = component.location.toString()
			vAmount.text = component.amount.toString()
			vToolbar.title = "Details: ${component.name}"
		}
	}
}
