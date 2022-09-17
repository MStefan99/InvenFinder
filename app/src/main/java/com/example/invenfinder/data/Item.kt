package com.example.invenfinder.data

import androidx.compose.runtime.*

class Item(
	val id: Int,
	name: String,
	description: String?,
	link: String?,
	location: String,
	amount: Int
): NewItem(name, description, link, location, amount)

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
