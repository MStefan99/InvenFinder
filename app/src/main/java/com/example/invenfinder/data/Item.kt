package com.example.invenfinder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Location(
	var drawer: Int,
	var col: Int,
	var row: Int
) : Parcelable {
	override fun toString(): String {
		val numberCol = col.toString(26)
		var letterCol = ""

		for (c in numberCol) {
			if (c.code in 48..57) {
				letterCol += (c.code + 17).toChar()  // converting number to letter (i.e 0 -> A)
			} else if (c.code in 97..112) {
				letterCol += (c.code - 22).toChar()  // shifting letter (i.e A -> K)
			}
		}

		return "${drawer + 1}-${letterCol}${row + 1}"
	}


	companion object {
		fun parseLocation(string: String): Location? {
			val drawer = Regex("^\\d+").find(string)?.value
			val letterCol = Regex("[A-Z]+").find(string)?.value
			val row = Regex("\\d+$").find(string)?.value
			var numberCol = ""

			if (drawer == null || letterCol == null || row == null) {
				return null
			}

			for (c in letterCol) {
				if (c.code in 65..74) {
					numberCol += (c.code - 17).toChar()
				} else if (c.code in 75..90) {
					numberCol += (c.code + 22).toChar()
				}
			}

			return Location(drawer.toInt() - 1, numberCol.toInt(26), row.toInt() - 1)
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
) : Parcelable
