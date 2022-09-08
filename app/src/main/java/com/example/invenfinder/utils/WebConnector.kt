package com.example.invenfinder.utils

import android.util.Log
import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

val apiPrefix = "/api"

class WebConnector(private var url: String) : ConnectorInterface() {
	override suspend fun testConnectionAsync(): Deferred<Boolean> {
		return CompletableDeferred(false)
	}

	override suspend fun loginAsync(username: String, password: String): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val conn = URL("$url$apiPrefix").openConnection() as HttpURLConnection
				Log.i("BeforeConnect", "conn")
				conn.connect()

				Log.i("Connecting", "connecting")

				val inStream = BufferedInputStream(conn.inputStream)
				val s = Scanner(inStream).useDelimiter("\\A")
				val res = if (s.hasNext()) s.next() else ""

				Log.i("Response", res)
				true
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> {
		TODO("Not yet implemented")
	}

	override suspend fun addAsync(item: NewItem): Deferred<Item> {
		TODO("Not yet implemented")
	}

	override suspend fun getAllAsync(): Deferred<ArrayList<Item>> {
		TODO("Not yet implemented")
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
