package com.example.invenfinder.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Component(
	var name: String,
	var description: String?,
	var drawer: Int,
	var col: Int,
	var row: Int,
	var amount: Int
): Parcelable
