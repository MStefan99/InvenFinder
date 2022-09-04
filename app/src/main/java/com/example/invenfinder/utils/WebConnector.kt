package com.example.invenfinder.utils

import com.example.invenfinder.data.Item
import com.example.invenfinder.data.NewItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class WebConnector(private var url: String) : ConnectorInterface() {
	override suspend fun testConnectionAsync(): Deferred<Boolean> {
		TODO("Not yet implemented")
	}

	override suspend fun loginAsync(username: String, password: String): Deferred<Boolean> {
		return CompletableDeferred(false)
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
