package com.mstefan99.invenfinder.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Store {
	var permissions by mutableStateOf<Int?>(null)

	fun hasPermissions(requestedPermissions: List<Permissions.PERMISSIONS>): Boolean {
		return Permissions.hasPermissions(Permissions.toNumber(requestedPermissions), permissions)
	}
}