package com.mstefan99.invenfinder.activities

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
import com.mstefan99.invenfinder.components.TitleBar
import com.mstefan99.invenfinder.data.Item
import com.mstefan99.invenfinder.data.NewItem
import com.mstefan99.invenfinder.utils.AppColors
import com.mstefan99.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.abs


class ItemEditActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val itemID: Int? = if (intent.hasExtra("itemID")) intent.getIntExtra("itemID", 0) else null

		if (itemID == null) {
			val item by mutableStateOf(NewItem("", null, null, "", 1))

			setContent {
				Column {
					TitleBar(stringResource(R.string.add_item))
					ItemEditor(item, modifier = Modifier.padding(horizontal = 16.dp),
						onItemSave = { name, description, link, location, amount ->
							MainScope().launch {
								try {
									@Suppress("DeferredResultUnused")
									val newItem =
										ItemManager.addAsync(Item(0, name, description, link, location, amount)).await()
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
						ItemEditor(it, modifier = Modifier.padding(horizontal = 16.dp),
							onItemSave = { name, description, link, location, amount ->
								MainScope().launch {
									try {
										@Suppress("DeferredResultUnused")
										ItemManager.editAsync(Item(it.id, name, description, link, location, amount))
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
	onItemSave: (
		name: String,
		description: String?,
		link: String?, location: String, amount: Int
	) -> Unit,
	modifier: Modifier = Modifier
) {
	var name by rememberSaveable { mutableStateOf(i.name) }
	var description by rememberSaveable { mutableStateOf(i.description) }
	var link by rememberSaveable { mutableStateOf(i.link) }
	var location by rememberSaveable { mutableStateOf(i.location) }
	var amount by rememberSaveable { mutableStateOf(i.amount) }

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
		Spacer(Modifier.padding(top = 16.dp))

		Text(stringResource(R.string.name), color = AppColors.auto.foreground)
		OutlinedTextField(
			name,
			placeholder = { Text(stringResource(R.string.name)) },
			onValueChange = { n -> name = n },
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.description), color = AppColors.auto.foreground)
		OutlinedTextField(
			description ?: "",
			placeholder = { Text(stringResource(R.string.description)) },
			onValueChange = { d ->
				description = d.ifEmpty { null }
			},
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.link), color = AppColors.auto.foreground)
		OutlinedTextField(
			link ?: "",
			placeholder = { Text(stringResource(R.string.link)) },
			onValueChange = { l ->
				link = l.ifEmpty { null }
			},
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.location), color = AppColors.auto.foreground)
		OutlinedTextField(
			location,
			placeholder = { Text(stringResource(R.string.location)) },
			onValueChange = { l -> location = l },
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 16.dp)
		)

		Text(stringResource(R.string.amount), color = AppColors.auto.foreground)
		OutlinedTextField(
			amount.toString(),
			placeholder = { Text(stringResource(R.string.amount)) },
			onValueChange = { a ->
				if (a.isEmpty()) {
					amount = 0
				}
				a.toIntOrNull()?.let { amount = abs(it) }
			},
			colors = textFieldColors,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 8.dp)
		)

		Row {
			Spacer(modifier = Modifier.weight(1f))
			Button(
				onClick = {
					onItemSave(name, description, link, location, amount)
				},
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
