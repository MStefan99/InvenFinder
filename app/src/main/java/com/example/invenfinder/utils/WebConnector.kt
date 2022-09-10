package com.example.invenfinder.utils

import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

const val apiPrefix = "api"

class WebConnector : ConnectorInterface() {
	private val client = OkHttpClient()

	override suspend fun testConnectionAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val url = Preferences.getPreferences().getString("url", null)
					?: throw Error("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix")
						.build()
				).execute()

				return@async res.header("who-am-i") == "Invenfinder"
			}
		}

	override suspend fun testConnectionAsync(url: String): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix")
						.build()
				).execute()

				return@async res.header("who-am-i") == "Invenfinder"
			}
		}

	override suspend fun loginAsync(
		url: String,
		username: String,
		password: String
	): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val payload = JSONObject()
				payload.put("username", username)
				payload.put("password", password)

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/login")
						.post(payload.toString().toRequestBody())
						.build()
				).execute()

				val result = JSONObject(res.body!!.string())
				if (res.code == 201) {

					val editor = Preferences.getPreferences().edit()
					editor.putString("url", url)
					editor.putString("key", result.getString("key"))
					editor.apply()
				}
				return@async res.code == 201
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/logout")
						.header("API-Key", prefs.getString("key", null) ?: throw Error("Not logged in"))
						.build()
				).execute()

				if (res.code == 200) {
					Preferences.getPreferences().edit().remove("key").apply()
				}
				return@async res.code == 200
			}
		}

	override suspend fun addAsync(item: NewItem): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val payload = JSONObject()
				payload.put("name", item.name)
				payload.put("description", item.description)
				payload.put("link", item.link)
				payload.put("location", item.location)
				payload.put("amount", item.amount)

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items")
						.header("API-Key", prefs.getString("key", null) ?: throw Error("Not signed in"))
						.post(payload.toString().toRequestBody())
						.build()
				).execute()

				if (res.code == 201) {
					val result = JSONObject(res.body!!.string())
					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						result.getString("description"),
						result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Error(error.getString("message"))
				}
			}
		}

	override suspend fun getAllAsync(): Deferred<ArrayList<Item>> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items")
						.header("API-Key", prefs.getString("key", null) ?: throw Error("Not signed in"))
						.build()
				).execute()

				if (res.code == 200) {
					val data = JSONArray(res.body!!.string())
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
				} else {
					val error = JSONObject(res.body!!.string())
					throw Error(error.getString("message"))
				}
			}
		}

	override suspend fun getByIDAsync(id: Int): Deferred<Item> {
		TODO("Not yet implemented")
	}

	override suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val payload = JSONObject()
				payload.put("amount", amount)

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items/$id/amount")
						.header(
							"API-Key", prefs.getString("key", null)
								?: throw Error("Not logged in")
						)
						.put(payload.toString().toRequestBody())
						.build()
				).execute()

				if (res.code == 200) {
					val result = JSONObject(res.body!!.string())
					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						result.getString("description"),
						result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Error(error.getString("message"))
				}
			}
		}

	override suspend fun editAsync(item: Item): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val payload = JSONObject()
				payload.put("name", item.name)
				payload.put("description", item.description)
				payload.put("link", item.link)
				payload.put("location", item.location)
				payload.put("amount", item.amount)

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items/${item.id}")
						.header(
							"API-Key", prefs.getString("key", null)
								?: throw Error("Not logged in")
						)
						.patch(payload.toString().toRequestBody())
						.build()
				).execute()

				if (res.code == 200) {
					val result = JSONObject(res.body!!.string())
					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						result.getString("description"),
						result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Error(error.getString("message"))
				}
			}
		}

	override suspend fun deleteAsync(item: Item): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Error("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items/${item.id}")
						.header(
							"API-Key", prefs.getString("key", null)
								?: throw Error("Not logged in")
						)
						.delete()
						.build()
				).execute()

				if (res.code == 200) {
					val result = JSONObject(res.body!!.string())
					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						result.getString("description"),
						result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Error(error.getString("message"))
				}
			}
		}
}
