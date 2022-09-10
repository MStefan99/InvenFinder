package com.example.invenfinder.utils

import android.content.SharedPreferences

object Preferences {
	private lateinit var preferences: SharedPreferences

	fun setPreferences(preferences: SharedPreferences) {
		this.preferences = preferences
	}

	fun getPreferences(): SharedPreferences {
		if (!this::preferences.isInitialized) {
			throw Error("Preferences not initialized")
		}

		return this.preferences
	}
}