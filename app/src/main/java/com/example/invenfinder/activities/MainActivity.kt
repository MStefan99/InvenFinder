package com.example.invenfinder.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.invenfinder.components.TitleBar
import com.example.invenfinder.data.Item
import com.example.invenfinder.utils.ItemManager
import com.example.invenfinder.utils.Preferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
	private var items by mutableStateOf(mutableListOf<Item>())
	var filteredItems by mutableStateOf(mutableListOf<Item>())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		Preferences.setPreferences(prefs)

		setContent {
			var searchQuery by remember { mutableStateOf("") }
			Column {
				TitleBar("Inventory")
				SearchField(searchQuery, onQueryChange = { q -> searchQuery = q; filteredItems = filter(items, q) })
				ItemList(filteredItems)
			}
		}
	}

	override fun onResume() {
		super.onResume()
		MainScope().launch {
			items = loadItems()
			filteredItems = items
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

@Composable
fun SearchField(query: String = "", onQueryChange: (String) -> Unit = {}) {
	TextField(value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth())
}

fun filter(items: MutableList<Item>, query: String?): MutableList<Item> {
	val filtered = ArrayList<Item>()

	if (query == null || query.isEmpty()) {
		filtered.addAll(items)
	} else {
		val q = query.trim().lowercase()
		val l = query.trim().lowercase()

		for (c in items) {
			if (c.name.lowercase().contains(q)
				|| c.description!!.lowercase().contains(q)
				|| c.location == l.lowercase()
			) {
				filtered.add(c)
			}
		}
	}

	return filtered
}
