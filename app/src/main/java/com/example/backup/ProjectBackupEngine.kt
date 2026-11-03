package com.example.backup

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.CanonicalSourceEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TakeEntity
import com.example.data.model.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupExportResult(
    val backupFile: File,
    val sha256Checksum: String,
    val filesCount: Int,
    val sizeBytes: Long
)

data class RestoreValidationResult(
    val isValid: Boolean,
    val projectName: String,
    val takesCount: Int,
    val errorMessage: String? = null
)

class ProjectBackupEngine(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.musicDao()

    suspend fun createProjectBackup(projectId: String): BackupExportResult = withContext(Dispatchers.IO) {
        val project = dao.getProjectById(projectId) ?: throw IllegalArgumentException("پروژه یافت نشد.")
        val tracks = dao.getTracksForProject(projectId).first()
        val allTakes = mutableListOf<TakeEntity>()
        val allSources = mutableListOf<CanonicalSourceEntity>()

        for (t in tracks) {
            val takes = dao.getTakesForTrack(t.id).first()
            allTakes.addAll(takes)
            for (take in takes) {
                dao.getCanonicalSourceById(take.sourceId)?.let { allSources.add(it) }
            }
        }

        val backupsDir = File(context.filesDir, "backups").apply { mkdirs() }
        val timestamp = System.currentTimeMillis()
        val cleanName = project.name.replace(Regex("[^a-zA-Z0-9آ-ی]"), "_")
        val zipFile = File(backupsDir, "SiavashBackup_${cleanName}_$timestamp.siavash-bak")

        // Build Manifest JSON
        val manifestJson = JSONObject().apply {
            put("version", "1.0")
            put("app", "SIAVASH MUSIC CREATOR")
            put("createdAt", timestamp)
            put("projectId", project.id)
            put("projectName", project.name)
            put("bpm", project.bpm)
            put("timeSignature", project.timeSignature)
            put("tuning", project.referenceTuning)

            val tracksArray = JSONArray()
            for (tr in tracks) {
                tracksArray.put(JSONObject().apply {
                    put("id", tr.id)
                    put("title", tr.title)
                    put("trackNumber", tr.trackNumber)
                    put("volume", tr.volume.toDouble())
                    put("pan", tr.pan.toDouble())
                })
            }
            put("tracks", tracksArray)

            val takesArray = JSONArray()
            for (tk in allTakes) {
                takesArray.put(JSONObject().apply {
                    put("id", tk.id)
                    put("trackId", tk.trackId)
                    put("sourceId", tk.sourceId)
                    put("takeNumber", tk.takeNumber)
                    put("title", tk.title)
                    put("ratingStars", tk.ratingStars)
                })
            }
            put("takes", takesArray)

            val sourcesArray = JSONArray()
            for (src in allSources) {
                sourcesArray.put(JSONObject().apply {
                    put("id", src.id)
                    put("sha256", src.sha256Checksum)
                    put("sampleRate", src.sampleRate)
                    put("bitDepth", src.bitDepth)
                    put("channelCount", src.channelCount)
                    put("durationMs", src.durationMs)
                })
            }
            put("canonicalSources", sourcesArray)
        }

        var totalFilesAdded = 1 // manifest

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Write manifest.json
            val manifestBytes = manifestJson.toString(2).toByteArray(Charsets.UTF_8)
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestBytes)
            zos.closeEntry()

            // Write all canonical audio files
            for (src in allSources) {
                val audioFile = File(src.filePath)
                if (audioFile.exists()) {
                    zos.putNextEntry(ZipEntry("audio/${src.id}.wav"))
                    FileInputStream(audioFile).use { fis ->
                        val buf = ByteArray(8192)
                        var read: Int
                        while (fis.read(buf).also { read = it } != -1) {
                            zos.write(buf, 0, read)
                        }
                    }
                    zos.closeEntry()
                    totalFilesAdded++
                }
            }
        }

        val checksum = calculateSha256(zipFile)
        BackupExportResult(
            backupFile = zipFile,
            sha256Checksum = checksum,
            filesCount = totalFilesAdded,
            sizeBytes = zipFile.length()
        )
    }

    suspend fun restoreProjectBackup(backupZipFile: File): RestoreValidationResult = withContext(Dispatchers.IO) {
        if (!backupZipFile.exists()) {
            return@withContext RestoreValidationResult(false, "", 0, "فایل پشتیبان وجود ندارد.")
        }

        var manifestContent: String? = null
        val extractedAudioFiles = mutableMapOf<String, ByteArray>()

        try {
            ZipInputStream(FileInputStream(backupZipFile)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "manifest.json") {
                        manifestContent = zis.readBytes().toString(Charsets.UTF_8)
                    } else if (entry.name.startsWith("audio/") && entry.name.endsWith(".wav")) {
                        val sourceId = entry.name.removePrefix("audio/").removeSuffix(".wav")
                        extractedAudioFiles[sourceId] = zis.readBytes()
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            return@withContext RestoreValidationResult(false, "", 0, "خطا در خواندن فایل آرشیو پشتیبان: ${e.message}")
        }

        if (manifestContent == null) {
            return@withContext RestoreValidationResult(false, "", 0, "فایل مانیفست در آرشیو یافت نشد.")
        }

        val manifest = JSONObject(manifestContent!!)
        val projectName = manifest.optString("projectName", "پروژه بازیابی شده")
        val restoredProjectId = UUID.randomUUID().toString() // New ID to prevent conflict

        val project = ProjectEntity(
            id = restoredProjectId,
            name = "$projectName (بازیابی شده)",
            bpm = manifest.optInt("bpm", 110),
            timeSignature = manifest.optString("timeSignature", "4/4"),
            referenceTuning = manifest.optString("tuning", "A440")
        )
        dao.insertProject(project)

        val tracksArray = manifest.optJSONArray("tracks") ?: JSONArray()
        val oldToNewTrackId = mutableMapOf<String, String>()

        for (i in 0 until tracksArray.length()) {
            val trObj = tracksArray.getJSONObject(i)
            val oldTrId = trObj.getString("id")
            val newTrId = UUID.randomUUID().toString()
            oldToNewTrackId[oldTrId] = newTrId

            dao.insertTrack(
                TrackEntity(
                    id = newTrId,
                    projectId = restoredProjectId,
                    title = trObj.getString("title"),
                    trackNumber = trObj.getInt("trackNumber"),
                    volume = trObj.getDouble("volume").toFloat(),
                    pan = trObj.getDouble("pan").toFloat()
                )
            )
        }

        val sourcesArray = manifest.optJSONArray("canonicalSources") ?: JSONArray()
        val oldToNewSourceId = mutableMapOf<String, String>()

        for (i in 0 until sourcesArray.length()) {
            val srcObj = sourcesArray.getJSONObject(i)
            val oldSrcId = srcObj.getString("id")
            val expectedSha256 = srcObj.getString("sha256")
            val audioBytes = extractedAudioFiles[oldSrcId] ?: continue

            val newSrcId = UUID.randomUUID().toString()
            oldToNewSourceId[oldSrcId] = newSrcId

            val audioDir = File(context.filesDir, "projects/$restoredProjectId/takes/$newSrcId").apply { mkdirs() }
            val audioFile = File(audioDir, "raw_canonical.wav")
            FileOutputStream(audioFile).use { it.write(audioBytes) }
            audioFile.setReadOnly()

            // Verify integrity
            val actualChecksum = calculateSha256(audioFile)
            if (actualChecksum != expectedSha256) {
                return@withContext RestoreValidationResult(
                    false,
                    projectName,
                    0,
                    "خطای تطابق هش SHA-256 در فایل صوتی: یکپارچگی فایل مخدوش شده است."
                )
            }

            dao.insertCanonicalSource(
                CanonicalSourceEntity(
                    id = newSrcId,
                    filePath = audioFile.absolutePath,
                    sha256Checksum = actualChecksum,
                    sampleRate = srcObj.optInt("sampleRate", 44100),
                    bitDepth = srcObj.optInt("bitDepth", 16),
                    channelCount = srcObj.optInt("channelCount", 1),
                    durationMs = srcObj.optLong("durationMs", 0L),
                    fileSizeBytes = audioFile.length(),
                    isReadOnly = true
                )
            )
        }

        val takesArray = manifest.optJSONArray("takes") ?: JSONArray()
        for (i in 0 until takesArray.length()) {
            val tkObj = takesArray.getJSONObject(i)
            val oldTrId = tkObj.getString("trackId")
            val oldSrcId = tkObj.getString("sourceId")
            val newTrId = oldToNewTrackId[oldTrId] ?: continue
            val newSrcId = oldToNewSourceId[oldSrcId] ?: continue

            val newTakeId = UUID.randomUUID().toString()
            dao.insertTake(
                TakeEntity(
                    id = newTakeId,
                    trackId = newTrId,
                    sourceId = newSrcId,
                    takeNumber = tkObj.getInt("takeNumber"),
                    title = tkObj.getString("title"),
                    ratingStars = tkObj.optInt("ratingStars", 0),
                    sourceType = "RAW"
                )
            )
        }

        RestoreValidationResult(
            isValid = true,
            projectName = project.name,
            takesCount = takesArray.length(),
            errorMessage = null
        )
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(8192)
            var n: Int
            while (fis.read(buf).also { n = it } != -1) {
                digest.update(buf, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
