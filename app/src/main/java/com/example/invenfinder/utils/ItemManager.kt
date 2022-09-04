package com.example.invenfinder.utils

import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.*


object ItemManager {
	private var connector: CompletableDeferred<ConnectorInterface> = CompletableDeferred()

	suspend fun loginAsync(url: String, username: String, password: String): Deferred<Boolean> =
		withContext(Dispatchers.IO) {
			async {
				val webConnector = WebConnector(url)
				if (webConnector.loginAsync(username, password).await()) {
					connector.complete(webConnector)
					return@async true
				}

				val dbConnector = DatabaseConnector(url)
				if (dbConnector.loginAsync(username, password).await()) {
					connector.complete(dbConnector)
					return@async true
				}

				return@async false
			}
		}

	suspend fun logoutAsync(): Deferred<Boolean> =
		connector.await().logoutAsync()

	suspend fun addAsync(item: NewItem): Deferred<Item> =
		connector.await().addAsync(item)

	suspend fun getAllAsync(): Deferred<ArrayList<Item>> =
		connector.await().getAllAsync()

	suspend fun getByIDAsync(id: Int): Deferred<Item> =
		connector.await().getByIDAsync(id)

	suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item> =
		connector.await().editAmountAsync(id, amount)

	suspend fun editAsync(item: Item): Deferred<Item> =
		connector.await().editAsync(item)

	suspend fun deleteAsync(item: Item): Deferred<Item> =
		connector.await().deleteAsync(item)
}
