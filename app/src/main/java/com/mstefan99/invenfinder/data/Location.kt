package com.mstefan99.invenfinder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Location(
	var cabinet: Int,
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

		return "${cabinet + 1}-${letterCol}${row + 1}"
	}


	companion object {
		fun parseLocation(string: String): Location? {
			val cabinet = Regex("^\\d+").find(string)?.value
			val letterCol = Regex("[A-Z]+").find(string)?.value
			val row = Regex("\\d+$").find(string)?.value
			var numberCol = ""

			if (cabinet == null || letterCol == null || row == null) {
				return null
			}

			for (c in letterCol) {
				if (c.code in 65..74) {
					numberCol += (c.code - 17).toChar()
				} else if (c.code in 75..90) {
					numberCol += (c.code + 22).toChar()
				}
			}

			return Location(cabinet.toInt() - 1, numberCol.toInt(26), row.toInt() - 1)
		}
	}
}
