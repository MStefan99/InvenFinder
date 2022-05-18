package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.example.invenfinder.R
import com.example.invenfinder.data.Component


class ComponentActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_component)

		val component = intent.getParcelableExtra<Component>("component");

		val name = findViewById<TextView>(R.id.component_name)
		val description = findViewById<TextView>(R.id.component_description)

		name.text = component?.name
		description.text = component?.description
	}
}
