package com.example.invenfinder.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invenfinder.R
import com.example.invenfinder.components.TitleBar
import com.example.invenfinder.data.Item
import com.example.invenfinder.utils.ItemManager
import com.example.invenfinder.utils.Preferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

const val MAX_LENGTH = 40

class MainActivity : ComponentActivity() {
	private var items by mutableStateOf(listOf<Item>())
	var filteredItems by mutableStateOf(listOf<Item>())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		Preferences.setPreferences(prefs)

		setContent {
			var searchQuery by remember { mutableStateOf("") }

			Column {
				Title()
				Column(
					modifier = Modifier.padding(horizontal = 16.dp)
				) {
					SearchField(
						searchQuery,
						onQueryChange = { q -> searchQuery = q; filteredItems = filter(items, q) })

					if (items.isEmpty()) {
						Text(stringResource(R.string.inventory_empty))
					} else if (filteredItems.isEmpty()) {
						Text(stringResource(R.string.search_empty))
					} else {
						ItemList(filteredItems) {
							this@MainActivity.startActivity(
								Intent(
									this@MainActivity,
									ItemActivity::class.java
								).apply {
									putExtra("itemID", it.id)
								})
						}
					}
				}
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
		} catch (e: Exception) {
			Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
			ArrayList()
		}
	}

	@Composable
	private fun Title() {
		TitleBar("Inventory") {
			Image(
				painterResource(R.drawable.add_button),
				stringResource(R.string.add_item),
				modifier = Modifier
					.height(28.dp)
					.clickable {
						startActivity(Intent(this@MainActivity, ItemEditActivity::class.java))
					})
			Spacer(modifier = Modifier.padding(start = 16.dp))
			Image(
				painterResource(R.drawable.settings),
				stringResource(R.string.settings),
				modifier = Modifier
					.height(28.dp)
					.clickable {
						startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
					}
			)
		}
	}
}

@Composable
private fun SearchField(query: String = "", onQueryChange: (String) -> Unit = {}) {
	TextField(
		value = query,
		onValueChange = onQueryChange,
		leadingIcon = { Icon(Icons.Default.Search, null) },
		placeholder = { Text(stringResource(R.string.search)) },
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp)
	)
}

@Composable
private fun ItemList(items: List<Item>, onItemClick: (Item) -> Unit) {
	LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
		items(items) { item ->
			Item(item, onItemClick)
		}
	}
}

@Suppress("SameParameterValue")
@Composable
private fun Item(item: Item, onItemClick: (Item) -> Unit = {}) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onItemClick(item) }
	) {
		Column {
			Row {
				Text(
					truncate(item.name, MAX_LENGTH),
					fontSize = 16.sp, fontWeight = FontWeight(500),
					modifier = Modifier.padding(bottom = 8.dp)
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					truncate(item.location, MAX_LENGTH),
					modifier = Modifier.padding(bottom = 8.dp)
				)
			}
			Row {
				item.description?.let { Text(truncate(it, MAX_LENGTH)) }
				Spacer(modifier = Modifier.weight(1f))
				Text(item.amount.toString())
			}
			Divider(modifier = Modifier.padding(vertical = 8.dp))
		}
	}
}

private fun filter(items: List<Item>, query: String?): List<Item> {
	val filtered = ArrayList<Item>()

	if (query == null || query.isEmpty()) {
		filtered.addAll(items)
	} else {
		val q = query.trim().lowercase()
		val l = query.trim().lowercase()

		for (c in items) {
			if (c.name.lowercase().contains(q)
				|| c.description!!.lowercase().contains(q)
				|| c.location.lowercase() == l
			) {
				filtered.add(c)
			}
		}
	}

	return filtered
}

@Suppress("SameParameterValue")
private fun truncate(str: String, len: Int): String {
	return if (str.length > len) {
		str.substring(0, len - 1) + "…"
	} else {
		str
	}
}
