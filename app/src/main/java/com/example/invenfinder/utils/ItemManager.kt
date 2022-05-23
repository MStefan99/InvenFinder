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


	// TODO: return null if unable to connect?
	suspend fun openConnectionAsync(options: ConnectionOptions): Deferred<Connection> =
		withContext(Dispatchers.IO) {
			async {
				try {
					DriverManager.setLoginTimeout(10)

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


	// Only use returned connection if there's no suitable function
	fun getConnectionAsync(): Deferred<Connection> = connection


	suspend fun getComponentsAsync(): Deferred<ArrayList<Component>?> =
		withContext(Dispatchers.IO) {
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


	suspend fun getComponentAsync(): Deferred<Component?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					// TODO: get component
					return@async null
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun updateAmountAsync(component: Component): Deferred<Component?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.prepareStatement("update components set amount = ? where id = ?")

					st.setInt(1, component.amount)
					st.setInt(2, component.id)

					st.executeUpdate()

					return@async component  // TODO: get component from db
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun addComponentAsync(component: Component): Deferred<Component?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					// TODO: add component to db
					return@async null  // TODO: get component from db
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun removeComponentAsync(component: Component): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					// TODO: remove item
					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}
}
