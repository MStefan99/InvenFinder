package com.example.invenfinder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class Item(
	var id: Int,
	override var name: String,
	override var description: String?,
	override var link: String?,
	override var location: String,
	override var amount: Int
) : NewItem(name, description, link, location, amount)


@Parcelize
open class NewItem(
	open var name: String,
	open var description: String?,
	open var link: String?,
	open var location: String,
	open var amount: Int
) : Parcelable
