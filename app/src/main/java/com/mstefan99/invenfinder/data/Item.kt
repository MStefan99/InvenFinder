package com.mstefan99.invenfinder.data

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

class Item(
	val id: Int,
	name: String,
	description: String?,
	link: String?,
	location: String,
	amount: Int
) : NewItem(name, description, link, location, amount)

open class NewItem(
	name: String,
	description: String?,
	link: String?,
	location: String,
	amount: Int
) {
	var name by mutableStateOf(name)
	var description by mutableStateOf(description)
	var link by mutableStateOf(link)
	var location by mutableStateOf(location)
	var amount by mutableStateOf(amount)
}

val ItemSaver = Saver<MutableState<NewItem>, Bundle>(
	save = {
		return@Saver Bundle().apply {
			putString("name", it.value.name)
			putString("desc", it.value.description)
			putString("link", it.value.link)
			putString("location", it.value.location)
			putInt("amount", it.value.amount)
		}
	},
	restore = {
		return@Saver mutableStateOf(
			NewItem(
				it.getString("name", ""),
				it.getString("desc", null),
				it.getString("link", null),
				it.getString("location", null),
				it.getInt("amount")
			)
		)
	}
)
