package com.example.invenfinder.utils

import android.util.Log
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.*
import java.sql.*

private const val protocol: String = "jdbc:mariadb://"
private const val port: String = "3306"
private const val db: String = "invenfinder"

class DatabaseConnector : ConnectorInterface() {
	private var connection: CompletableDeferred<Connection?> = CompletableDeferred()

	init {
		connection.complete(null)
	}

	suspend fun openConnectionAsync(): Deferred<Connection> =
		withContext(Dispatchers.IO) {
			async {
				val savedConn = connection.await()

				if (savedConn != null) {
					return@async savedConn
				} else {
					val prefs = Preferences.getPreferences()
					val url = prefs.getString("url", null)
						?: throw Error("Database URL not set")
					val username = prefs.getString("username", null)
						?: throw Error("Database username not set")
					val password = prefs.getString("password", null)
						?: throw Error("Database password not set")

					try {
						val conn = DriverManager.getConnection(
							"$protocol$url:$port/$db",
							username,
							password
						)

						if (connection.isCompleted) {
							connection.await()?.close()
						}

						connection.complete(conn)
						return@async conn
					} catch (e: SQLException) {
						throw Error("Failed to open database connection: ", e.cause)
					}
				}
			}
		}

	override suspend fun testConnectionAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val url = Preferences.getPreferences().getString("url", null)
					?: throw Error("Database URL not set")

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

	override suspend fun loginAsync(url: String, username: String, password: String): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					DriverManager.setLoginTimeout(10)
					val prefs = Preferences.getPreferences()

					val c = DriverManager
						.getConnection(
							"$protocol$url:$port/$db",
							username,
							password
						)

					if (connection.isCompleted) {
						connection.await()?.close()
					}

					val prefEditor = prefs.edit()
					prefEditor.putString("url", url)
					prefEditor.putString("username", username)
					prefEditor.putString("password", password)
					prefEditor.apply()

					connection.complete(c)
					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> {
		val prefEditor = Preferences.getPreferences().edit()
		prefEditor.remove("username")
		prefEditor.remove("password")
		prefEditor.apply()

		connection = CompletableDeferred(null)
		return CompletableDeferred(true)
	}

	override suspend fun addAsync(item: NewItem): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val conn = openConnectionAsync().await()

				try {
					val st = conn
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
					val id: Int = generatedKeys.getInt(1)

					return@async Item(
						id,
						item.name,
						item.description,
						item.link,
						item.location,
						item.amount
					)
				} catch (e: SQLException) {
					throw Error("Adding an item failed: ", e.cause)
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

					return@async items
				} catch (e: SQLException) {
					e.message?.let { Log.e("SQL error", it) };
					throw Error("Failed to retrieve items: ", e.cause);
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
						throw Error("Item not found")
					}
				} catch (e: SQLException) {
					throw Error("Retrieving an item failed: ", e.cause)
				}
			}
		}

	override suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val conn = openConnectionAsync().await()

				try {
					val st = conn
						.prepareStatement("update items set amount = ? where id = ?")
					st.setInt(1, amount)
					st.setInt(2, id)
					st.executeUpdate()

					return@async getByIDAsync(id).await()
				} catch (e: SQLException) {
					throw Error("Failed to update item amount: ", e.cause)
				}
			}
		}

	override suspend fun editAsync(item: Item): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val conn = openConnectionAsync().await()

				try {
					val st = conn
						.prepareStatement(
							"update items set name = ?, description = ?, link = ?, " +
									"location = ?, amount = ? where id = ?"
						)
					st.setString(1, item.name)
					st.setString(2, item.description)
					st.setString(3, item.link)
					st.setString(4, item.location)
					st.setInt(5, item.amount)
					st.setInt(6, item.id)
					st.executeUpdate()

					return@async getByIDAsync(item.id).await()
				} catch (e: SQLException) {
					throw Error("Failed to update item: ", e.cause)
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

					return@async i
				} catch (e: SQLException) {
					throw Error("Failed to delete item: ", e.cause)
				}
			}
		}
}
