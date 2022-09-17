package com.example.invenfinder.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.invenfinder.R
import com.example.invenfinder.data.Item
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ItemActivity : Activity() {
	private lateinit var vName: TextView
	private lateinit var vDescription: TextView
	private lateinit var vLink: TextView
	private lateinit var vLocation: TextView
	private lateinit var vAmount: TextView
	private lateinit var vTake: ImageView
	private lateinit var vPut: ImageView
	private lateinit var vEdit: ImageView
	private lateinit var vRemove: ImageView
	private lateinit var vRefresh: SwipeRefreshLayout

	private lateinit var item: Item


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_item)

		vRefresh = findViewById(R.id.refresh_layout)
		vName = findViewById(R.id.component_name)
		vDescription = findViewById(R.id.component_description)
		vLink = findViewById(R.id.component_link)
		vLocation = findViewById(R.id.component_location)
		vAmount = findViewById(R.id.component_amount)
		vTake = findViewById(R.id.take_button)
		vPut = findViewById(R.id.put_button)
		vEdit = findViewById(R.id.edit_button)
		vRemove = findViewById(R.id.remove_button)

		vRefresh.setOnRefreshListener {
			loadItem(item.id)
		}

		vEdit.setOnClickListener {
			startActivityForResult(Intent(this, ItemEditActivity::class.java).apply {
				putExtra("itemID", item.id)
			}, 0)
		}

		vRemove.setOnClickListener {
			AlertDialog
				.Builder(this)
				.setTitle(R.string.remove_item)
				.setMessage(R.string.remove_confirm)
				.setPositiveButton(R.string.remove) { _, _ ->
					MainScope().launch {
						try {
							@Suppress("DeferredResultUnused")
							ItemManager.deleteAsync(item)
							finish()
						} catch (e: Exception) {
							Toast.makeText(this@ItemActivity, e.message, Toast.LENGTH_LONG).show()
						}
					}
				}
				.setNegativeButton(R.string.cancel, null)
				.show()
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
						try {
							@Suppress("DeferredResultUnused")
							ItemManager.editAmountAsync(item.id, item.amount)
						} catch (e: Exception) {
							Toast.makeText(this@ItemActivity, e.message, Toast.LENGTH_LONG).show()
						}
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
						try {
							@Suppress("DeferredResultUnused")
							ItemManager.editAmountAsync(item.id, item.amount)
						} catch (e: Exception) {
							Toast.makeText(this@ItemActivity, e.message, Toast.LENGTH_LONG).show()
						}
					}
				}
				.setNegativeButton(R.string.cancel, null)
				.show()
		}
	}

	override fun onResume() {
		super.onResume()
		loadItem(intent.getIntExtra("itemID", 0))
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		val newItem: Item = data?.getParcelableExtra("item") ?: return
		setItem(newItem)
	}

	private fun loadItem(id: Int) {
		MainScope().launch {
			vRefresh.isRefreshing = true
			try {
				setItem(ItemManager.getByIDAsync(id).await())
			} catch (e: Exception) {
				Toast.makeText(this@ItemActivity, e.message, Toast.LENGTH_LONG).show()
			}
			vRefresh.isRefreshing = false
		}
	}

	private fun setItem(newItem: Item) {
		item = newItem

		vName.text = item.name
		vDescription.text = item.description ?: "No description"
		vLink.text = item.link ?: "No link"
		vLocation.text = item.location
		vAmount.text = item.amount.toString()
	}
}
