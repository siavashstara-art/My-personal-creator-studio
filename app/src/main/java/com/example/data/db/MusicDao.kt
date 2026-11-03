package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    // Tracks
    @Query("SELECT * FROM tracks WHERE projectId = :projectId ORDER BY trackNumber ASC")
    fun getTracksForProject(projectId: String): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: String)

    // Canonical Sources
    @Query("SELECT * FROM canonical_sources WHERE id = :id LIMIT 1")
    suspend fun getCanonicalSourceById(id: String): CanonicalSourceEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCanonicalSource(source: CanonicalSourceEntity)

    // Takes
    @Query("SELECT * FROM takes WHERE trackId = :trackId ORDER BY takeNumber DESC")
    fun getTakesForTrack(trackId: String): Flow<List<TakeEntity>>

    @Query("SELECT * FROM takes WHERE id = :takeId LIMIT 1")
    suspend fun getTakeById(takeId: String): TakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTake(take: TakeEntity)

    @Update
    suspend fun updateTake(take: TakeEntity)

    @Query("DELETE FROM takes WHERE id = :id")
    suspend fun deleteTakeById(id: String)

    // Processing Chains
    @Query("SELECT * FROM processing_chains WHERE takeId = :takeId LIMIT 1")
    fun getProcessingChainForTake(takeId: String): Flow<ProcessingChainEntity?>

    @Query("SELECT * FROM processing_chains WHERE takeId = :takeId LIMIT 1")
    suspend fun getProcessingChainDirect(takeId: String): ProcessingChainEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProcessingChain(chain: ProcessingChainEntity)

    // Analysis Results
    @Query("SELECT * FROM analysis_results WHERE takeId = :takeId LIMIT 1")
    fun getAnalysisForTake(takeId: String): Flow<AnalysisResultEntity?>

    @Query("SELECT * FROM analysis_results WHERE takeId = :takeId LIMIT 1")
    suspend fun getAnalysisDirect(takeId: String): AnalysisResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysisResult(result: AnalysisResultEntity)

    // Lyrics
    @Query("SELECT * FROM lyrics WHERE projectId = :projectId LIMIT 1")
    fun getLyricsForProject(projectId: String): Flow<LyricsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLyrics(lyrics: LyricsEntity)

    // Referral League
    @Query("SELECT * FROM referral_league WHERE id = :id LIMIT 1")
    fun getReferralProfile(id: String = "default_user"): Flow<ReferralEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReferralProfile(referral: ReferralEntity)

    // Hall of Fame
    @Query("SELECT * FROM hall_of_fame ORDER BY rankIndex ASC")
    fun getHallOfFameMembers(): Flow<List<HallOfFameMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHallOfFameMembers(members: List<HallOfFameMember>)
}
