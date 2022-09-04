package com.example.invenfinder.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.invenfinder.R
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ItemEditActivity : Activity() {
	enum class Action {
		ADD, EDIT
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_item_edit)

		val vToolbar: Toolbar = findViewById(R.id.toolbar)
		val vName: EditText = findViewById(R.id.name_input)
		val vDescription: EditText = findViewById(R.id.description_input)
		val vLink: EditText = findViewById(R.id.link_input)
		val vLocation: EditText = findViewById(R.id.location_input)
		val vAmount: EditText = findViewById(R.id.amount_input)
		val vError: TextView = findViewById(R.id.error_label)
		val vSubmit: Button = findViewById(R.id.submit_button)

		val action = intent.getSerializableExtra("action") as Action
		val item: Item? = intent.getParcelableExtra("item")

		if (action == Action.ADD) {
			vToolbar.setTitle(R.string.add_item)
			vSubmit.setText(R.string.add)
		} else if (action == Action.EDIT && item != null) {
			vToolbar.setTitle(R.string.edit_item)
			vSubmit.setText(R.string.save)

			vName.setText(item.name)
			vDescription.setText(item.description)
			vLink.setText(item.link)
			vLocation.setText(item.location.toString())
			vAmount.setText(item.amount.toString())
		} else {
			Log.d("ITEM", "Trying to edit item but not item was provided")
			finish()
		}

		vSubmit.setOnClickListener {
			if (vName.text.isEmpty()) {
				vError.setText(R.string.name_cannot_be_empty)
				return@setOnClickListener
			}

			val location = vLocation.text.toString().uppercase()
			if (location.isEmpty()) {
				vError.setText(R.string.location_is_invalid)
				return@setOnClickListener
			}

			if (action == Action.ADD) {
				MainScope().launch {
					// TODO: check if successful

					ItemManager.addAsync(
						NewItem(
							vName.text.toString(),
							vDescription.text.toString(),
							vLink.text.toString(),
							location,
							if (vAmount.text.isNotEmpty()) vAmount.text.toString().toInt() else 0
						)
					)
				}
				finish()
			} else {
				if (item == null) {  // Should never get here but Kotlin complains
					return@setOnClickListener
				}

				item.name = vName.text.toString()
				item.description = vDescription.text.toString()
				item.location = location
				item.amount = if (vAmount.text.isNotEmpty()) vAmount.text.toString().toInt() else 0

				MainScope().launch {
					ItemManager.editAsync(item)
				}

				setResult(0, Intent().apply {
					putExtra("item", item)
				})

				finish()
			}
		}
	}
}
