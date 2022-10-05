package com.mstefan99.invenfinder.data

class Item(
	val id: Int,
	override var name: String,
	override var description: String?,
	override var link: String?,
	override var location: String,
	override var amount: Int
) : NewItem(name, description, link, location, amount)

open class NewItem(
	open var name: String,
	open var description: String?,
	open var link: String?,
	open var location: String,
	open var amount: Int
)