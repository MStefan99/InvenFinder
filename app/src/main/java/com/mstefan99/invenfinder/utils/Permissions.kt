package com.mstefan99.invenfinder.utils

object Permissions {
	enum class PERMISSIONS {
		EDIT_ITEM_AMOUNT,
		MANAGE_ITEMS,
		MANAGE_USERS
	}

	fun fromNumber(value: Int): List<PERMISSIONS> {
		val permissions = ArrayList<PERMISSIONS>()
		var i: Int = 0

		while (value != 0) {
			if (value and 1 == 1) {
				permissions.add(PERMISSIONS.values()[i]);
			}

			++i;
			value shr 1;
		}

		return permissions;
	}

	fun toNumber(permissions: List<PERMISSIONS>): Int {
		var value: Int = 0

		for (p in permissions) {
			value = value or (1 shl p.ordinal)
		}

		return value
	}

	fun hasPermissions(
		requestedPermissions: List<PERMISSIONS>,
		grantedPermissions: List<PERMISSIONS>
	): Boolean {
		val requestedValue = toNumber(requestedPermissions)
		val grantedValue = toNumber(grantedPermissions)

		return (grantedValue and requestedValue) == requestedValue
	}

	fun hasPermissions(
		requestedPermissions: Int,
		grantedPermissions: Int
	): Boolean {
		return (grantedPermissions and requestedPermissions) == requestedPermissions
	}
}