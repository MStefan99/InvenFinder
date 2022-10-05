package com.mstefan99.invenfinder.backup

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity
data class Backup(
	@PrimaryKey(autoGenerate = true) val id: Int,

	val time: Long,
)

@Entity(
	primaryKeys = ["backupID", "id"],
	foreignKeys = [ForeignKey(
		entity = Backup::class,
		parentColumns = arrayOf("id"),
		childColumns = arrayOf("backupID"),
		onDelete = CASCADE
	)]
)
data class Item(
	val backupID: Int,
	val id: Int,

	val name: String,
	val description: String?,
	val link: String?,
	val location: String,
	val amount: Int
)

data class BackupWithItems(
	@Embedded val backup: Backup,
	@Relation(
		parentColumn = "id",
		entityColumn = "backupID"
	)
	val items: List<Item>
)

@Dao
interface BackupDao {
	@Insert
	suspend fun add(backup: Backup): Long

	@Query("select * from backup")
	suspend fun getAll(): Array<Backup>

	@Query("select * from backup order by time desc limit 1")
	suspend fun getLast(): Backup?

	@Query("delete from backup where time < :time")
	suspend fun deleteOlderThan(time: Long)

	@Delete
	suspend fun delete(backup: Backup)
}

@Dao
interface ItemDao {
	@Insert
	suspend fun add(item: Item): Long

	@Query("select * from item")
	suspend fun getAll(): Array<Item>

	@Query("select * from item where backupID = :id")
	suspend fun getFromBackup(id: Int): Array<Item>

	@Delete
	suspend fun delete(item: Item)
}

@Database(entities = [Backup::class, Item::class], version = 1)
abstract class BackupDatabase: RoomDatabase() {
	abstract fun backupDao(): BackupDao
	abstract fun itemDao(): ItemDao
}