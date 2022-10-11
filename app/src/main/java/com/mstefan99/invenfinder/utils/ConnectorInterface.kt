package com.mstefan99.invenfinder.utils

import com.mstefan99.invenfinder.data.Item
import com.mstefan99.invenfinder.data.NewItem
import kotlinx.coroutines.Deferred

abstract class ConnectorInterface {
	abstract suspend fun testConnectionAsync(): Deferred<Boolean>
	abstract suspend fun testConnectionAsync(url: String): Deferred<Boolean>
	abstract suspend fun testAuthAsync(): Deferred<Boolean>

	abstract suspend fun loginAsync(
		url: String,
		username: String,
		password: String
	): Deferred<Boolean>
	abstract suspend fun logoutAsync(): Deferred<Boolean>

	abstract suspend fun addAsync(item: NewItem): Deferred<Item>
	abstract suspend fun getAllAsync(): Deferred<ArrayList<Item>>
	abstract suspend fun searchAsync(query: String): Deferred<ArrayList<Item>>
	abstract suspend fun getByIDAsync(id: Int): Deferred<Item>
	abstract suspend fun editAmountAsync(id: Int, amount: Int): Deferred<Item>
	abstract suspend fun editAsync(item: Item): Deferred<Item>
	abstract suspend fun deleteAsync(item: Item): Deferred<Item>
}
