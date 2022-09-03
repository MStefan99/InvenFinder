package com.example.invenfinder.utils

import android.util.Log
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

private const val protocol: String = "jdbc:mariadb://"
private const val port: String = "3306"
private const val dbName: String = "invenfinder"

class DatabaseConnector(private var url: String) : ConnectorInterface() {
	private var connection: CompletableDeferred<Connection?> = CompletableDeferred(null);

	override suspend fun testConnectionAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					DriverManager
						.getConnection(
							"$protocol$url:$port/$dbName",
						)
						.close()

					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}

	override suspend fun loginAsync(username: String, password: String): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				try {
					DriverManager.setLoginTimeout(10)

					val c = DriverManager
						.getConnection(
							"$protocol$url:$port/$dbName",
							username,
							password
						)

					if (connection.isCompleted) {
						connection.await()?.close()
					}

					connection.complete(c)
					return@async true
				} catch (e: SQLException) {
					return@async false
				}
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> {
		connection = CompletableDeferred(null)
		return CompletableDeferred(true)
	}

	override suspend fun addAsync(item: NewItem): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val conn = connection.await() ?: throw Error("Not connected");

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
				val conn = connection.await() ?: throw Error("Not connected");

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
				val conn = connection.await() ?: throw Error("Not connected");

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
				val conn = connection.await() ?: throw Error("Not connected");

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
				val conn = connection.await() ?: throw Error("Not connected");

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
				val conn = connection.await() ?: throw Error("Not connected");
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
