package com.example.invenfinder.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
	private lateinit var vName: TextView
	private lateinit var vDescription: TextView
	private lateinit var vLocation: TextView
	private lateinit var vAmount: TextView
	private lateinit var vTake: ImageView
	private lateinit var vPut: ImageView
	private lateinit var vEdit: ImageView
	private lateinit var vRemove: ImageView

	private lateinit var item: Item


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_item)

		vName = findViewById(R.id.component_name)
		vDescription = findViewById(R.id.component_description)
		vLocation = findViewById(R.id.component_location)
		vAmount = findViewById(R.id.component_amount)
		vTake = findViewById(R.id.take_button)
		vPut = findViewById(R.id.put_button)
		vEdit = findViewById(R.id.edit_button)
		vRemove = findViewById(R.id.remove_button)

		val receivedItem = intent.getParcelableExtra<Item>("item")

		if (receivedItem == null) {
			finish()
			return
		}
		setItem(receivedItem)

		vEdit.setOnClickListener {
			startActivityForResult(Intent(this, ItemEditActivity::class.java).apply {
				putExtra("action", ItemEditActivity.Action.EDIT)
				putExtra("item", item)
			}, 0)
		}

		vRemove.setOnClickListener {
			MainScope().launch {
				@Suppress("DeferredResultUnused")
				ItemManager.removeItemAsync(item)
				finish()
			}
		}

		vTake.setOnClickListener {
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

		vPut.setOnClickListener {
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


	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		val newItem: Item = data?.getParcelableExtra("item") ?: return
		setItem(newItem)
	}


	private fun setItem(newItem: Item) {
		item = newItem

		vName.text = item.name
		vDescription.text = item.description
		vLocation.text = item.location.toString()
		vAmount.text = item.amount.toString()
	}
}
