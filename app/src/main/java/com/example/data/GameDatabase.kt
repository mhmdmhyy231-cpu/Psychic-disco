package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "game_progress")
data class GameProgress(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "current_chapter") val currentChapter: Int = 0,
    @ColumnInfo(name = "score") val score: Int = 0,
    @ColumnInfo(name = "backpack_items_csv") val backpackItemsCsv: String = "خريطة ورقية, مصباح مكسور",
    @ColumnInfo(name = "solved_riddles_csv") val solvedRiddlesCsv: String = "",
    @ColumnInfo(name = "current_relic_id") val currentRelicId: String = "scarab"
)

@Entity(tableName = "relic")
data class Relic(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val dateUnlocked: Long = 0L
)

@Dao
interface AdventureDao {
    @Query("SELECT * FROM game_progress WHERE id = 1 LIMIT 1")
    fun getProgressFlow(): Flow<GameProgress?>

    @Query("SELECT * FROM game_progress WHERE id = 1 LIMIT 1")
    suspend fun getProgressDirect(): GameProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: GameProgress)

    @Query("SELECT * FROM relic")
    fun getAllRelicsFlow(): Flow<List<Relic>>

    @Query("SELECT * FROM relic WHERE id = :id LIMIT 1")
    suspend fun getRelicById(id: String): Relic?

    @Query("SELECT * FROM relic WHERE isUnlocked = 1")
    suspend fun getUnlockedRelics(): List<Relic>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelics(relics: List<Relic>)

    @Query("UPDATE relic SET isUnlocked = 1, dateUnlocked = :timestamp WHERE id = :id")
    suspend fun unlockRelic(id: String, timestamp: Long)

    @Query("DELETE FROM game_progress")
    suspend fun deleteProgress()
}

@Database(entities = [GameProgress::class, Relic::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun adventureDao(): AdventureDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "adventure_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class GameRepository(private val dao: AdventureDao) {
    val progressFlow: Flow<GameProgress?> = dao.getProgressFlow()
    val relicsFlow: Flow<List<Relic>> = dao.getAllRelicsFlow()

    suspend fun getProgressDirect(): GameProgress {
        return dao.getProgressDirect() ?: GameProgress().also {
            dao.saveProgress(it)
        }
    }

    suspend fun saveProgress(progress: GameProgress) {
        dao.saveProgress(progress)
    }

    suspend fun unlockRelic(id: String) {
        dao.unlockRelic(id, System.currentTimeMillis())
        val progress = getProgressDirect()
        val backpackItems = progress.backpackItemsCsv.split(",").map { it.trim() }.toMutableList()
        val relic = dao.getRelicById(id)
        if (relic != null && !backpackItems.contains(relic.name)) {
            backpackItems.add(relic.name)
            saveProgress(progress.copy(backpackItemsCsv = backpackItems.joinToString(", ")))
        }
    }

    suspend fun addScore(points: Int) {
        val progress = getProgressDirect()
        saveProgress(progress.copy(score = progress.score + points))
    }

    suspend fun advanceChapter() {
        val progress = getProgressDirect()
        if (progress.currentChapter < 8) {
            saveProgress(progress.copy(currentChapter = progress.currentChapter + 1))
        }
    }

    suspend fun resetGame() {
        dao.deleteProgress()
        val defaultProgress = GameProgress()
        dao.saveProgress(defaultProgress)
        // Reset relics locking
        val defaultRelics = listOf(
            Relic("scarab", "جعفر الجعل الذهبي", "تميمة مصرية قديمة بشكل خنفسة الروث (الجعل) ترمز للحياة والبعث وتجدد الشمس.", isUnlocked = true),
            Relic("ankh", "مفتاح الحياة (عنخ)", "رمز هيروغليفي يمثل الحياة الأبدية، وكان ملوك الآلهة يحملونه في الرسومات القديمة.", isUnlocked = false),
            Relic("sphinx", "أبو الهول الذهبي", "تمثال برأس إنسان وجسد أسد يمثل القوة والحكمة ويحرس هضبة الجيزة لآلاف السنين.", isUnlocked = false),
            Relic("pyramid", "مكعب خوفو الذهبي", "هرم خوفو الأكبر، أحد عجائب الدنيا السبع القديمة الباقية، تحفة هندسية فلكية رائعة.", isUnlocked = false),
            Relic("mummy", "القناع الذهبي لـ توت عنخ آمون", "القناع الجنائزي الذهبي للملك توت عنخ آمون المصنوع من الذهب الخالص والأحجار الكريمة.", isUnlocked = false),
            Relic("cartouche", "خرطوشة الأسماء الملكية", "إطار بيضاوي يحتوي على قائمة الرموز الملكية والأسماء لحماية فرعون من الأرواح الضارة.", isUnlocked = false),
            Relic("obelisk", "المسلة الفلكية الشامخة", "عمود فلكي فرعوني قديم شاهق الارتفاع يقترن بمسارات النجوم والأشعة الهندسية المتوازية للشمس.", isUnlocked = false),
            Relic("scroll", "بردية الحكمة المفقودة", "لفافة بردي نادرة مكتوبة بالخط الهيراطيقي والرموز الطبية والفلكية الفرعونية العميقة.", isUnlocked = false)
        )
        dao.insertRelics(defaultRelics)
    }

    suspend fun initializeDefaultRelicsIfEmpty() {
        val count = dao.getUnlockedRelics().size
        val allRelics = dao.getRelicById("scarab")
        if (allRelics == null) {
            val defaultRelics = listOf(
                Relic("scarab", "جعفر الجعل الذهبي", "تميمة مصرية قديمة بشكل خنفسة الروث (الجعل) ترمز للحياة والبعث وتجدد الشمس.", isUnlocked = true),
                Relic("ankh", "مفتاح الحياة (عنخ)", "رمز هيروغليفي يمثل الحياة الأبدية، وكان ملوك الآلهة يحملونه في الرسومات القديمة.", isUnlocked = false),
                Relic("sphinx", "أبو الهول الذهبي", "تمثال برأس إنسان وجسد أسد يمثل القوة والحكمة ويحرس هضبة الجيزة لآلاف السنين.", isUnlocked = false),
                Relic("pyramid", "مكعب خوفو الذهبي", "هرم خوفو الأكبر، أحد عجائب الدنيا السبع القديمة الباقية، تحفة هندسية فلكية رائعة.", isUnlocked = false),
                Relic("mummy", "القناع الذهبي لـ توت عنخ آمون", "القناع الجنائزي الذهبي للملك توت عنخ آمون المصنوع من الذهب الخالص والأحجار الكريمة.", isUnlocked = false),
                Relic("cartouche", "خرطوشة الأسماء الملكية", "إطار بيضاوي يحتوي على قائمة الرموز الملكية والأسماء لحماية فرعون من الأرواح الضارة.", isUnlocked = false),
                Relic("obelisk", "المسلة الفلكية الشامخة", "عمود فلكي فرعوني قديم شاهق الارتفاع يقترن بمسارات النجوم والأشعة الهندسية المتوازية للشمس.", isUnlocked = false),
                Relic("scroll", "بردية الحكمة المفقودة", "لفافة بردي نادرة مكتوبة بالخط الهيراطيقي والرموز الطبية والفلكية الفرعونية العميقة.", isUnlocked = false)
            )
            dao.insertRelics(defaultRelics)
        }
    }
}
