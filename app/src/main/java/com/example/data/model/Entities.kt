package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tagline: String = "Your Voice. Your Music. Your Studio.",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val bpm: Int = 110,
    val timeSignature: String = "4/4",
    val referenceTuning: String = "A440 - همایون و اصفهان (میکروتونال ایرانی)",
    val notes: String = ""
)

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class TrackEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val trackNumber: Int,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val volume: Float = 0.85f,
    val pan: Float = 0.0f,
    val colorHex: String = "#D4AF37"
)

@Entity(tableName = "canonical_sources")
data class CanonicalSourceEntity(
    @PrimaryKey val id: String,
    val filePath: String,
    val sha256Checksum: String,
    val sampleRate: Int = 44100,
    val bitDepth: Int = 16,
    val channelCount: Int = 1,
    val durationSamples: Long = 0L,
    val durationMs: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val isReadOnly: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "takes",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CanonicalSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["trackId"]), Index(value = ["sourceId"])]
)
data class TakeEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val sourceId: String,
    val takeNumber: Int,
    val title: String,
    val ratingStars: Int = 0,
    val sourceType: String = "RAW", // "RAW", "PROCESSED", "AI_GENERATED"
    val isMasterTake: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "processing_chains",
    foreignKeys = [
        ForeignKey(
            entity = TakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["takeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["takeId"])]
)
data class ProcessingChainEntity(
    @PrimaryKey val id: String,
    val takeId: String,
    val eqLowGain: Float = 0f, // -12dB .. +12dB
    val eqMidGain: Float = 0f,
    val eqHighGain: Float = 0f,
    val compressorThreshold: Float = -18f, // -40dB .. 0dB
    val compressorRatio: Float = 3.0f,
    val compressorAttackMs: Float = 20f,
    val reverbRoomSize: Float = 0.25f, // 0..1
    val reverbWetDry: Float = 0.15f,   // 0..1
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val volumeGain: Float = 1.0f,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "analysis_results",
    foreignKeys = [
        ForeignKey(
            entity = TakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["takeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["takeId"])]
)
data class AnalysisResultEntity(
    @PrimaryKey val id: String,
    val takeId: String,
    val sourceType: String = "RAW",
    val averageF0Hz: Float = 0f,
    val lowestF0Hz: Float = 0f,
    val highestF0Hz: Float = 0f,
    val f0RangeName: String = "",
    val pitchStabilityScore: Int = 0,
    val vibratoRateHz: Float = 0f,
    val vibratoDepthCents: Float = 0f,
    val tahrirDensityScore: Int = 0,
    val microtonalVarianceCents: Float = 0f,
    val coachSummaryText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "lyrics",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class LyricsEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val content: String,
    val fontScale: Float = 1.3f,
    val scrollSpeed: Int = 2,
    val isRtl: Boolean = true
)

@Entity(tableName = "referral_league")
data class ReferralEntity(
    @PrimaryKey val id: String = "default_user",
    val myReferralCode: String = "SIAVASH-GOLD-777",
    val invitedCount: Int = 7,
    val earnedStudioPoints: Int = 1450,
    val referralRank: String = "سفیر طلایی استودیو (Gold Studio Ambassador)"
)

@Entity(tableName = "hall_of_fame")
data class HallOfFameMember(
    @PrimaryKey val id: String,
    val name: String,
    val badge: String,
    val points: Int,
    val avatarColor: String,
    val rankIndex: Int,
    val title: String
)
