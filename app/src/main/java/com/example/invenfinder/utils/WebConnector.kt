package com.example.invenfinder.utils

import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import java.io.OutputStreamWriter
import java.io.OutputStream


const val apiPrefix = "api"

fun getBody(conn: HttpURLConnection): String {
	val inStream = BufferedInputStream(conn.inputStream)
	val s = Scanner(inStream).useDelimiter("\\A")
	return if (s.hasNext()) s.next() else ""
}

fun setBody(conn: HttpURLConnection, data: String) {
	val os: OutputStream = conn.outputStream
	val osw = OutputStreamWriter(os, "UTF-8")
	osw.write(data)
	osw.flush()
	osw.close()
	os.close()
}

class WebConnector : ConnectorInterface() {
	override suspend fun testConnectionAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val url = Preferences.getPreferences().getString("url", null)
					?: throw Error("Server address not set")

				val conn = URL("$url/$apiPrefix").openConnection() as HttpURLConnection
				conn.connect()

				return@async conn.getHeaderField("who-am-i") == "Invenfinder"
			}
		}

	override suspend fun testConnectionAsync(url: String): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val conn = URL("$url/$apiPrefix").openConnection() as HttpURLConnection
				conn.connect()

				return@async conn.getHeaderField("who-am-i") == "Invenfinder"
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
					val conn = URL("$url/$apiPrefix/login").openConnection() as HttpURLConnection
					conn.requestMethod = "POST"
					conn.setRequestProperty("Content-Type", "application/json")

					val body = JSONObject()
					body.put("username", username)
					body.put("password", password)
					setBody(conn, body.toString())

					conn.connect()

					if (conn.responseCode == 201) {
						val data = JSONObject(getBody(conn))

						val editor = Preferences.getPreferences().edit()
						editor.putString("url", url)
						editor.putString("key", data.getString("key"))
						editor.apply()
					}

					return@async conn.responseCode == 201
				} catch (e: Throwable) {
					return@async false
				}
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val conn = URL("$url/$apiPrefix/logout").openConnection() as HttpURLConnection
				conn.setRequestProperty(
					"API-Key", prefs.getString("key", null) ?: throw Error("Not logged in")
				)
				conn.connect()

				if (conn.responseCode == 200) {
					Preferences.getPreferences().edit().remove("key").apply()
				}

				return@async conn.responseCode == 200
			}
		}

	override suspend fun addAsync(item: NewItem): Deferred<Item> {
		TODO("Not yet implemented")
	}

	override suspend fun getAllAsync(): Deferred<ArrayList<Item>> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val conn = URL("$url/$apiPrefix/items").openConnection() as HttpURLConnection
				conn.setRequestProperty(
					"API-Key", prefs.getString("key", null) ?: throw Error("Not signed in")
				)
				conn.connect()

				if (conn.responseCode == 200) {
					val data = JSONArray(getBody(conn))
					val items = ArrayList<Item>()

					for (i in 0 until data.length()) {
						val jsonItem = data.getJSONObject(i)

						items.add(
							Item(
								jsonItem.getInt("id"),
								jsonItem.getString("name"),
								jsonItem.getString("description"),
								jsonItem.getString("link"),
								jsonItem.getString("location"),
								jsonItem.getInt("amount")
							)
						)
					}

					return@async items
				} else throw Error("Failed to get items")
			}
		}

	override suspend fun getByIDAsync(id: Int): Deferred<Item> {
		TODO("Not yet implemented")
	}

	override suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item> {
		TODO("Not yet implemented")
	}

	override suspend fun editAsync(item: Item): Deferred<Item> {
		TODO("Not yet implemented")
	}

	override suspend fun deleteAsync(item: Item): Deferred<Item> {
		TODO("Not yet implemented")
	}
}
