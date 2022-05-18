package com.example.invenfinder.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Location(
	var drawer: Int,
	var col: Int,
	var row: Int
) : Parcelable {
	override fun toString(): String {
		return "${drawer + 1}-${col}${row + 1}"
	}


	companion object {
		fun parseLocation(string: String): Location {
			return Location(0, 0, 0)
		}
	}
}


@Parcelize
data class Component(
	var id: Int,
	var name: String,
	var description: String?,
	var location: Location,
	var amount: Int
) : Parcelable {
	fun isLocatedAt(l: Location?): Boolean {
		return location == l;
	}
}
