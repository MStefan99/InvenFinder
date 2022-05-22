package com.example.invenfinder.activities

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toolbar
import com.example.invenfinder.R
import com.example.invenfinder.data.Component
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ItemActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_item)

		val component = intent.getParcelableExtra<Component>("component")

		val vName: TextView = findViewById(R.id.component_name)
		val vDescription: TextView = findViewById(R.id.component_description)
		val vLocation: TextView = findViewById(R.id.component_location)
		val vAmount: TextView = findViewById(R.id.component_amount)
		val vToolbar: Toolbar = findViewById(R.id.toolbar)
		val vTakeStock: ImageView = findViewById(R.id.take_image)
		val vPutStock: ImageView = findViewById(R.id.put_image)

		if (component != null) {
			vName.text = component.name
			vDescription.text = component.description
			vLocation.text = component.location.toString()
			vAmount.text = component.amount.toString()

			vTakeStock.setOnClickListener {
				val layout = layoutInflater.inflate(R.layout.dialog_stock, null)
				val vPicker: NumberPicker = layout.findViewById(R.id.picker)

				vPicker.minValue = 1
				vPicker.maxValue = component.amount

				AlertDialog
					.Builder(this)
					.setView(layout)
					.setPositiveButton(R.string.take_from_storage) { _, _ ->
						component.amount -= vPicker.value
						vAmount.text = component.amount.toString()

						MainScope().launch {
							ItemManager.updateAmountAsync(component)
						}

						if (component.amount <= 0) {
							finish()
						}
					}
					.setNegativeButton(R.string.cancel, null)
					.show()
			}

			vPutStock.setOnClickListener {
				val layout = layoutInflater.inflate(R.layout.dialog_stock, null)
				val vPicker: NumberPicker = layout.findViewById(R.id.picker)

				vPicker.minValue = 1
				vPicker.maxValue = 1000

				AlertDialog
					.Builder(this)
					.setView(layout)
					.setPositiveButton(R.string.put_in_storage) { _, _, ->
						component.amount += vPicker.value
						vAmount.text = component.amount.toString()

						MainScope().launch {
							ItemManager.updateAmountAsync(component)
						}
					}
					.setNegativeButton(R.string.cancel, null)
					.show()
			}
		}
	}
}
