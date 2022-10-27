package com.mstefan99.invenfinder.backup

import com.mstefan99.invenfinder.data.Item
import com.mstefan99.invenfinder.utils.ItemManager
import com.mstefan99.invenfinder.backup.Item as DBItem

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

fun DBItem.toItem(): Item {
	return Item(
		this.id,
		this.name,
		this.description,
		this.link,
		this.location,
		this.amount
	)
}

fun Item.equalsDBItem(other: DBItem): Boolean {
	return this.name == other.name
}

class BackupManager(private val db: BackupDatabase) {
	suspend fun autoBackup(items: List<Item>) {
		val lastBackup = db.backupDao().getLast()

		if (lastBackup == null || hasNewItems(lastBackup.id, items)) {
			backup(items)
		}

		cleanup()
	}

	suspend fun backup(items: List<Item>) {
		val backupID = db.backupDao().add(
			Backup(0, System.currentTimeMillis() / 1000)
		)

		for (item in items) {
			db.itemDao().add(item.toDBItem(backupID))
		}
	}

	suspend fun restore(backupID: Int, itemManager: ItemManager): List<Item> {
		itemManager.getAllAsync().await().forEach { itemManager.deleteAsync(it).await() }
		val items = db.itemDao().getFromBackup(backupID)
		items.forEach { itemManager.addAsync(it.toItem()).await() }
		return items.map { it.toItem() }
	}

	suspend fun cleanup() {
		db.backupDao().deleteOlderThan(
			(System.currentTimeMillis() / 1000) - 60 * 60 * 24 * 5
		)
	}

	suspend fun delete() {
		db.backupDao().deleteAll()
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

	suspend fun hasNewItems(backupID: Int, items: List<Item>): Boolean {
		val backupItems = db.itemDao().getFromBackup(backupID)

		return items.any { i -> !backupItems.any { bi -> i.equalsDBItem(bi) } }
	}
}
