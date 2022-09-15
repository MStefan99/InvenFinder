package com.example.invenfinder.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.invenfinder.data.Item
import com.example.invenfinder.utils.ItemManager
import com.example.invenfinder.utils.Preferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
	private var items by mutableStateOf(listOf<Item>())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		Preferences.setPreferences(prefs)

		setContent {
			ItemList(items)
		}
	}

	override fun onResume() {
		super.onResume()
		MainScope().launch {
			items = loadItems()
		}
	}

	@Composable
	fun ItemList(items: List<Item>) {
		if (items.isEmpty()) {
			Text("No items")
		} else {
			LazyColumn {
				items(items) { item ->
					Text(item.name)
				}
			}
		}
	}

	private suspend fun loadItems(): ArrayList<Item> {
		return try {
			ItemManager.getAllAsync().await()
			//				itemAdapter.filter(vSearch.text.toString())
		} catch (e: Exception) {
			Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
			ArrayList()
		}
	}
}