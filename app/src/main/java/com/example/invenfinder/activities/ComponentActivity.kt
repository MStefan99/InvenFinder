package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.example.invenfinder.R


class ComponentActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_component)

		val name = findViewById<TextView>(R.id.component_name)

		name.text = intent.getStringExtra("name")
	}
}
