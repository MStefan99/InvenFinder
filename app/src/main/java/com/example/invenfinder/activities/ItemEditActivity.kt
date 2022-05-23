package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.invenfinder.R
import com.example.invenfinder.data.ItemBase
import com.example.invenfinder.data.Location
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ItemEditActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_item_edit)

		val vName: EditText = findViewById(R.id.name_input)
		val vDescription: EditText = findViewById(R.id.description_input)
		val vLocation: EditText = findViewById(R.id.location_input)
		val vAmount: EditText = findViewById(R.id.amount_input)
		val vLabel: TextView = findViewById(R.id.error_label)
		val vAdd: Button = findViewById(R.id.add_button)

		vAdd.setOnClickListener {
			if (vName.text.isEmpty()) {
				vLabel.setText(R.string.name_cannot_be_empty)
				return@setOnClickListener
			}

			val location = Location.parseLocation(vLocation.text.toString().uppercase())
			if (location == null) {
				vLabel.setText(R.string.location_is_invalid)
				return@setOnClickListener
			}

			MainScope().launch {
				val res = ItemManager.addItemAsync(
					ItemBase(
						vName.text.toString(),
						vDescription.text.toString(),
						location,
						if (vAmount.text.isNotEmpty()) vAmount.text.toString().toInt() else 0
					)
				)

				// TODO: check if successful
			}

			finish()
		}
	}
}
