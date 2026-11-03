package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.*
import com.example.backup.ProjectBackupEngine
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class AppScreen {
    STUDIO,
    PROJECTS,
    MIXER,
    ANALYSIS,
    REFERRAL_LEAGUE,
    HALL_OF_FAME,
    BACKUP_RESTORE,
    PROMO_VIDEO
}

class SiavashStudioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.musicDao()

    val recorder = WavAudioRecorder(application)
    val player = WavAudioPlayer()
    val vocalEngine = VocalAnalysisEngine()
    val mixerExporter = AudioMixerExporter(application)
    val backupEngine = ProjectBackupEngine(application)

    private val _currentScreen = MutableStateFlow(AppScreen.STUDIO)
    val currentScreen = _currentScreen.asStateFlow()

    val allProjects: StateFlow<List<ProjectEntity>> = dao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProjectId = MutableStateFlow<String?>(null)
    val activeProjectId = _activeProjectId.asStateFlow()

    val activeProject: StateFlow<ProjectEntity?> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else allProjects.map { list -> list.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeTracks: StateFlow<List<TrackEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else dao.getTracksForProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId = _selectedTrackId.asStateFlow()

    val currentTakes: StateFlow<List<TakeEntity>> = _selectedTrackId.flatMapLatest { trackId ->
        if (trackId == null) flowOf(emptyList())
        else dao.getTakesForTrack(trackId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // A/B Take Comparison
    private val _takeA = MutableStateFlow<TakeEntity?>(null)
    val takeA = _takeA.asStateFlow()

    private val _takeB = MutableStateFlow<TakeEntity?>(null)
    val takeB = _takeB.asStateFlow()

    private val _activeTakeAB = MutableStateFlow<String>("A") // "A" or "B"
    val activeTakeAB = _activeTakeAB.asStateFlow()

    // Processing Chain for current selected take
    private val _selectedTakeForDsp = MutableStateFlow<TakeEntity?>(null)
    val selectedTakeForDsp = _selectedTakeForDsp.asStateFlow()

    val currentProcessingChain: StateFlow<ProcessingChainEntity?> = _selectedTakeForDsp.flatMapLatest { take ->
        if (take == null) flowOf(null)
        else dao.getProcessingChainForTake(take.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Vocal Analysis for current selected take
    val currentAnalysis: StateFlow<AnalysisResultEntity?> = _selectedTakeForDsp.flatMapLatest { take ->
        if (take == null) flowOf(null)
        else dao.getAnalysisForTake(take.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Teleprompter Lyrics
    val activeLyrics: StateFlow<LyricsEntity?> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else dao.getLyricsForProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Referral League & Hall of Fame
    val referralProfile: StateFlow<ReferralEntity?> = dao.getReferralProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hallOfFameList: StateFlow<List<HallOfFameMember>> = dao.getHallOfFameMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Status notifications / feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        // Automatically select first project when loaded
        viewModelScope.launch {
            allProjects.filter { it.isNotEmpty() }.first().let { list ->
                if (_activeProjectId.value == null) {
                    selectProject(list.first().id)
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectProject(projectId: String) {
        _activeProjectId.value = projectId
        viewModelScope.launch {
            val tracks = dao.getTracksForProject(projectId).first()
            if (tracks.isNotEmpty()) {
                _selectedTrackId.value = tracks.first().id
            } else {
                _selectedTrackId.value = null
            }
        }
    }

    fun selectTrack(trackId: String) {
        _selectedTrackId.value = trackId
    }

    fun createNewProject(name: String, tuning: String, bpm: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val projectId = UUID.randomUUID().toString()
            val project = ProjectEntity(
                id = projectId,
                name = name.ifBlank { "پروژه آوازی جدید" },
                referenceTuning = tuning,
                bpm = bpm
            )
            dao.insertProject(project)

            // Create default Lead Vocal track
            val trackId = UUID.randomUUID().toString()
            dao.insertTrack(
                TrackEntity(
                    id = trackId,
                    projectId = projectId,
                    title = "آواز اصلی (Lead Vocal)",
                    trackNumber = 1,
                    colorHex = "#D4AF37"
                )
            )

            // Default lyrics placeholder
            dao.insertOrUpdateLyrics(
                LyricsEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = projectId,
                    content = "متن ترانه یا شعر دستگاهی خود را اینجا بنویسید...",
                    isRtl = true
                )
            )

            _activeProjectId.value = projectId
            _selectedTrackId.value = trackId
            withContext(Dispatchers.Main) {
                showToast("پروژه با موفقیت ایجاد شد.")
            }
        }
    }

    fun startRecording() {
        val projId = _activeProjectId.value ?: return
        val trId = _selectedTrackId.value ?: return
        val tempTakeId = UUID.randomUUID().toString()

        recorder.startRecording(projId, tempTakeId) { errorMsg ->
            showToast(errorMsg)
        }
    }

    fun stopRecordingAndSave() {
        val projId = _activeProjectId.value ?: return
        val trId = _selectedTrackId.value ?: return
        val currentTakeCount = currentTakes.value.size

        viewModelScope.launch(Dispatchers.IO) {
            val tempTakeId = UUID.randomUUID().toString()
            val result = recorder.stopRecording(projId, tempTakeId)

            if (result != null) {
                // 1. Insert immutable canonical source
                val canonicalSource = CanonicalSourceEntity(
                    id = result.canonicalSourceId,
                    filePath = result.filePath,
                    sha256Checksum = result.sha256Checksum,
                    sampleRate = result.sampleRate,
                    bitDepth = result.bitDepth,
                    channelCount = result.channelCount,
                    durationSamples = result.durationSamples,
                    durationMs = result.durationMs,
                    fileSizeBytes = result.fileSizeBytes,
                    isReadOnly = true
                )
                dao.insertCanonicalSource(canonicalSource)

                // 2. Insert Take
                val takeId = UUID.randomUUID().toString()
                val newTake = TakeEntity(
                    id = takeId,
                    trackId = trId,
                    sourceId = result.canonicalSourceId,
                    takeNumber = currentTakeCount + 1,
                    title = "برداشت (Take ${currentTakeCount + 1})",
                    sourceType = "RAW"
                )
                dao.insertTake(newTake)

                // 3. Create default Non-destructive Processing Chain
                dao.insertOrUpdateProcessingChain(
                    ProcessingChainEntity(
                        id = UUID.randomUUID().toString(),
                        takeId = takeId
                    )
                )

                // Automatically run offline Vocal Intelligence analysis on raw canonical take
                val analysisResult = vocalEngine.analyzeCanonicalWav(File(result.filePath), result.sampleRate)
                dao.insertAnalysisResult(
                    AnalysisResultEntity(
                        id = UUID.randomUUID().toString(),
                        takeId = takeId,
                        sourceType = "RAW",
                        averageF0Hz = analysisResult.averageF0Hz,
                        lowestF0Hz = analysisResult.lowestF0Hz,
                        highestF0Hz = analysisResult.highestF0Hz,
                        f0RangeName = analysisResult.vocalRangeDescription,
                        pitchStabilityScore = analysisResult.pitchStabilityScore,
                        vibratoRateHz = analysisResult.vibratoRateHz,
                        vibratoDepthCents = analysisResult.vibratoDepthCents,
                        tahrirDensityScore = analysisResult.tahrirDensityScore,
                        microtonalVarianceCents = analysisResult.microtonalVarianceCents,
                        coachSummaryText = analysisResult.coachSummaryReport
                    )
                )

                _selectedTakeForDsp.value = newTake
                setTakeForComparison(newTake)

                withContext(Dispatchers.Main) {
                    showToast("برداشت ${currentTakeCount + 1} با امضای یکپارچگی SHA-256 ذخیره و تحلیل شد.")
                }
            }
        }
    }

    fun setTakeForComparison(take: TakeEntity) {
        if (_takeA.value == null) {
            _takeA.value = take
        } else if (_takeB.value == null && _takeA.value?.id != take.id) {
            _takeB.value = take
        } else {
            // cycle
            _takeA.value = take
        }
    }

    fun setTakeA(take: TakeEntity) {
        _takeA.value = take
    }

    fun setTakeB(take: TakeEntity) {
        _takeB.value = take
    }

    fun switchAB(target: String) {
        _activeTakeAB.value = target
        val currentPosition = player.currentPositionMs.value
        val targetTake = if (target == "A") _takeA.value else _takeB.value
        if (targetTake != null) {
            playTake(targetTake, currentPosition)
        }
    }

    fun playTake(take: TakeEntity, startFromMs: Long = 0L) {
        _selectedTakeForDsp.value = take
        viewModelScope.launch(Dispatchers.IO) {
            val source = dao.getCanonicalSourceById(take.sourceId)
            if (source != null && File(source.filePath).exists()) {
                withContext(Dispatchers.Main) {
                    player.playTake(source.filePath, take.id, startFromMs)
                }
            } else {
                withContext(Dispatchers.Main) {
                    showToast("فایل صوتی یافت نشد.")
                }
            }
        }
    }

    fun updateProcessingChain(
        eqLow: Float? = null,
        eqMid: Float? = null,
        eqHigh: Float? = null,
        compThresh: Float? = null,
        compRatio: Float? = null,
        reverbRoom: Float? = null,
        reverbWet: Float? = null,
        volGain: Float? = null
    ) {
        val currentChain = currentProcessingChain.value ?: return
        val updated = currentChain.copy(
            eqLowGain = eqLow ?: currentChain.eqLowGain,
            eqMidGain = eqMid ?: currentChain.eqMidGain,
            eqHighGain = eqHigh ?: currentChain.eqHighGain,
            compressorThreshold = compThresh ?: currentChain.compressorThreshold,
            compressorRatio = compRatio ?: currentChain.compressorRatio,
            reverbRoomSize = reverbRoom ?: currentChain.reverbRoomSize,
            reverbWetDry = reverbWet ?: currentChain.reverbWetDry,
            volumeGain = volGain ?: currentChain.volumeGain,
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertOrUpdateProcessingChain(updated)
        }
    }

    fun updateLyrics(content: String, fontScale: Float = 1.3f, scrollSpeed: Int = 2) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = activeLyrics.value
            val entity = LyricsEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                projectId = projId,
                content = content,
                fontScale = fontScale,
                scrollSpeed = scrollSpeed,
                isRtl = true
            )
            dao.insertOrUpdateLyrics(entity)
        }
    }

    fun exportMasterMix() {
        val proj = activeProject.value ?: return
        val takes = currentTakes.value
        if (takes.isEmpty()) {
            showToast("هیچ برداشتی برای میکس وجود ندارد.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val pairs = mutableListOf<Pair<File, ProcessingChainEntity?>>()
            for (t in takes.take(3)) {
                val src = dao.getCanonicalSourceById(t.sourceId)
                if (src != null) {
                    val file = File(src.filePath)
                    val chain = dao.getProcessingChainDirect(t.id)
                    pairs.add(Pair(file, chain))
                }
            }
            val result = mixerExporter.exportProjectMaster(proj.id, proj.name, pairs)
            withContext(Dispatchers.Main) {
                showToast("مستر نهایی در حافظه استودیو ذخیره شد: ${result.exportFile.name}")
            }
        }
    }

    fun backupCurrentProject() {
        val proj = activeProject.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = backupEngine.createProjectBackup(proj.id)
                withContext(Dispatchers.Main) {
                    showToast("پشتیبان امن با موفقیت ایجاد شد: ${res.backupFile.name} (حجم: ${res.sizeBytes / 1024} KB)")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("خطا در ایجاد پشتیبان: ${e.message}")
                }
            }
        }
    }
}
