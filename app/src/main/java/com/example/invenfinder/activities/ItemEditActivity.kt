package com.example.invenfinder.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.invenfinder.R
import com.example.invenfinder.components.TitleBar
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import com.example.invenfinder.utils.AppColors
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.abs


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
	val item by rememberSaveable { mutableStateOf(i) }
	val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
		backgroundColor = AppColors.auto.background,
		textColor = AppColors.auto.foreground,
		unfocusedBorderColor = AppColors.auto.light,
		focusedBorderColor = AppColors.auto.muted,
		placeholderColor = AppColors.auto.light
	)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.verticalScroll(rememberScrollState())
			.padding(bottom = 16.dp)
	) {
		Text(stringResource(R.string.name), color = AppColors.auto.foreground)
		OutlinedTextField(
			item.name,
			placeholder = { Text(stringResource(R.string.name)) },
			onValueChange = { name -> item.name = name; onItemUpdate(item) },
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.description), color = AppColors.auto.foreground)
		OutlinedTextField(
			item.description ?: "",
			placeholder = { Text(stringResource(R.string.description)) },
			onValueChange = { desc -> item.description = desc.ifEmpty { null }; onItemUpdate(item) },
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.link), color = AppColors.auto.foreground)
		OutlinedTextField(
			item.link ?: "",
			placeholder = { Text(stringResource(R.string.link)) },
			onValueChange = { link -> item.link = link.ifEmpty { null }; onItemUpdate(item) },
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.location), color = AppColors.auto.foreground)
		OutlinedTextField(
			item.location,
			placeholder = { Text(stringResource(R.string.location)) },
			onValueChange = { location -> item.location = location; onItemUpdate(item) },
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.amount), color = AppColors.auto.foreground)
		OutlinedTextField(
			item.amount.toString(),
			placeholder = { Text(stringResource(R.string.amount)) },
			onValueChange = { amount ->
				if (amount.isEmpty()) {
					item.amount = 0
				}
				amount.toIntOrNull()?.let { item.amount = abs(it) }
			},
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 8.dp)
		)

		Row {
			Spacer(modifier = Modifier.weight(1f))
			Button(
				onClick = onItemSave,
				colors = ButtonDefaults.buttonColors(
					backgroundColor = AppColors.auto.accent
				),
				modifier = Modifier.padding(top = 8.dp)
			) {
				Text(stringResource(R.string.save), color = AppColors.auto.onAccent)
			}
		}
	}
}
