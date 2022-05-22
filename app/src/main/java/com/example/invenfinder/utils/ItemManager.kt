package com.example.invenfinder.utils

import com.example.invenfinder.data.Component
import com.example.invenfinder.data.Location
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


object ItemManager {
	data class ConnectionOptions(
		var url: String,
		var username: String,
		var password: String
	)

	private val connection = CompletableDeferred<Connection>()
	private const val protocol: String = "jdbc:mariadb://"
	private const val port: String = "3306"
	private const val dbName: String = "invenfinder"


	suspend fun openConnectionAsync(options: ConnectionOptions): Deferred<Connection> {
		return withContext(Dispatchers.IO) {
			async {
				try {
					val c = DriverManager
						.getConnection(
							"$protocol${options.url}:$port/$dbName",
							options.username,
							options.password
						)

					if (connection.isCompleted) {
						connection.await().close()
					}

					connection.complete(c)
					return@async c
				} catch (e: SQLException) {
					return@async connection.await()
				}
			}
		}
	}


	// Only use returned connection if there's no suitable function
	fun getConnectionAsync(): Deferred<Connection> {
		return connection
	}


	suspend fun getComponentsAsync(): Deferred<ArrayList<Component>?> {
		return withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.createStatement()
					val res = st.executeQuery("select * from components where amount > 0")

					val components = ArrayList<Component>()

					while (res.next()) {
						components.add(
							Component(
								res.getInt("id"),
								res.getString("name"),
								res.getString("description"),
								Location(
									res.getInt("drawer"),
									res.getInt("col"),
									res.getInt("row")
								),
								res.getInt("amount")
							)
						)
					}

					return@async components
				} catch (e: SQLException) {
					return@async null
				}
			}
		}
	}


	suspend fun updateAmountAsync(component: Component) {
		return withContext(Dispatchers.IO) {
			launch {
				val st = connection
					.await()
					.prepareStatement("update components set amount = ? where id = ?")

				st.setInt(1, component.amount)
				st.setInt(2, component.id)

				st.executeUpdate()
			}
		}
	}


	suspend fun testConnectionAsync(options: ConnectionOptions): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					DriverManager
						.getConnection(
							"$protocol${options.url}:$port/$dbName",
							options.username,
							options.password
						)
						.close()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}
}
