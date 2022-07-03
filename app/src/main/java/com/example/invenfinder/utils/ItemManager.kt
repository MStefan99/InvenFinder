package com.example.invenfinder.utils

import android.util.Log
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
							"insert into items(name, description, link, location, amount) " +
									"values(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
						)
					st.setString(1, item.name)
					st.setString(2, item.description)
					st.setString(3, item.link)
					st.setString(4, item.location)
					st.setInt(5, item.amount)
					st.executeUpdate()

					val generatedKeys = st.generatedKeys
					if (generatedKeys.next()) {
						val id: Int = generatedKeys.getInt(1)

						return@async Item(
							id,
							item.name,
							item.description,
							item.link,
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
						.prepareStatement("select * from items")
					val res = st.executeQuery()

					val items = ArrayList<Item>()

					while (res.next()) {
						items.add(
							Item(
								res.getInt("id"),
								res.getString("name"),
								res.getString("description"),
								res.getString("link"),
								res.getString("location"),
								res.getInt("amount")
							)
						)
					}

					return@async items
				} catch (e: SQLException) {
					e.message?.let { Log.e("SQL error", it) };
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
						.prepareStatement("select * from items where id = ?")
					st.setInt(1, id)
					val res = st.executeQuery()

					if (res.next()) {
						return@async Item(
							res.getInt("id"),
							res.getString("name"),
							res.getString("description"),
							res.getString("link"),
							res.getString("location"),
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
						.prepareStatement("update items set name = ?, description = ?, link = ?, " +
								"location = ?, amount = ? where id = ?")
					st.setString(1, item.name)
					st.setString(2, item.description)
					st.setString(3, item.link)
					st.setString(4, item.location)
					st.setInt(5, item.amount)
					st.setInt(6, item.id)
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
						.prepareStatement("update items set amount = ? where id = ?")
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
						.prepareStatement("delete from items where id = ?")
					st.setInt(1, item.id)
					st.executeUpdate()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}
}
