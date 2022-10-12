package com.mstefan99.invenfinder.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.invenfinder.R
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mstefan99.invenfinder.backup.BackupDatabase
import com.mstefan99.invenfinder.backup.BackupManager
import com.mstefan99.invenfinder.components.TitleBar
import com.mstefan99.invenfinder.data.Item
import com.mstefan99.invenfinder.utils.AppColors
import com.mstefan99.invenfinder.utils.ItemManager
import com.mstefan99.invenfinder.utils.Preferences
import com.mstefan99.invenfinder.utils.Timeout
import com.mstefan99.invenfinder.utils.Timeout.TimeoutEvent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

const val MAX_LENGTH = 40

class InventoryActivity : ComponentActivity() {
	private var items by mutableStateOf(listOf<Item>())
	private var filteredItems by mutableStateOf(listOf<Item>())
	private var loading by mutableStateOf(true)
	private var missingItemCount by mutableStateOf(0)
	private var searchQuery by mutableStateOf("")
	private var debounceHandle: TimeoutEvent? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		Preferences.setPreferences(prefs)

		setContent {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.background(AppColors.auto.background)
			) {
				Title()
				Column(
					modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)
				) {
					SearchField(
						searchQuery,
						onQueryChange = { q -> searchQuery = q; filter(items, q) })
					Inventory()
					BackupAlert()
				}
			}

			Column(modifier = Modifier.padding(bottom = 32.dp)) {
				Spacer(modifier = Modifier.weight(1f))
				Row(modifier = Modifier.padding(end = 32.dp)) {
					Spacer(modifier = Modifier.weight(1f))
					FloatingActionButton(
						backgroundColor = AppColors.auto.accent,
						contentColor = AppColors.auto.onAccent,
						onClick = {
							startActivity(
								Intent(
									this@InventoryActivity,
									ItemEditActivity::class.java
								)
							)
						}
					) {
						Text(
							"+",
							fontSize = 32.sp,
							fontWeight = FontWeight(300),
							color = AppColors.auto.onAccent
						)
					}
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()
		searchQuery = ""
		loadItems()
	}

	private fun loadItems() {
		MainScope().launch {
			try {
				loading = true
				items = ItemManager.getAllAsync().await()
				filter(items, searchQuery)
				val db =
					Room.databaseBuilder(this@InventoryActivity, BackupDatabase::class.java, "backup-db")
						.build()
				val bm = BackupManager(db)
				val backup = db.backupDao().getLast()
				if (backup == null) {
					bm.backup(items)
				} else {
					val mi = bm.missingItems(backup.id, items)

					if (mi > 5 && mi > items.size * 0.1) {
						missingItemCount = mi
					} else {
						if (bm.hasNewItems(backup.id, items)) bm.backup(items)
						bm.cleanup()
					}
				}
			} catch (e: Exception) {
				Toast.makeText(this@InventoryActivity, e.message, Toast.LENGTH_LONG).show()
			}
			loading = false
		}
	}

	@Composable
	private fun Title() {
		TitleBar("Inventory") {
			Image(
				painterResource(R.drawable.settings),
				stringResource(R.string.settings),
				modifier = Modifier
					.height(28.dp)
					.clickable {
						startActivity(Intent(this@InventoryActivity, SettingsActivity::class.java))
					}
			)
		}
	}

	@Composable
	fun Inventory() {
		SwipeRefresh(
			state = rememberSwipeRefreshState(loading),
			onRefresh = { loadItems() }
		) {
			if (items.isEmpty()) {
				Row(horizontalArrangement = Arrangement.Center) {
					Text(
						stringResource(R.string.inventory_empty),
						fontSize = 18.sp,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = 40.dp),
						color = AppColors.auto.muted
					)
				}
			} else if (filteredItems.isEmpty()) {
				Row(horizontalArrangement = Arrangement.Center) {
					Text(
						stringResource(R.string.search_empty),
						fontSize = 18.sp,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = 40.dp),
						color = AppColors.auto.muted
					)
				}
			} else {
				ItemList(filteredItems) {
					this@InventoryActivity.startActivity(
						Intent(
							this@InventoryActivity,
							ItemActivity::class.java
						).apply {
							putExtra("itemID", it.id)
						})
				}
			}
		}
	}

	@Composable
	fun BackupAlert() {
		if (missingItemCount > 0) {
			AlertDialog(onDismissRequest = {},
				backgroundColor = AppColors.auto.background,
				title = {
					Text(
						stringResource(R.string.attention),
						fontSize = 20.sp,
						fontWeight = FontWeight(500),
						color = AppColors.auto.foreground
					)
				}, text = {
					Text(
						stringResource(R.string.items_deleted, missingItemCount.toString()),
						color = AppColors.auto.foreground
					)
				}, buttons = {
					Row(modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
						Spacer(modifier = Modifier.weight(1f))
						OutlinedButton(
							onClick = { backupCurrent() },
							modifier = Modifier.padding(end = 8.dp),
							colors = ButtonDefaults.outlinedButtonColors(backgroundColor = AppColors.auto.background)
						) {
							Text(stringResource(R.string.no), color = AppColors.auto.accent)
						}
						OutlinedButton(
							onClick = { restoreBackup() },
							colors = ButtonDefaults.outlinedButtonColors(backgroundColor = AppColors.auto.background)
						) {
							Text(stringResource(R.string.yes), color = AppColors.auto.accent)
						}
					}
				})
		}
	}

	private fun filter(items: List<Item>, query: String?) {
		val filtered = ArrayList<Item>()
		Timeout.clearTimeout(debounceHandle)

		if (query == null || query.isEmpty()) {
			filtered.addAll(items)
		} else {
			val q = query.trim().lowercase()
			val l = query.trim().lowercase()

			for (c in items) {
				if (c.name.lowercase().contains(q)
					|| c.description?.lowercase()?.contains(q) == true
					|| c.location.lowercase() == l
				) {
					filtered.add(c)
				}
			}

			debounceHandle = Timeout.setTimeout(2000) {
				MainScope().launch {
					try {
						val i = ItemManager.searchAsync(q).await()
						if (filteredItems.size != i.size) {
							Toast.makeText(
								this@InventoryActivity,
								"Search results enhanced", Toast.LENGTH_LONG
							).show()
						}
						filteredItems = i
					} catch (e: Exception) {
						Toast.makeText(this@InventoryActivity, e.message, Toast.LENGTH_LONG).show()
					}
				}
			}
		}
		filteredItems = filtered
	}

	private fun backupCurrent() {
		missingItemCount = 0
		MainScope().launch {
			val db =
				Room.databaseBuilder(
					this@InventoryActivity,
					BackupDatabase::class.java,
					"backup-db"
				).build()
			val bm = BackupManager(db)
			bm.backup(items)
		}
	}

	private fun restoreBackup() {
		missingItemCount = 0
		MainScope().launch {
			val db =
				Room.databaseBuilder(
					this@InventoryActivity,
					BackupDatabase::class.java,
					"backup-db"
				).build()
			val bm = BackupManager(db)
			db.backupDao().getLast()?.let {
				items = bm.restore(it.id, ItemManager)
				filter(items, searchQuery)
			}
		}
	}
}

@Composable
private fun SearchField(query: String = "", onQueryChange: (String) -> Unit = {}) {
	OutlinedTextField(
		value = query,
		onValueChange = onQueryChange,
		leadingIcon = { Icon(Icons.Default.Search, null) },
		placeholder = { Text(stringResource(R.string.search)) },
		colors = TextFieldDefaults.outlinedTextFieldColors(
			backgroundColor = AppColors.auto.background,
			textColor = AppColors.auto.foreground,
			placeholderColor = AppColors.auto.muted,
			leadingIconColor = AppColors.auto.muted,
			unfocusedBorderColor = AppColors.auto.light,
			focusedBorderColor = AppColors.auto.muted
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp)
	)
}

@Composable
private fun ItemList(items: List<Item>, onItemClick: (Item) -> Unit) {
	LazyColumn(
		modifier = Modifier
			.padding(top = 8.dp)
			.fillMaxSize()
	) {
		items(items) { item ->
			Item(item, onItemClick)
		}
	}
}

@Composable
private fun Item(item: Item, onItemClick: (Item) -> Unit = {}) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp)
			.clickable { onItemClick(item) }
	) {
		Row {
			Text(
				truncate(item.name, MAX_LENGTH),
				fontSize = 16.sp, fontWeight = FontWeight(500),
				color = AppColors.auto.foreground,
				modifier = Modifier.padding(bottom = 8.dp)
			)
			Spacer(modifier = Modifier.weight(1f))
			Text(
				truncate(item.location, MAX_LENGTH),
				color = AppColors.auto.foreground,
				modifier = Modifier.padding(bottom = 8.dp)
			)
		}
		Row {
			Text(
				truncate(
					item.description ?: "",
					MAX_LENGTH
				).ifBlank { "No description" },
				color = AppColors.auto.muted,
			)
			Spacer(modifier = Modifier.weight(1f))
			Text(
				item.amount.toString(),
				color = AppColors.auto.muted,
			)
		}
		Divider(modifier = Modifier.padding(top = 8.dp), color = AppColors.auto.light)
	}
}

@Suppress("SameParameterValue")
private fun truncate(str: String, len: Int): String {
	return if (str.length > len) {
		str.substring(0, len - 1) + "…"
	} else {
		str
	}
}
