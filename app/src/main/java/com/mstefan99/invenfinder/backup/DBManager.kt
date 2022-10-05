package com.mstefan99.invenfinder.backup

import android.util.Log
import com.mstefan99.invenfinder.data.Item
import com.mstefan99.invenfinder.backup.Item as DBItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun Item.toDBItem(id: Long): DBItem {
	return DBItem(
		id.toInt(),
		this.id,
		this.name,
		this.description,
		this.link,
		this.location,
		this.amount
	)
}

fun Item.equalsDBItem(other: DBItem): Boolean {
	return this.id == other.id &&
			this.description == other.description &&
			this.link == other.link &&
			this.location == other.location &&
			this.amount == other.amount
}

class BackupManager(private val db: BackupDatabase) {
	fun autoBackup(items: List<Item>) {
		MainScope().launch {
			val lastBackup = db.backupDao().getLast();

			if (lastBackup == null || hasNewItems(lastBackup.id, items)) {
				backup(items)
			}
		}
	}

	fun backup(items: List<Item>) {
		MainScope().launch {
			val backupID = db.backupDao().add(
				Backup(0, System.currentTimeMillis() / 1000)
			)

			for (item in items) {
				db.itemDao().add(item.toDBItem(backupID))
			}
		}
	}

	fun cleanup() {
		MainScope().launch {
			db.backupDao().deleteOlderThan(
				(System.currentTimeMillis() / 1000) - 60 * 60 * 24 * 5
			)
		}
	}

	private suspend fun hasNewItems(backupID: Int, items: List<Item>): Boolean {
		val backupItems = db.itemDao().getFromBackup(backupID)

		return items.any { i -> !backupItems.any { bi -> i.equalsDBItem(bi) } }
	}

	suspend fun missingItems(backupID: Int, items: List<Item>): Int {
		val backupItems = db.itemDao().getFromBackup(backupID)
		var count = 0

		for (backupItem in backupItems) {
			if (!items.any { it.equalsDBItem(backupItem) }) {
				++count
			}
		}
		return count
	}
}
