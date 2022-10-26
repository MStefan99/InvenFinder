package com.mstefan99.invenfinder.utils

import android.util.Log
import com.mstefan99.invenfinder.data.Item
import com.mstefan99.invenfinder.data.NewItem
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

private const val protocol: String = "jdbc:mariadb://"
private const val port: String = "3306"
private const val db: String = "invenfinder"

class DatabaseConnector : ConnectorInterface() {
	private suspend fun openConnectionAsync(): Deferred<Connection> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Database URL not set")
				val username = prefs.getString("username", null)
					?: throw Exception("Database username not set")
				val password = prefs.getString("password", null)
					?: throw Exception("Database password not set")

				return@async DriverManager.getConnection(
					"$protocol$url:$port/$db",
					username,
					password
				)
			}
		}

	override suspend fun testConnectionAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val url = Preferences.getPreferences().getString("url", null)
					?: throw Exception("Database URL not set")

				try {
					DriverManager
						.getConnection(
							"$protocol$url:$port/$db",
						)
						.close()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}

	override suspend fun testConnectionAsync(url: String): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					DriverManager
						.getConnection(
							"$protocol$url:$port/$db",
						)
						.close()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}

	override suspend fun testAuthAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Database URL not set")
				val username = prefs.getString("username", null)
					?: throw Exception("Database username not set")
				val password = prefs.getString("password", null)
					?: throw Exception("Database password not set")

				try {
					DriverManager.getConnection(
						"$protocol$url:$port/$db",
						username,
						password
					)
						.close()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}

	override suspend fun loginAsync(
		url: String,
		username: String,
		password: String
	): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					DriverManager.setLoginTimeout(10)
					val prefs = Preferences.getPreferences()
					val trimmedUsername = username.trim()

					DriverManager
						.getConnection(
							"$protocol$url:$port/$db",
							trimmedUsername,
							password
						)
						.close()

					prefs.edit()
						.putString("url", url)
						.putString("username", trimmedUsername)
						.putString("password", password)
						.apply()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> {
		Preferences.getPreferences().edit()
			.remove("password")
			.apply()

		return CompletableDeferred(true)
	}

	override suspend fun addAsync(item: NewItem): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
					if (item.amount < 0) {
						throw Exception("Amount cannot be negative")
					}

				val conn = openConnectionAsync().await()
				try {
					val st = conn
						.prepareStatement(
							"insert into items(name, description, link, location, amount) " +
									"values(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
						)

					item.name = item.name.trim()
					item.description = item.description?.trim()
					item.link = item.link?.trim()
					item.location = item.location.trim()

					st.setString(1, item.name)
					st.setString(2, item.description)
					st.setString(3, item.link)
					st.setString(4, item.location)
					st.setInt(5, item.amount)
					st.executeUpdate()

					val generatedKeys = st.generatedKeys
					if (generatedKeys.next()) {
						val id: Int = generatedKeys.getInt(1)

						st.close()
						conn.close()

						return@async Item(
							id,
							item.name,
							item.description,
							item.link,
							item.location,
							item.amount
						)
					} else {
						throw Exception("Failed to get item ID")
					}
				} catch (e: SQLException) {
					throw Exception("Adding an item failed: ", e.cause)
				}
			}
		}

	override suspend fun getAllAsync(): Deferred<ArrayList<Item>> =
		withContext(Dispatchers.IO) {
			async {
				val conn = openConnectionAsync().await()

				try {
					val st = conn
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

					st.close()
					conn.close()

					return@async items
				} catch (e: SQLException) {
					e.message?.let { Log.e("SQL error", it) }
					throw Exception("Failed to retrieve items: ", e.cause)
				}
			}
		}

	override suspend fun searchAsync(query: String): Deferred<ArrayList<Item>> =
		withContext(Dispatchers.IO) {
			async {
				val conn = openConnectionAsync().await()

				try {
					val st = conn
						.prepareStatement("select * from items where match(name, description) against (?)")
					st.setString(1, query)
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

					st.close()
					conn.close()

					return@async items
				} catch (e: SQLException) {
					e.message?.let { Log.e("SQL error", it) }
					throw Exception("Failed to retrieve items: ", e.cause)
				}
			}
		}

	override suspend fun getByIDAsync(id: Int): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val conn = openConnectionAsync().await()

				try {
					val st = conn
						.prepareStatement("select * from items where id = ?")
					st.setInt(1, id)
					val res = st.executeQuery()

					st.close()
					conn.close()

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
						throw Exception("Item not found")
					}
				} catch (e: SQLException) {
					throw Exception("Retrieving an item failed: ", e.cause)
				}
			}
		}

	override suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				if (amount < 0) {
					throw Exception("Amount cannot be negative")
				}

				val conn = openConnectionAsync().await()
				try {
					val st = conn
						.prepareStatement("update items set amount = ? where id = ?")
					st.setInt(1, amount)
					st.setInt(2, id)
					st.executeUpdate()

					st.close()
					conn.close()

					return@async getByIDAsync(id).await()
				} catch (e: SQLException) {
					throw Exception("Failed to update item amount: ", e.cause)
				}
			}
		}

	override suspend fun editAsync(item: Item): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				if (item.amount < 0) {
					throw Exception("Amount cannot be negative")
				}

				val conn = openConnectionAsync().await()
				try {
					val st = conn
						.prepareStatement(
							"update items set name = ?, description = ?, link = ?, " +
									"location = ?, amount = ? where id = ?"
						)

					item.name = item.name.trim()
					item.description = item.description?.trim()
					item.link = item.link?.trim()
					item.location = item.location.trim()

					st.setString(1, item.name)
					st.setString(2, item.description)
					st.setString(3, item.link)
					st.setString(4, item.location)
					st.setInt(5, item.amount)
					st.setInt(6, item.id)
					st.executeUpdate()

					st.close()
					conn.close()

					return@async getByIDAsync(item.id).await()
				} catch (e: SQLException) {
					throw Exception("Failed to update item: ", e.cause)
				}
			}
		}

	override suspend fun deleteAsync(item: Item): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val conn = openConnectionAsync().await()
				val i = getByIDAsync(item.id).await()

				try {
					val st = conn
						.prepareStatement("delete from items where id = ?")
					st.setInt(1, item.id)
					st.executeUpdate()

					st.close()
					conn.close()

					return@async i
				} catch (e: SQLException) {
					throw Exception("Failed to delete item: ", e.cause)
				}
			}
		}
}
