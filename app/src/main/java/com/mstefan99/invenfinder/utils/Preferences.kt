package com.mstefan99.invenfinder.utils

import android.content.SharedPreferences

object Preferences {
	private lateinit var preferences: SharedPreferences

	fun setPreferences(preferences: SharedPreferences) {
		Preferences.preferences = preferences
	}

	fun getPreferences(): SharedPreferences {
		if (!this::preferences.isInitialized) {
			throw Exception("Preferences not initialized")
		}

		return preferences
	}
}