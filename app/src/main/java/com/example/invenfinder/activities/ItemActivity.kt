package com.example.invenfinder.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ItemActivity : ComponentActivity() {
	var item by mutableStateOf<Item?>(null)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val itemID = intent.getIntExtra("itemID", 0)
		loadItem(itemID)

		setContent {
			Column {
				item?.let {
					Title(it)
					ItemDetails(it)
				}
			}
		}
	}

	@Composable
	private fun Title(item: Item) {
		TitleBar(stringResource(R.string.item_details)) {
			Image(
				painterResource(R.drawable.pen),
				stringResource(R.string.edit_item),
				modifier = Modifier
					.height(28.dp)
					.clickable {
						startActivity(Intent(this@ItemActivity, ItemEditActivity::class.java).apply {
							putExtra("itemID", item.id)
						})
					})
			Spacer(modifier = Modifier.padding(start = 16.dp))
			Image(
				painterResource(R.drawable.trash_bin),
				stringResource(R.string.settings),
				modifier = Modifier
					.height(28.dp)
					.clickable {
						AlertDialog
							.Builder(this@ItemActivity)
							.setTitle(R.string.remove_item)
							.setMessage(R.string.remove_confirm)
							.setPositiveButton(R.string.remove) { _, _ ->
								MainScope().launch {
									try {
										@Suppress("DeferredResultUnused")
										ItemManager.deleteAsync(item)
										finish()
									} catch (e: Exception) {
										Toast
											.makeText(this@ItemActivity, e.message, Toast.LENGTH_LONG)
											.show()
									}
								}
							}
							.setNegativeButton(R.string.cancel, null)
							.show()
					}
			)
		}
	}

	@Composable
	private fun ItemDetails(item: Item) {
		Column(modifier = Modifier.padding(horizontal = 16.dp)) {
			ItemLocation(item)
			ItemInfo(item)
			ItemButtons(item)
		}
	}

	@Composable
	private fun ItemLocation(item: Item) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Spacer(modifier = Modifier.weight(1f))
			Image(
				painterResource(R.drawable.shelf),
				stringResource(R.string.amount),
				modifier = Modifier
					.padding(start = 16.dp)
					.heightIn(max = 96.dp)
			)
			Text(
				item.location,
				fontSize = 108.sp,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(start = 32.dp)
			)
		}
	}

	@Composable
	private fun ItemInfo(item: Item) {
		Column {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(bottom = 16.dp)
			) {
				Text(item.name, fontSize = 32.sp, modifier = Modifier.weight(1f))
				Image(
					painterResource(R.drawable.warehouse),
					stringResource(R.string.amount),
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.heightIn(max = 24.dp)
				)
				Text(
					item.amount.toString(),
					fontSize = 24.sp,
				)
			}

			Text(item.description ?: "No description", modifier = Modifier.padding(bottom = 16.dp))

			item.link?.let {
				Button(onClick = {
					val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$it"))
					startActivity(browserIntent)
				}) {
					Text(it)
				}
			} ?: run {
				Text("No link")
			}
		}
	}

	@Composable
	private fun ItemButtons(item: Item) {
		Row(modifier = Modifier.padding(top = 32.dp)) {
			Spacer(modifier = Modifier.weight(1f))
			Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
				val layout = layoutInflater.inflate(R.layout.dialog_stock, null)
				val vPicker: NumberPicker = layout.findViewById(R.id.picker)
				vPicker.minValue = 1
				vPicker.maxValue = item.amount

				AlertDialog
					.Builder(this@ItemActivity)
					.setView(layout)
					.setPositiveButton(R.string.take_from_storage) { _, _ ->
						item.amount -= vPicker.value

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
			}) {
				Image(
					painterResource(R.drawable.remove_stock),
					stringResource(R.string.take_from_storage),
					modifier = Modifier
						.heightIn(max = 76.dp)
						.padding(bottom = 8.dp)
				)
				Text(stringResource(R.string.take_from_storage))
			}
			Spacer(modifier = Modifier.weight(1f))
			Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
				val layout = layoutInflater.inflate(R.layout.dialog_stock, null)
				val vPicker: NumberPicker = layout.findViewById(R.id.picker)

				vPicker.minValue = 1
				vPicker.maxValue = 1000

				AlertDialog
					.Builder(this@ItemActivity)
					.setView(layout)
					.setPositiveButton(R.string.put_in_storage) { _, _ ->
						item.amount += vPicker.value

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
			}) {
				Image(
					painterResource(R.drawable.add_stock), stringResource(R.string.put_in_storage),
					modifier = Modifier
						.heightIn(max = 76.dp)
						.padding(bottom = 8.dp)
				)
				Text(stringResource(R.string.put_in_storage))
			}
			Spacer(modifier = Modifier.weight(1f))
		}
	}

	override fun onResume() {
		super.onResume()
		loadItem(intent.getIntExtra("itemID", 0))
	}

	private fun loadItem(id: Int) {
		MainScope().launch {
			try {
				item = ItemManager.getByIDAsync(id).await()
			} catch (e: Exception) {
				Toast.makeText(this@ItemActivity, e.message, Toast.LENGTH_LONG).show()
			}
		}
	}
}
