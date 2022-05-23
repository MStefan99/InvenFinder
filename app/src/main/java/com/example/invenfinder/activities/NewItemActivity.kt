package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.invenfinder.R
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.ItemBase
import com.example.invenfinder.data.Location
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class NewItemActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_new_item)

		val vName: EditText = findViewById(R.id.name_input)
		val vDescription: EditText = findViewById(R.id.description_input)
		val vLocation: EditText = findViewById(R.id.location_input)
		val vAmount: EditText = findViewById(R.id.amount_input)
		val vAdd: Button = findViewById(R.id.add_button)

		vAdd.setOnClickListener {
			val location = Location.parseLocation(vLocation.text.toString())

			if (location == null) {
				Toast.makeText(this, "Invalid location!", Toast.LENGTH_LONG).show()
				return@setOnClickListener
			}

			MainScope().launch {
				@Suppress("DeferredResultUnused")
				ItemManager.addItemAsync(
					ItemBase(
						vName.text.toString(),
						vDescription.text.toString(),
						location,
						vAmount.text.toString().toInt()
					)
				)
			}

			finish()
		}
	}
}
