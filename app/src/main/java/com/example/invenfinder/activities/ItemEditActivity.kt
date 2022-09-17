package com.example.invenfinder.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.invenfinder.R
import com.example.invenfinder.components.TitleBar
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ItemEditActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val itemID: Int? = if (intent.hasExtra("itemID")) intent.getIntExtra("itemID", 0) else null

		if (itemID == null) {
			var item by mutableStateOf(NewItem("", null, null, "", 1))

			setContent {
				Column {
					TitleBar(stringResource(R.string.add_item))
					ItemEditor(item, modifier = Modifier.padding(horizontal = 16.dp), onItemUpdate = {
						item = it
					}, onItemSave = {
						MainScope().launch {
							try {
								@Suppress("DeferredResultUnused")
								val newItem = ItemManager.addAsync(item).await()
								startActivity(Intent(this@ItemEditActivity, ItemActivity::class.java).apply {
									flags = Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
									putExtra("itemID", newItem.id)
								})
							} catch (e: Exception) {
								Toast.makeText(this@ItemEditActivity, e.message, Toast.LENGTH_LONG).show()
							}
						}
						finish()
					})
				}
			}
		} else {
			var item by mutableStateOf<Item?>(null)

			MainScope().launch {
				try {
					item = ItemManager.getByIDAsync(itemID).await()
				} catch (e: Exception) {
					Toast.makeText(this@ItemEditActivity, e.message, Toast.LENGTH_LONG).show()
				}
			}

			setContent {
				item?.let {
					Column {
						TitleBar(stringResource(R.string.edit_item))
						ItemEditor(it, modifier = Modifier.padding(horizontal = 16.dp), onItemUpdate = {
							it.name = it.name
							it.description = it.description
							it.link = it.link
							it.location = it.location
							it.amount = it.amount
						}, onItemSave = {
							MainScope().launch {
								try {
									@Suppress("DeferredResultUnused")
									ItemManager.editAsync(it)
								} catch (e: Exception) {
									Toast.makeText(this@ItemEditActivity, e.message, Toast.LENGTH_LONG).show()
								}
							}

							setResult(0, Intent().apply {
								putExtra("itemID", it.id)
							})

							finish()
						})
					}
				}
			}
		}
	}
}

@Composable
private fun ItemEditor(
	i: NewItem,
	onItemUpdate: (NewItem) -> Unit,
	onItemSave: () -> Unit,
	modifier: Modifier = Modifier
) {
	val item by remember { mutableStateOf(i) }

	Column(modifier = modifier.fillMaxWidth()) {
		Text(stringResource(R.string.name))
		TextField(
			item.name,
			onValueChange = { name -> item.name = name; onItemUpdate(item) },
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.description))
		TextField(
			item.description ?: "",
			onValueChange = { desc -> item.description = desc.ifEmpty { null }; onItemUpdate(item) },
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.link))
		TextField(
			item.link ?: "",
			onValueChange = { link -> item.link = link.ifEmpty { null }; onItemUpdate(item) },
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.location))
		TextField(
			item.location,
			onValueChange = { location -> item.location = location; onItemUpdate(item) },
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.amount))
		TextField(
			item.amount.toString(),
			onValueChange = { amount ->
				item.amount = if (amount.isNotEmpty()) amount.toInt() else 0; onItemUpdate(item)
			},
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 8.dp)
		)

		Row {
			Spacer(modifier = Modifier.weight(1f))
			Button(onClick = onItemSave, modifier = Modifier.padding(top = 8.dp)) {
				Text(stringResource(R.string.save))
			}
		}
	}
}
