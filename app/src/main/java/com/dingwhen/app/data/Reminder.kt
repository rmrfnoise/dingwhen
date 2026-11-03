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
import com.dingwhen.app.model.AudioState
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
    /** 通知按钮：是否显示确认按钮（默认文案「知道了」，可自定义） */
    @ColumnInfo(name = "action_dismiss") val actionDismiss: Boolean = false,
    /** 「知道了」按钮的自定义文字；null 或空表示用语言包里的默认文案 */
    @ColumnInfo(name = "action_dismiss_label") val actionDismissLabel: String? = null,
    /** 播报文案。null 或空表示直接用 content——通知要短，播报要完整，两者不必相同 */
    @ColumnInfo(name = "speech_text") val speechText: String? = null,
    /**
     * 这条提醒用的音色。空表示没指定过，合成时回落到设置里的默认音色。
     *
     * 音色在合成那一刻就写进了音频文件，所以不同音色的提醒天然是不同文件，
     * 播放侧不需要任何判断——这也是"打卡用男声、喝水用女声"能成立的原因。
     */
    @ColumnInfo(name = "voice_type") val voiceType: String = "",
    /**
     * 这条提醒的语气（豆包 context_texts，一句话描述）。空表示不用语气。
     *
     * 和音色一样在合成那一刻写进音频，所以"上班打卡催一点、下班打卡轻一点"
     * 两句话会合成出两个文件，各播各的。
     */
    @ColumnInfo(name = "emotion") val emotion: String = "",
    /** 这条提醒是否启用语气。它进缓存键，所以必须逐条落库 */
    @ColumnInfo(name = "emotion_enabled") val emotionEnabled: Boolean = false,
    /**
     * 每轮念几遍。
     *
     * 放在提醒上而不是全局：催打卡和提醒喝水，需要的力度本来就不一样，
     * 一个全局值只能迁就其中一头。
     */
    @ColumnInfo(name = "play_times") val playTimes: Int = 1,
    /**
     * 没确认就再响。关掉确认按钮时它没有意义——没有停下来的入口，
     * 持续重响只能逼人去关掉整条提醒，排程那边会挡住。
     */
    @ColumnInfo(name = "repeat_on") val repeatOn: Boolean = true,
    /** 已合成的本地音频文件名（不含目录），null 表示没有可用音频 */
    @ColumnInfo(name = "audio_file") val audioFile: String? = null,
    @ColumnInfo(name = "audio_state") val audioState: AudioState = AudioState.NONE,
    /** 合成失败原因，存机器码，文案交给语言包 */
    @ColumnInfo(name = "audio_error") val audioError: String? = null,
    /** 本次触发被确认的时刻，升级提醒靠它判断要不要继续 */
    @ColumnInfo(name = "acked_at") val ackedAt: Long? = null,
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

    @TypeConverter
    fun audioStateToString(value: AudioState): String = value.name

    @TypeConverter
    fun stringToAudioState(value: String): AudioState = AudioState.fromName(value)
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
 * 表结构变更就把 version +1，靠 fallbackToDestructiveMigration 直接重建空库。
 * 这是自用应用，只有一个人用，改结构时丢掉旧提醒可以接受；
 * 因此绝不写 Migration——少一份需要维护、还可能写错的东西。
 * 注意版本号必须跟着改，否则 Room 校验失败会直接崩（同版本号不同结构它不会重建）。
 */
@Database(entities = [Reminder::class], version = 8, exportSchema = false)
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
