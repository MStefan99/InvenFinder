package com.example.invenfinder.utils

import android.util.Log
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

const val apiPrefix = "api"

class WebConnector : ConnectorInterface() {
	private val client = OkHttpClient.Builder()
		.connectTimeout(10, TimeUnit.SECONDS)
		.build()

	override suspend fun testConnectionAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val url = Preferences.getPreferences().getString("url", null)
					?: throw Exception("Server address not set")

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

	override suspend fun testAuthAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")
				val key = prefs.getString("key", null)
					?: return@async false

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/auth")
						.header("API-Key", key)
						.build()
				).execute()

				return@async res.code == 200
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
					.put("username", username)
					.put("password", password)

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/login")
						.post(payload.toString().toRequestBody())
						.build()
				).execute()

				val result = JSONObject(res.body!!.string())
				if (res.code == 201) {
					Preferences.getPreferences().edit()
						.putString("url", url)
						.putString("username", username)
						.putString("key", result.getString("key"))
						.apply()
				}
				return@async res.code == 201
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/logout")
						.header("API-Key", prefs.getString("key", null) ?: throw Exception("Not logged in"))
						.build()
				).execute()

				if (res.code == 200) {
					Preferences.getPreferences().edit()
						.remove("key")
						.apply()
				}
				return@async res.code == 200
			}
		}

	override suspend fun addAsync(item: NewItem): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")

				val payload = JSONObject()
					.put("name", item.name)
					.put("description", item.description)
					.put("link", item.link)
					.put("location", item.location)
					.put("amount", item.amount)

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items")
						.header("API-Key", prefs.getString("key", null) ?: throw Exception("Not signed in"))
						.post(payload.toString().toRequestBody())
						.build()
				).execute()

				if (res.code == 201) {
					val result = JSONObject(res.body!!.string())
					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						if (result.isNull("description")) null else result.getString("description"),
						if (result.isNull("link")) null else result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Exception(error.getString("message"))
				}
			}
		}

	override suspend fun getAllAsync(): Deferred<ArrayList<Item>> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items")
						.header("API-Key", prefs.getString("key", null) ?: throw Exception("Not signed in"))
						.build()
				).execute()

				if (res.code == 200) {
					val data = JSONArray(res.body!!.string())
					val items = ArrayList<Item>()

					for (i in 0 until data.length()) {
						val result = data.getJSONObject(i)

						items.add(
							Item(
								result.getInt("id"),
								result.getString("name"),
								if (result.isNull("description")) null else result.getString("description"),
								if (result.isNull("link")) null else result.getString("link"),
								result.getString("location"),
								result.getInt("amount")
							)
						)
					}
					return@async items
				} else {
					val error = JSONObject(res.body!!.string())
					throw Exception(error.getString("message"))
				}
			}
		}

	override suspend fun getByIDAsync(id: Int): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items/$id")
						.header("API-Key", prefs.getString("key", null) ?: throw Exception("Not signed in"))
						.build()
				).execute()

				if (res.code == 200) {
					val result = JSONObject(res.body!!.string())

					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						if (result.isNull("description")) null else result.getString("description"),
						if (result.isNull("link")) null else result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Exception(error.getString("message"))
				}
			}
		}

	override suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")

				val payload = JSONObject()
					.put("amount", amount)

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items/$id/amount")
						.header(
							"API-Key", prefs.getString("key", null)
								?: throw Exception("Not logged in")
						)
						.put(payload.toString().toRequestBody())
						.build()
				).execute()

				if (res.code == 200) {
					val result = JSONObject(res.body!!.string())

					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						if (result.isNull("description")) null else result.getString("description"),
						if (result.isNull("link")) null else result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Exception(error.getString("message"))
				}
			}
		}

	override suspend fun editAsync(item: Item): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")

				val payload = JSONObject()
					.put("name", item.name)
					.put("description", item.description ?: JSONObject.NULL)
					.put("link", item.link ?: JSONObject.NULL)
					.put("location", item.location)
					.put("amount", item.amount)
				Log.i("Payload", payload.toString());

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items/${item.id}")
						.header(
							"API-Key", prefs.getString("key", null)
								?: throw Exception("Not logged in")
						)
						.patch(payload.toString().toRequestBody())
						.build()
				).execute()

				if (res.code == 200) {
					val result = JSONObject(res.body!!.string())

					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						if (result.isNull("description")) null else result.getString("description"),
						if (result.isNull("link")) null else result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Exception(error.getString("message"))
				}
			}
		}

	override suspend fun deleteAsync(item: Item): Deferred<Item> =
		withContext(Dispatchers.IO) {
			async {
				val prefs = Preferences.getPreferences()
				val url = prefs.getString("url", null)
					?: throw Exception("Server address not set")

				val res = client.newCall(
					Request.Builder()
						.url("$url/$apiPrefix/items/${item.id}")
						.header(
							"API-Key", prefs.getString("key", null)
								?: throw Exception("Not logged in")
						)
						.delete()
						.build()
				).execute()

				if (res.code == 200) {
					val result = JSONObject(res.body!!.string())

					return@async Item(
						result.getInt("id"),
						result.getString("name"),
						if (result.isNull("description")) null else result.getString("description"),
						if (result.isNull("link")) null else result.getString("link"),
						result.getString("location"),
						result.getInt("amount")
					)
				} else {
					val error = JSONObject(res.body!!.string())
					throw Exception(error.getString("message"))
				}
			}
		}
}
