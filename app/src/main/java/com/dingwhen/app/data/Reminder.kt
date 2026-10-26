package com.dingwhen.app.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import android.content.Context
import com.dingwhen.app.model.RepeatType
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** 提醒内容，例如"打卡""还信用卡" */
    val content: String,
    val hour: Int,
    val minute: Int,
    @ColumnInfo(name = "repeat_type") val repeatType: RepeatType,
    /** 每周的星期几，逗号分隔的 ISO 序号（1=周一 … 7=周日） */
    @ColumnInfo(name = "days_of_week") val daysOfWeek: String = "",
    /** 每月几号，仅 MONTHLY 使用 */
    @ColumnInfo(name = "day_of_month") val dayOfMonth: Int? = null,
    /** 仅一次提醒的日期（epochDay），仅 ONCE 使用 */
    @ColumnInfo(name = "date_epoch_day") val dateEpochDay: Long? = null,
    val enabled: Boolean = true,
    /** 下一次触发的绝对时刻（epoch millis），列表排序与调度都读它 */
    @ColumnInfo(name = "next_trigger_at") val nextTriggerAt: Long = 0L,
    /** 通知按钮：到点打开的目标应用包名，null 表示不出这个按钮 */
    @ColumnInfo(name = "action_open_pkg") val actionOpenPackage: String? = null,
    /** 通知按钮：是否显示「稍后 10 分钟」 */
    @ColumnInfo(name = "action_snooze") val actionSnooze: Boolean = false,
    /** 通知按钮：是否显示「知道了」 */
    @ColumnInfo(name = "action_dismiss") val actionDismiss: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_fired_at") val lastFiredAt: Long? = null
) {
    val days: Set<DayOfWeek>
        get() = daysOfWeek.split(',').mapNotNull { it.trim().toIntOrNull() }
            .mapNotNull { v -> DayOfWeek.entries.firstOrNull { it.value == v } }
            .toSet()

    val date: LocalDate?
        get() = dateEpochDay?.let { LocalDate.ofEpochDay(it) }
}

class Converters {
    @TypeConverter
    fun repeatToString(value: RepeatType): String = value.name

    @TypeConverter
    fun stringToRepeat(value: String): RepeatType = RepeatType.fromName(value)
}

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY enabled DESC, next_trigger_at ASC")
    fun observeAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/**
 * 开发阶段不做数据库迁移：表结构变了就把 version +1，靠 fallbackToDestructiveMigration 直接重建。
 * 注意版本号必须跟着改，否则 Room 校验会直接崩（同版本不同结构它不会自动重建）。
 * 上线前如需要保留用户数据，再补 Migration 并去掉 fallback。
 */
@Database(entities = [Reminder::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "dingwhen.db"
            ).fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
