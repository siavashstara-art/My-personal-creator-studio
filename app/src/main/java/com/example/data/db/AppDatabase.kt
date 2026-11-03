package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ProjectEntity::class,
        TrackEntity::class,
        CanonicalSourceEntity::class,
        TakeEntity::class,
        ProcessingChainEntity::class,
        AnalysisResultEntity::class,
        LyricsEntity::class,
        ReferralEntity::class,
        HallOfFameMember::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "siavash_music_studio.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default Hall of Fame & initial referral info
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).musicDao()
                                dao.saveReferralProfile(
                                    ReferralEntity(
                                        id = "default_user",
                                        myReferralCode = "SIAVASH-GOLD-777",
                                        invitedCount = 12,
                                        earnedStudioPoints = 2800,
                                        referralRank = "سفیر ارشد آفرینش آفرینا (Master Studio Ambassador)"
                                    )
                                )
                                dao.insertHallOfFameMembers(
                                    listOf(
                                        HallOfFameMember(
                                            id = "hof_1",
                                            name = "استاد سیاوش علی‌میری",
                                            badge = "👑 اسطوره بنیان‌گذار (Master Creator)",
                                            points = 18500,
                                            avatarColor = "#D4AF37",
                                            rankIndex = 1,
                                            title = "توسعه‌دهنده ارشد و خالق اکوسیستم توانا سیتی"
                                        ),
                                        HallOfFameMember(
                                            id = "hof_2",
                                            name = "آریا گلد استودیو",
                                            badge = "💎 حامی طلایی و سفیر افتخاری",
                                            points = 14200,
                                            avatarColor = "#F5D77F",
                                            rankIndex = 2,
                                            title = "پیشگام طنین‌های اصیل ایرانی و میکروتونال"
                                        ),
                                        HallOfFameMember(
                                            id = "hof_3",
                                            name = "همراز آفرینش",
                                            badge = "🌟 مربی برتر تحریر و آوازیابی",
                                            points = 11900,
                                            avatarColor = "#E5B842",
                                            rankIndex = 3,
                                            title = "پایشگر دقیق فواصل شور، دشتی و اصفهان"
                                        ),
                                        HallOfFameMember(
                                            id = "hof_4",
                                            name = "مجموعه خدمات‌جو امیری",
                                            badge = "🛡️ محافظ اصالت فایل‌های کانونیکال",
                                            points = 9800,
                                            avatarColor = "#C5A038",
                                            rankIndex = 4,
                                            title = "پشتیبان داده‌های غیرمخرب و یکپارچگی صوتی"
                                        )
                                    )
                                )

                                // Seed a demonstration initial project
                                val demoProjectId = UUID.randomUUID().toString()
                                val demoProject = ProjectEntity(
                                    id = demoProjectId,
                                    name = "آواز همایون و عشاق (پروژه نمونه)",
                                    tagline = "Your Voice. Your Music. Your Studio.",
                                    bpm = 95,
                                    timeSignature = "6/8",
                                    referenceTuning = "A440 - دستگاه همایون (گام میکروتونال با ربع پرده کرن)",
                                    notes = "پروژه ضبط آوازی با تأکید بر تحریرهای اصیل و فواصل کرن ایرانی."
                                )
                                dao.insertProject(demoProject)

                                val trackLeadId = UUID.randomUUID().toString()
                                dao.insertTrack(
                                    TrackEntity(
                                        id = trackLeadId,
                                        projectId = demoProjectId,
                                        title = "آواز اصلی (Lead Vocal)",
                                        trackNumber = 1,
                                        volume = 0.9f,
                                        pan = 0.0f,
                                        colorHex = "#D4AF37"
                                    )
                                )

                                val trackBackingId = UUID.randomUUID().toString()
                                dao.insertTrack(
                                    TrackEntity(
                                        id = trackBackingId,
                                        projectId = demoProjectId,
                                        title = "بک‌ترک / ساز همراه (Backing Track)",
                                        trackNumber = 2,
                                        volume = 0.75f,
                                        pan = 0.0f,
                                        colorHex = "#38BDF8"
                                    )
                                )

                                dao.insertOrUpdateLyrics(
                                    LyricsEntity(
                                        id = UUID.randomUUID().toString(),
                                        projectId = demoProjectId,
                                        content = """
در ازل پرتو حسنت ز تجلی دم زد
عشق پیدا شد و آتش به همه عالم زد

جلوه‌ای کرد رخت دید ملک عشق نداشت
عین آتش شد از این غیرت و بر آدم زد

مدعی خواست که آید به تماشاگه راز
دست غیب آمد و بر سینه نامحرم زد
                                        """.trimIndent(),
                                        fontScale = 1.35f,
                                        scrollSpeed = 2,
                                        isRtl = true
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
