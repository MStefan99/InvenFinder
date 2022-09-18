package com.example.invenfinder.activities

//import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import kotlin.math.abs


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
		var deleteDialogVisible by remember { mutableStateOf(false) }

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
						deleteDialogVisible = true
					}
			)
		}

		if (deleteDialogVisible) {
			AlertDialog(onDismissRequest = { deleteDialogVisible = false },
				title = {
					Text(
						stringResource(R.string.remove_item),
						fontSize = 20.sp,
						fontWeight = FontWeight(500)
					)
				},
				text = { Text(stringResource(R.string.remove_confirm)) },
				buttons = {
					Row(modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
						Spacer(modifier = Modifier.weight(1f))
						OutlinedButton(
							onClick = { deleteDialogVisible = false },
							modifier = Modifier.padding(end = 8.dp)
						) {
							Text(stringResource(R.string.cancel))
						}
						OutlinedButton(onClick = {
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
						}) {
							Text(stringResource(R.string.remove))
						}
					}
				})
		}
	}

	@Composable
	private fun ItemDetails(item: Item) {
		Column(
			modifier = Modifier
				.padding(16.dp, 0.dp, 16.dp, 16.dp)
				.verticalScroll(rememberScrollState())
		) {
			ItemHeader(item)
			ItemLocation(item)
			ItemInfo(item)
			ItemButtons(item)
		}
	}

	@Composable
	private fun ItemHeader(item: Item) {
		Column {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(bottom = 16.dp)
			) {
				Text(item.name, fontSize = 32.sp, modifier = Modifier.weight(1f))
				Spacer(modifier = Modifier.weight(1f))
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
		}
	}

	@Composable
	private fun ItemLocation(item: Item) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(bottom = 16.dp)
		) {
			Image(
				painterResource(R.drawable.shelf),
				stringResource(R.string.amount),
				modifier = Modifier
					.padding(end = 16.dp)
					.heightIn(max = 24.dp)
			)
			Spacer(modifier = Modifier.weight(1f))
			Text(
				item.location,
				fontSize = 16.sp,
				fontWeight = FontWeight.Bold,
			)
		}
	}

	@Composable
	private fun ItemInfo(item: Item) {
		Column {
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

	private enum class Action {
		Take,
		Put
	}

	@Composable
	private fun ItemButtons(item: Item) {
		var amountDialogAction by remember { mutableStateOf<Action?>(null) }
		var changeAmount by remember { mutableStateOf(1) }

		fun setDialog(action: Action?) {
			amountDialogAction = action
			changeAmount = 1
		}

		Row(modifier = Modifier.padding(top = 32.dp)) {
			Spacer(modifier = Modifier.weight(1f))
			Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
				setDialog(Action.Take)
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
				setDialog(Action.Put)
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

		if (amountDialogAction != null) {
			val text = if (amountDialogAction == Action.Take)
				stringResource(R.string.take_from_storage)
			else stringResource(R.string.put_in_storage)

			AlertDialog(onDismissRequest = { setDialog(null) },
				title = {
					Text(
						text,
						fontSize = 20.sp,
						fontWeight = FontWeight(500),
						modifier = Modifier.padding(bottom = 16.dp)
					)
				}, text = {
					TextField(
						value = changeAmount.toString(),
						onValueChange = { a ->
							if (a.isEmpty()) {
								changeAmount = 0
							}
							a.toIntOrNull()?.let { changeAmount = abs(it) }
						},
						modifier = Modifier.padding(top = 16.dp)
					)
				}, buttons = {
					Row(modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
						Spacer(modifier = Modifier.weight(1f))
						OutlinedButton(
							onClick = { setDialog(null) },
							modifier = Modifier.padding(end = 8.dp)
						) {
							Text(stringResource(R.string.cancel))
						}
						OutlinedButton(onClick = {
							if (amountDialogAction == Action.Take) item.amount -= changeAmount
							else item.amount += changeAmount
							setDialog(null)

							MainScope().launch {
								try {
									@Suppress("DeferredResultUnused")
									ItemManager.editAmountAsync(item.id, item.amount)
								} catch (e: Exception) {
									Toast.makeText(this@ItemActivity, e.message, Toast.LENGTH_LONG).show()
								}
							}
						}) {
							Text(text)
						}
					}
				})
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
