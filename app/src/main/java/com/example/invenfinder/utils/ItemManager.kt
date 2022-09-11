package com.example.invenfinder.utils

import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.*


object ItemManager : ConnectorInterface() {
	private var connector: CompletableDeferred<ConnectorInterface> = CompletableDeferred()

	init {
		val prefs = Preferences.getPreferences()
		val url = prefs.getString("url", null)
		connector.complete(if (url?.contains("http") == true) WebConnector() else DatabaseConnector())
	}

	override suspend fun testConnectionAsync() =
		connector.await().testConnectionAsync()

	override suspend fun testConnectionAsync(url: String) =
		connector.await().testConnectionAsync(url)

	override suspend fun testAuthAsync(): Deferred<Boolean> =
		connector.await().testAuthAsync()

	override suspend fun loginAsync(
		url: String,
		username: String,
		password: String
	): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				if (url.contains("http")) {
					val webConnector = WebConnector()
					if (webConnector.loginAsync(url, username, password).await()) {
						connector = CompletableDeferred(webConnector)
						return@async true
					}
				} else {
					val dbConnector = DatabaseConnector()
					if (dbConnector.loginAsync(url, username, password).await()) {
						connector = CompletableDeferred(dbConnector)
						return@async true
					}
				}

				return@async false
			}
		}

	override suspend fun logoutAsync(): Deferred<Boolean> =
		connector.await().logoutAsync()

	override suspend fun addAsync(item: NewItem): Deferred<Item> =
		connector.await().addAsync(item)

	override suspend fun getAllAsync(): Deferred<ArrayList<Item>> =
		connector.await().getAllAsync()

	override suspend fun getByIDAsync(id: Int): Deferred<Item> =
		connector.await().getByIDAsync(id)

	override suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item> =
		connector.await().editAmountAsync(id, amount)

	override suspend fun editAsync(item: Item): Deferred<Item> =
		connector.await().editAsync(item)

	override suspend fun deleteAsync(item: Item): Deferred<Item> =
		connector.await().deleteAsync(item)
}
