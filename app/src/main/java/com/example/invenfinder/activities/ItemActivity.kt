package com.example.invenfinder.activities

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import com.example.invenfinder.R
import com.example.invenfinder.data.Item
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ItemActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_item)

		val vName: TextView = findViewById(R.id.component_name)
		val vDescription: TextView = findViewById(R.id.component_description)
		val vLocation: TextView = findViewById(R.id.component_location)
		val vAmount: TextView = findViewById(R.id.component_amount)
		val vTakeStock: ImageView = findViewById(R.id.take_image)
		val vPutStock: ImageView = findViewById(R.id.put_image)
		val vRemove: ImageView = findViewById(R.id.remove_button)

		val item = intent.getParcelableExtra<Item>("item")

		if (item == null) {
			finish()
			return
		}

		vName.text = item.name
		vDescription.text = item.description
		vLocation.text = item.location.toString()
		vAmount.text = item.amount.toString()

		vRemove.setOnClickListener {
			MainScope().launch {
				@Suppress("DeferredResultUnused")
				ItemManager.removeItemAsync(item)
				finish()
			}
		}

		vTakeStock.setOnClickListener {
			val layout = layoutInflater.inflate(R.layout.dialog_stock, null)
			val vPicker: NumberPicker = layout.findViewById(R.id.picker)

			vPicker.minValue = 1
			vPicker.maxValue = item.amount

			AlertDialog
				.Builder(this)
				.setView(layout)
				.setPositiveButton(R.string.take_from_storage) { _, _ ->
					item.amount -= vPicker.value
					vAmount.text = item.amount.toString()

					MainScope().launch {
						@Suppress("DeferredResultUnused")
						ItemManager.updateItemAmountAsync(item)
					}

					if (item.amount <= 0) {
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
				.setPositiveButton(R.string.put_in_storage) { _, _ ->
					item.amount += vPicker.value
					vAmount.text = item.amount.toString()

					MainScope().launch {
						@Suppress("DeferredResultUnused")
						ItemManager.updateItemAmountAsync(item)
					}
				}
				.setNegativeButton(R.string.cancel, null)
				.show()
		}
	}
}
