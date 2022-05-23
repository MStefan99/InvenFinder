package com.example.invenfinder.utils

import com.example.invenfinder.data.Item
import com.example.invenfinder.data.ItemBase
import com.example.invenfinder.data.Location
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement


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


	suspend fun addItemAsync(item: ItemBase): Deferred<Item?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.prepareStatement(
							"insert into components(name, description, drawer, col, row, amount) " +
									"values(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
						)
					st.setString(1, item.name)
					st.setString(2, item.description)
					st.setInt(3, item.location.drawer)
					st.setInt(4, item.location.col)
					st.setInt(5, item.location.row)
					st.setInt(6, item.amount)
					st.executeUpdate()

					val generatedKeys = st.generatedKeys
					if (generatedKeys.next()) {
						val id: Int = generatedKeys.getInt(1)

						return@async Item(
							id,
							item.name,
							item.description,
							item.location,
							item.amount
						)
					} else {
						return@async null
					}
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun getItemsAsync(): Deferred<ArrayList<Item>?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.prepareStatement("select * from components where amount > 0")
					val res = st.executeQuery()

					val items = ArrayList<Item>()

					while (res.next()) {
						items.add(
							Item(
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

					return@async items
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun getSingleItemAsync(id: Int): Deferred<Item?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.prepareStatement("select * from components where id = ?")
					st.setInt(1, id)
					val res = st.executeQuery()

					if (res.next()) {
						return@async Item(
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
					} else {
						return@async null
					}
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun updateItemAsync(item: Item): Deferred<Item?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.prepareStatement("update components set name = ?, description = ?, drawer = ?, " +
								"col = ?, row = ?, amount = ? where id = ?")
					st.setString(1, item.name)
					st.setString(2, item.description)
					st.setInt(3, item.location.drawer)
					st.setInt(4, item.location.col)
					st.setInt(5, item.location.row)
					st.setInt(6, item.amount)
					st.setInt(7, item.id)
					st.executeUpdate()

					return@async null
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun updateItemAmountAsync(item: Item): Deferred<Item?> =
		withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.prepareStatement("update components set amount = ? where id = ?")
					st.setInt(1, item.amount)
					st.setInt(2, item.id)
					st.executeUpdate()

					return@async item  // TODO: get component from db
				} catch (e: SQLException) {
					return@async null
				}
			}
		}


	suspend fun removeItemAsync(item: Item): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					val st = connection
						.await()
						.prepareStatement("delete from components where id = ?")
					st.setInt(1, item.id)
					st.executeUpdate()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}
}
