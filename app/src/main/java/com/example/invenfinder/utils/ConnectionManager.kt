package com.example.invenfinder.utils

import android.app.Activity
import java.sql.DriverManager
import java.sql.SQLException


class ConnectionManager {
	data class Options(
		var url: String,
		var username: String,
		var password: String
	)

	companion object {
		fun testConnection(options: Options, activity: Activity, cb: (Boolean) -> Unit) {
			Thread {
				try {
					DriverManager.getConnection(
						"jdbc:mariadb://${options.url}:3306/invenfinder",
						options.username,
						options.password
					)
						.close()

					activity.runOnUiThread {
						cb(true)
					}
				} catch (e: SQLException) {
					activity.runOnUiThread {
						cb(false)
					}
				}
			}.start()
		}
	}
}