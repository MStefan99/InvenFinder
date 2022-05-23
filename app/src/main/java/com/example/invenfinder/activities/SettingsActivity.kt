package com.example.invenfinder.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import com.example.invenfinder.R


class SettingsActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)

		val vConnection: FrameLayout = findViewById(R.id.connection_layout)

		vConnection.setOnClickListener {
			startActivity(Intent(this, ConnectionActivity::class.java))
		}
	}
}
