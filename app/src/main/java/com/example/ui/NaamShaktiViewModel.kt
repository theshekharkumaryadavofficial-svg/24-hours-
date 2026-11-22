package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.NaamJapDayEntity
import com.example.data.NaamJapRepository
import com.example.data.todayDateKey
import com.example.firebase.GoogleAuthHelper
import com.example.firebase.GoogleAuthResult
import com.example.naamshakti.AnimeWarrior
import com.example.naamshakti.LightningAbility
import com.example.naamshakti.LightningSoundEngine
import com.example.naamshakti.NaamShaktiCatalog
import com.example.naamshakti.NaamShaktiCloudRepository
import com.example.naamshakti.NaamShaktiDao
import com.example.naamshakti.NaamShaktiProfileEntity
import com.example.naamshakti.NaamShaktiRules
import com.example.naamshakti.ShaktiAchievement
import com.example.naamshakti.ShaktiHistoryEntity
import com.example.naamshakti.StoryMission
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NaamShaktiSubScreen(val label: String) {
    DASHBOARD("Shakti"),
    WARRIORS("Warriors"),
    BATTLE("Battle"),
    PROGRESS("Progress"),
    HISTORY("History")
}

data class ActiveBattleState(
    val mission: StoryMission,
    val warriorHp: Int,
    val warriorMaxHp: Int,
    val warriorShield: Int = 0,
    val dodgeReady: Boolean = false,
    val enemyHp: Int,
    val enemyMaxHp: Int,
    val turnNumber: Int = 1,
    val shaktiSpentThisBattle: Int = 0,
    val lastAbilityUsed: LightningAbility? = null,
    val strikeEventId: Long = 0L,
    val enemyHitEventId: Long = 0L,
    val battleLog: String = "Battle initiated! Channel your Naam Shakti ⚡",
    val victoryAchieved: Boolean = false,
    val retreatedByHp: Boolean = false
)

data class CloudAuthUiState(
    val signedIn: Boolean = false,
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val authBusy: Boolean = false,
    val syncingNow: Boolean = false,
    val lastSyncLabel: String = "Sign in to back up Naam Jap & Shakti to Firebase Cloud",
    val errorMessage: String? = null
)

data class NaamShaktiUiState(
    val profile: NaamShaktiProfileEntity = NaamShaktiProfileEntity(),
    val allTimeNaamCount: Int = 0,
    val todayNaamCount: Int = 0,
    val naamHistory: List<NaamJapDayEntity> = emptyList(),
    val shaktiHistory: List<ShaktiHistoryEntity> = emptyList(),
    val currentSubScreen: NaamShaktiSubScreen = NaamShaktiSubScreen.DASHBOARD,
    val showUnlockCelebration: Boolean = false,
    val activeBattle: ActiveBattleState? = null,
    val soundEnabled: Boolean = true,
    val cloudAuth: CloudAuthUiState = CloudAuthUiState(),
    val statusBanner: String? = null
) {
    val selectedWarrior: AnimeWarrior
        get() = NaamShaktiCatalog.warriorById(profile.selectedWarriorId)

    val unlockedWarriorIds: Set<String>
        get() = profile.unlockedWarriorsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet() + "vajra_arjun"

    val unlockProgressCount: Int
        get() = allTimeNaamCount.coerceIn(0, NaamShaktiRules.UNLOCK_NAAM_TARGET)

    val unlockRemainingNaam: Int
        get() = (NaamShaktiRules.UNLOCK_NAAM_TARGET - allTimeNaamCount).coerceAtLeast(0)

    val unlockProgressFraction: Float
        get() = (allTimeNaamCount.toFloat() / NaamShaktiRules.UNLOCK_NAAM_TARGET.toFloat()).coerceIn(0f, 1f)

    val recoveryNaamDone: Int
        get() = if (profile.koRecoveryActive) {
            (allTimeNaamCount - profile.koBaselineAllTimeNaam).coerceAtLeast(0)
        } else 0

    val recoveryNaamRemaining: Int
        get() = (NaamShaktiRules.REVIVE_NAAM_TARGET - recoveryNaamDone).coerceAtLeast(0)

    val recoveryProgressFraction: Float
        get() = (recoveryNaamDone.toFloat() / NaamShaktiRules.REVIVE_NAAM_TARGET.toFloat()).coerceIn(0f, 1f)

    val warriorMaxHealth: Int
        get() = selectedWarrior.baseHealth + (profile.defenseLevel - 1) * 90 + profile.transformationStage * 180

    val transformationMultiplier: Float
        get() = 1f + (profile.transformationStage * 0.16f)

    val currentTransformationName: String
        get() = if (profile.transformationStage <= 0) {
            "Base Form"
        } else {
            selectedWarrior.transformations.getOrElse(profile.transformationStage - 1) {
                selectedWarrior.transformations.last()
            }
        }

    val nextUpgradeCostXp: Int
        get() = 150 + (profile.attackLevel + profile.defenseLevel + profile.auraLevel - 3) * 80

    val nextTransformationCostXp: Int
        get() = when (profile.transformationStage) {
            0 -> 400
            1 -> 950
            2 -> 1_800
            else -> Int.MAX_VALUE
        }

    val achievements: List<ShaktiAchievement>
        get() = listOf(
            ShaktiAchievement(
                id = "ach_first_108",
                title = "First Sacred Mala",
                subtitle = "Chant 108 real Naam Jap",
                unlocked = allTimeNaamCount >= 108
            ),
            ShaktiAchievement(
                id = "ach_spark_1000",
                title = "Spark of Shakti",
                subtitle = "Reach 1,000 real Naam Jap",
                unlocked = allTimeNaamCount >= 1_000
            ),
            ShaktiAchievement(
                id = "ach_10k_unlock",
                title = "10,000 Naam Awakening",
                subtitle = "Unlock the NAAM SHAKTI Battlefield",
                unlocked = profile.unlockedBattlefield
            ),
            ShaktiAchievement(
                id = "ach_first_win",
                title = "First Lightning Victory",
                subtitle = "Defeat your first storm enemy",
                unlocked = profile.battlesWon >= 1
            ),
            ShaktiAchievement(
                id = "ach_boss_slayer",
                title = "Kingdom Boss Slayer",
                subtitle = "Defeat Lord Meghnad (Mission 2)",
                unlocked = profile.highestMissionCleared >= 2
            ),
            ShaktiAchievement(
                id = "ach_transform",
                title = "Shakti Transformation",
                subtitle = "Awaken a Special Warrior Transformation",
                unlocked = profile.transformationStage >= 1
            ),
            ShaktiAchievement(
                id = "ach_warriors_all",
                title = "Pantheon Commander",
                subtitle = "Unlock 3+ Anime Warriors",
                unlocked = unlockedWarriorIds.size >= 3
            ),
            ShaktiAchievement(
                id = "ach_sovereign",
                title = "Eclipse Conqueror",
                subtitle = "Clear all 8 Fantasy Story Missions",
                unlocked = profile.highestMissionCleared >= 8
            )
        )
}

class NaamShaktiViewModel(
    private val appContext: Context,
    private val naamJapRepository: NaamJapRepository,
    private val naamShaktiDao: NaamShaktiDao,
    private val cloudRepository: NaamShaktiCloudRepository
) : ViewModel() {

    private val soundEngine = LightningSoundEngine()
    private val subScreenFlow = MutableStateFlow(NaamShaktiSubScreen.DASHBOARD)
    private val showUnlockCelebrationFlow = MutableStateFlow(false)
    private val activeBattleFlow = MutableStateFlow<ActiveBattleState?>(null)
    private val soundEnabledFlow = MutableStateFlow(true)
    private val cloudAuthFlow = MutableStateFlow(CloudAuthUiState())
    private val statusBannerFlow = MutableStateFlow<String?>(null)

    private var cloudListenersJob: Job? = null
    private var lastSyncedAllTimeInSession: Int = -1

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null && user.uid.isNotBlank()) {
            cloudAuthFlow.value = CloudAuthUiState(
                signedIn = true,
                userId = user.uid,
                displayName = user.displayName ?: "Warrior",
                email = user.email ?: "",
                authBusy = false,
                syncingNow = false,
                lastSyncLabel = "Cloud Backup Connected (${user.email ?: "Google"})"
            )
            attachCloudListenersForUser(user.uid)
        } else {
            cloudListenersJob?.cancel()
            cloudListenersJob = null
            cloudAuthFlow.value = CloudAuthUiState(
                signedIn = false,
                lastSyncLabel = "Sign in with Google to save Naam Jap & Shakti in Firebase"
            )
        }
    }

    private data class CoreLocalBundle(
        val profile: NaamShaktiProfileEntity,
        val allTimeNaam: Int,
        val naamHistory: List<NaamJapDayEntity>,
        val shaktiHistory: List<ShaktiHistoryEntity>
    )

    private data class UiControlBundle(
        val subScreen: NaamShaktiSubScreen,
        val showUnlock: Boolean,
        val battle: ActiveBattleState?,
        val soundOn: Boolean,
        val cloudAuth: CloudAuthUiState,
        val statusBanner: String?
    )

    private val coreLocalFlow = combine(
        naamShaktiDao.observeProfile(),
        naamJapRepository.allTimeTotal,
        naamJapRepository.allHistory,
        naamShaktiDao.observeHistory()
    ) { profile, allTime, naamHistory, shaktiHistory ->
        CoreLocalBundle(
            profile = profile ?: NaamShaktiProfileEntity(),
            allTimeNaam = allTime,
            naamHistory = naamHistory,
            shaktiHistory = shaktiHistory
        )
    }

    private val uiControlFlow = combine(
        subScreenFlow,
        showUnlockCelebrationFlow,
        activeBattleFlow,
        soundEnabledFlow,
        cloudAuthFlow
    ) { screen, unlock, battle, sound, auth ->
        UiControlBundle(screen, unlock, battle, sound, auth, null)
    }.combine(statusBannerFlow) { ctrl, banner ->
        ctrl.copy(statusBanner = banner)
    }

    val uiState: StateFlow<NaamShaktiUiState> = combine(
        coreLocalFlow,
        uiControlFlow
    ) { local, ctrl ->
        val todayKey = todayDateKey()
        val todayCount = local.naamHistory.firstOrNull { it.dateKey == todayKey }?.count ?: 0
        NaamShaktiUiState(
            profile = local.profile,
            allTimeNaamCount = local.allTimeNaam,
            todayNaamCount = todayCount,
            naamHistory = local.naamHistory,
            shaktiHistory = local.shaktiHistory,
            currentSubScreen = ctrl.subScreen,
            showUnlockCelebration = ctrl.showUnlock,
            activeBattle = ctrl.battle,
            soundEnabled = ctrl.soundOn,
            cloudAuth = ctrl.cloudAuth,
            statusBanner = ctrl.statusBanner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NaamShaktiUiState()
    )

    private val isFirebaseInitialized: Boolean
        get() = runCatching { FirebaseApp.getApps(appContext).isNotEmpty() }.getOrDefault(false)

    init {
        if (isFirebaseInitialized) {
            runCatching { Firebase.auth.addAuthStateListener(authStateListener) }
        } else {
            cloudAuthFlow.value = CloudAuthUiState(
                signedIn = false,
                lastSyncLabel = "Local Offline Storage Active (All Naam Jap & Shakti saved on device)"
            )
        }
        viewModelScope.launch {
            ensureProfileInitialized()
            observeRealNaamJapEnergyPipeline()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isFirebaseInitialized) {
            runCatching { Firebase.auth.removeAuthStateListener(authStateListener) }
        }
        cloudListenersJob?.cancel()
    }

    private suspend fun ensureProfileInitialized() {
        if (naamShaktiDao.getProfileOnce() == null) {
            naamShaktiDao.upsertProfile(NaamShaktiProfileEntity())
        }
    }

    /**
     * Core invariant: Naam Jap Counter is the ONLY source of Shakti Energy (1 Naam = 1 Shakti).
     * - Before 10,000 real Naam: battlefield stays locked.
     * - At 10,000 real Naam: battlefield unlocks & awards 10,000 Shakti Points.
     * - After unlock: every new real Naam adds +1 Shakti Point.
     * - During KO Recovery Mode: tracks 1,000 NEW real Naam above koBaselineAllTimeNaam to revive with 1,000 Shakti.
     */
    private suspend fun observeRealNaamJapEnergyPipeline() {
        naamJapRepository.allTimeTotal.collect { allTimeNaam ->
            val current = naamShaktiDao.getProfileOnce() ?: NaamShaktiProfileEntity()
            var updated = current
            var unlockJustTriggered = false
            var reviveJustTriggered = false
            var historyToLog: ShaktiHistoryEntity? = null

            if (!current.unlockedBattlefield) {
                if (allTimeNaam >= NaamShaktiRules.UNLOCK_NAAM_TARGET) {
                    val grantedShakti = allTimeNaam // 10,000+ real Naam = 10,000+ Shakti (1:1)
                    updated = current.copy(
                        unlockedBattlefield = true,
                        koRecoveryActive = false,
                        currentShaktiEnergy = grantedShakti,
                        lifetimeShaktiEarned = current.lifetimeShaktiEarned + grantedShakti,
                        syncedAllTimeNaam = allTimeNaam,
                        lastUpdatedEpoch = System.currentTimeMillis()
                    )
                    unlockJustTriggered = true
                    historyToLog = ShaktiHistoryEntity(
                        eventId = "unlock_${System.currentTimeMillis()}",
                        eventType = "NAAM_UNLOCK",
                        title = "Battlefield Unlocked ⚡",
                        subtitle = "10,000 real Naam Jap completed → +$grantedShakti Shakti Energy",
                        shaktiDelta = grantedShakti,
                        naamCountSnapshot = allTimeNaam,
                        warriorId = updated.selectedWarriorId
                    )
                } else if (current.syncedAllTimeNaam != allTimeNaam) {
                    updated = current.copy(
                        syncedAllTimeNaam = allTimeNaam,
                        lastUpdatedEpoch = System.currentTimeMillis()
                    )
                }
            } else if (current.koRecoveryActive) {
                val newNaamSinceKo = (allTimeNaam - current.koBaselineAllTimeNaam).coerceAtLeast(0)
                if (newNaamSinceKo >= NaamShaktiRules.REVIVE_NAAM_TARGET) {
                    updated = current.copy(
                        koRecoveryActive = false,
                        currentShaktiEnergy = newNaamSinceKo,
                        lifetimeShaktiEarned = current.lifetimeShaktiEarned + newNaamSinceKo,
                        syncedAllTimeNaam = allTimeNaam,
                        lastUpdatedEpoch = System.currentTimeMillis()
                    )
                    reviveJustTriggered = true
                    historyToLog = ShaktiHistoryEntity(
                        eventId = "revive_${System.currentTimeMillis()}",
                        eventType = "REVIVED",
                        title = "Warrior Revived ⚡",
                        subtitle = "$newNaamSinceKo new Naam Jap chanted → +$newNaamSinceKo Shakti restored",
                        shaktiDelta = newNaamSinceKo,
                        naamCountSnapshot = allTimeNaam,
                        warriorId = updated.selectedWarriorId
                    )
                }
            } else {
                val newDelta = allTimeNaam - current.syncedAllTimeNaam
                if (newDelta > 0) {
                    val newLifetime = current.lifetimeShaktiEarned + newDelta
                    val unlockedCsv = computeUnlockedWarriorsCsv(
                        current.unlockedWarriorsCsv,
                        current.battlesWon,
                        newLifetime
                    )
                    updated = current.copy(
                        currentShaktiEnergy = current.currentShaktiEnergy + newDelta,
                        lifetimeShaktiEarned = newLifetime,
                        syncedAllTimeNaam = allTimeNaam,
                        unlockedWarriorsCsv = unlockedCsv,
                        lastUpdatedEpoch = System.currentTimeMillis()
                    )
                }
            }

            if (updated != current) {
                naamShaktiDao.upsertProfile(updated)
                if (historyToLog != null) {
                    naamShaktiDao.insertHistory(historyToLog)
                }
                if (unlockJustTriggered) {
                    showUnlockCelebrationFlow.value = true
                    soundEngine.playVictoryChime()
                }
                if (reviveJustTriggered) {
                    soundEngine.playVictoryChime()
                    statusBannerFlow.value = "Anime Warrior Revived! +${NaamShaktiRules.REVIVE_NAAM_TARGET} Shakti Restored ⚡"
                }
                syncLocalStateToCloudIfSignedIn(updated, historyToLog, syncTodayNaam = true)
            } else if (allTimeNaam != lastSyncedAllTimeInSession && allTimeNaam > 0) {
                lastSyncedAllTimeInSession = allTimeNaam
                syncTodayNaamDayToCloudIfSignedIn()
            }
        }
    }

    private fun attachCloudListenersForUser(uid: String) {
        cloudListenersJob?.cancel()
        cloudListenersJob = viewModelScope.launch {
            launch {
                cloudRepository.observeCloudProfile(uid).collect { result ->
                    result.onSuccess { cloud ->
                        if (cloud != null) {
                            val local = naamShaktiDao.getProfileOnce() ?: NaamShaktiProfileEntity()
                            // Merge conservatively: preserve highest real progress across devices
                            if (cloud.lifetimeShaktiEarned > local.lifetimeShaktiEarned ||
                                cloud.battlesWon > local.battlesWon ||
                                cloud.highestMissionCleared > local.highestMissionCleared
                            ) {
                                val merged = local.copy(
                                    selectedWarriorId = cloud.selectedWarriorId.ifBlank { local.selectedWarriorId },
                                    unlockedWarriorsCsv = mergeCsv(local.unlockedWarriorsCsv, cloud.unlockedWarriorsCsv),
                                    unlockedBattlefield = local.unlockedBattlefield || cloud.unlockedBattlefield,
                                    koRecoveryActive = if (cloud.lifetimeShaktiEarned > local.lifetimeShaktiEarned) {
                                        cloud.koRecoveryActive
                                    } else local.koRecoveryActive,
                                    currentShaktiEnergy = maxOf(local.currentShaktiEnergy, cloud.currentShaktiEnergy),
                                    lifetimeShaktiEarned = maxOf(local.lifetimeShaktiEarned, cloud.lifetimeShaktiEarned),
                                    lifetimeShaktiSpent = maxOf(local.lifetimeShaktiSpent, cloud.lifetimeShaktiSpent),
                                    syncedAllTimeNaam = maxOf(local.syncedAllTimeNaam, cloud.syncedAllTimeNaam),
                                    koBaselineAllTimeNaam = maxOf(local.koBaselineAllTimeNaam, cloud.koBaselineAllTimeNaam),
                                    battlesWon = maxOf(local.battlesWon, cloud.battlesWon),
                                    battlesPlayed = maxOf(local.battlesPlayed, cloud.battlesPlayed),
                                    highestMissionCleared = maxOf(local.highestMissionCleared, cloud.highestMissionCleared),
                                    attackLevel = maxOf(local.attackLevel, cloud.attackLevel),
                                    defenseLevel = maxOf(local.defenseLevel, cloud.defenseLevel),
                                    auraLevel = maxOf(local.auraLevel, cloud.auraLevel),
                                    transformationStage = maxOf(local.transformationStage, cloud.transformationStage),
                                    warriorXp = maxOf(local.warriorXp, cloud.warriorXp)
                                )
                                naamShaktiDao.upsertProfile(merged)
                            } else {
                                // Push current local state up to cloud on first connect
                                cloudRepository.upsertProfile(local)
                            }
                            cloudAuthFlow.value = cloudAuthFlow.value.copy(
                                lastSyncLabel = "Synced with Firebase Cloud"
                            )
                        } else {
                            val local = naamShaktiDao.getProfileOnce() ?: NaamShaktiProfileEntity()
                            cloudRepository.upsertProfile(local)
                            syncTodayNaamDayToCloudIfSignedIn()
                        }
                    }
                }
            }
            launch {
                cloudRepository.observeCloudHistory(uid).collect { result ->
                    result.onSuccess { events ->
                        events.forEach { ev ->
                            val id = ev.id.ifBlank { "cloud_${ev.createdAt?.seconds ?: 0}_${ev.eventType}" }
                            naamShaktiDao.insertHistory(
                                ShaktiHistoryEntity(
                                    eventId = id,
                                    eventType = ev.eventType,
                                    title = ev.title,
                                    subtitle = ev.subtitle,
                                    shaktiDelta = ev.shaktiDelta,
                                    naamCountSnapshot = ev.naamCountSnapshot,
                                    warriorId = ev.warriorId,
                                    createdEpoch = (ev.createdAt?.seconds ?: (System.currentTimeMillis() / 1000)) * 1000L
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun syncLocalStateToCloudIfSignedIn(
        profile: NaamShaktiProfileEntity,
        event: ShaktiHistoryEntity? = null,
        syncTodayNaam: Boolean = false
    ) {
        if (cloudRepository.currentUserIdOrNull() == null) return
        viewModelScope.launch {
            cloudAuthFlow.value = cloudAuthFlow.value.copy(syncingNow = true)
            cloudRepository.upsertProfile(profile)
            if (event != null) {
                cloudRepository.appendHistoryEvent(event)
            }
            if (syncTodayNaam) {
                syncTodayNaamDayToCloudIfSignedIn()
            }
            cloudAuthFlow.value = cloudAuthFlow.value.copy(
                syncingNow = false,
                lastSyncLabel = "Cloud Backup Synced"
            )
        }
    }

    private suspend fun syncTodayNaamDayToCloudIfSignedIn() {
        if (cloudRepository.currentUserIdOrNull() == null) return
        val history = naamJapRepository.allHistory.first()
        val today = history.firstOrNull { it.dateKey == todayDateKey() } ?: return
        cloudRepository.syncNaamJapDay(today)
    }

    fun attemptSilentSignIn(activityContext: Context) {
        viewModelScope.launch {
            if (!isFirebaseInitialized) return@launch
            if (runCatching { Firebase.auth.currentUser }.getOrNull() != null) return@launch
            GoogleAuthHelper.attemptAutoSignIn(activityContext)
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            cloudAuthFlow.value = cloudAuthFlow.value.copy(authBusy = true, errorMessage = null)
            when (val result = GoogleAuthHelper.signInWithGoogle(activityContext)) {
                is GoogleAuthResult.Success -> {
                    statusBannerFlow.value = "Google Cloud Sync Enabled for ${result.user.email ?: "Warrior"}"
                }
                GoogleAuthResult.Cancelled -> {
                    cloudAuthFlow.value = cloudAuthFlow.value.copy(authBusy = false)
                }
                GoogleAuthResult.NoSavedCredential -> {
                    cloudAuthFlow.value = cloudAuthFlow.value.copy(
                        authBusy = false,
                        errorMessage = "No Google account found on device."
                    )
                }
                is GoogleAuthResult.Failure -> {
                    cloudAuthFlow.value = cloudAuthFlow.value.copy(
                        authBusy = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            GoogleAuthHelper.signOut(appContext)
            statusBannerFlow.value = "Signed out of Cloud Sync (local progress remains safe)"
        }
    }

    fun selectSubScreen(screen: NaamShaktiSubScreen) {
        subScreenFlow.value = screen
    }

    fun dismissUnlockCelebration(goToBattle: Boolean) {
        showUnlockCelebrationFlow.value = false
        if (goToBattle) {
            subScreenFlow.value = NaamShaktiSubScreen.BATTLE
        }
    }

    fun toggleSound() {
        val next = !soundEnabledFlow.value
        soundEnabledFlow.value = next
        soundEngine.soundEnabled = next
    }

    fun clearStatusBanner() {
        statusBannerFlow.value = null
    }

    fun selectWarrior(warriorId: String) {
        viewModelScope.launch {
            val state = uiState.value
            if (!state.unlockedWarriorIds.contains(warriorId)) {
                statusBannerFlow.value = "Complete battle or Shakti milestones to unlock this warrior."
                return@launch
            }
            val updated = state.profile.copy(
                selectedWarriorId = warriorId,
                lastUpdatedEpoch = System.currentTimeMillis()
            )
            naamShaktiDao.upsertProfile(updated)
            syncLocalStateToCloudIfSignedIn(updated)
            statusBannerFlow.value = "${NaamShaktiCatalog.warriorById(warriorId).name} selected ⚡"
        }
    }

    fun startMissionBattle(mission: StoryMission) {
        val state = uiState.value
        if (!state.profile.unlockedBattlefield) {
            statusBannerFlow.value = "Battlefield locked! Complete 10,000 Naam Jap first."
            return
        }
        if (state.profile.koRecoveryActive || state.profile.currentShaktiEnergy <= 0) {
            statusBannerFlow.value = "Anime KO! Chant 1,000 new Naam Jap in Recovery Mode to revive."
            return
        }
        val maxHp = state.warriorMaxHealth
        activeBattleFlow.value = ActiveBattleState(
            mission = mission,
            warriorHp = maxHp,
            warriorMaxHp = maxHp,
            warriorShield = 0,
            dodgeReady = false,
            enemyHp = mission.enemyMaxHp,
            enemyMaxHp = mission.enemyMaxHp,
            turnNumber = 1,
            shaktiSpentThisBattle = 0,
            battleLog = "${mission.enemyName} appears in ${mission.arenaName}! Strike with Shakti ⚡"
        )
        subScreenFlow.value = NaamShaktiSubScreen.BATTLE
    }

    fun exitActiveBattle() {
        activeBattleFlow.value = null
    }

    fun castLightningAbility(ability: LightningAbility) {
        viewModelScope.launch {
            val state = uiState.value
            val battle = state.activeBattle ?: return@launch
            if (battle.victoryAchieved || battle.retreatedByHp) return@launch
            if (state.profile.koRecoveryActive || state.profile.currentShaktiEnergy <= 0) {
                activeBattleFlow.value = null
                return@launch
            }
            if (state.profile.currentShaktiEnergy < ability.shaktiCost) {
                statusBannerFlow.value = "Need ${ability.shaktiCost} Shakti for ${ability.title}. Chant real Naam Jap to charge!"
                return@launch
            }

            soundEngine.playAbilitySound(ability)

            val warrior = state.selectedWarrior
            val atkScale = warrior.attackPowerMultiplier *
                (1f + (state.profile.attackLevel - 1) * 0.10f) *
                state.transformationMultiplier
            val shieldScale = warrior.shieldMultiplier *
                (1f + (state.profile.defenseLevel - 1) * 0.12f) *
                (1f + (state.profile.auraLevel - 1) * 0.05f)

            val dealtDamage = (ability.baseDamage * atkScale).toInt()
            val gainedShield = (ability.shieldGain * shieldScale).toInt()

            val remainingShakti = (state.profile.currentShaktiEnergy - ability.shaktiCost).coerceAtLeast(0)
            val newEnemyHp = (battle.enemyHp - dealtDamage).coerceAtLeast(0)
            val newShield = battle.warriorShield + gainedShield
            val newDodge = battle.dodgeReady || ability.grantsDodge
            val spentTotal = battle.shaktiSpentThisBattle + ability.shaktiCost

            val now = System.currentTimeMillis()
            val abilityEvent = ShaktiHistoryEntity(
                eventId = "atk_${UUID.randomUUID().toString().take(12)}",
                eventType = "ABILITY_SPENT",
                title = "${ability.title} ⚡",
                subtitle = "${warrior.name} vs ${battle.mission.enemyName} (${if (dealtDamage > 0) "$dealtDamage DMG" else "+$gainedShield Shield"})",
                shaktiDelta = -ability.shaktiCost,
                naamCountSnapshot = state.allTimeNaamCount,
                warriorId = warrior.id
            )
            naamShaktiDao.insertHistory(abilityEvent)

            // Check Victory first
            if (newEnemyHp <= 0) {
                val newWins = state.profile.battlesWon + 1
                val newPlayed = state.profile.battlesPlayed + 1
                val newHighest = maxOf(state.profile.highestMissionCleared, battle.mission.index)
                val newXp = state.profile.warriorXp + battle.mission.xpReward
                val newUnlockedCsv = computeUnlockedWarriorsCsv(
                    state.profile.unlockedWarriorsCsv,
                    newWins,
                    state.profile.lifetimeShaktiEarned
                )
                val enteredKo = remainingShakti == 0
                val updatedProfile = state.profile.copy(
                    currentShaktiEnergy = remainingShakti,
                    lifetimeShaktiSpent = state.profile.lifetimeShaktiSpent + ability.shaktiCost,
                    koRecoveryActive = enteredKo,
                    koBaselineAllTimeNaam = if (enteredKo) state.allTimeNaamCount else state.profile.koBaselineAllTimeNaam,
                    battlesWon = newWins,
                    battlesPlayed = newPlayed,
                    highestMissionCleared = newHighest,
                    warriorXp = newXp,
                    unlockedWarriorsCsv = newUnlockedCsv,
                    lastUpdatedEpoch = now
                )
                naamShaktiDao.upsertProfile(updatedProfile)
                val winEvent = ShaktiHistoryEntity(
                    eventId = "win_${UUID.randomUUID().toString().take(12)}",
                    eventType = "BATTLE_WIN",
                    title = "Victory: ${battle.mission.enemyName}",
                    subtitle = "${battle.mission.kingdomName} cleared · +${battle.mission.xpReward} Warrior XP",
                    shaktiDelta = 0,
                    naamCountSnapshot = state.allTimeNaamCount,
                    warriorId = warrior.id
                )
                naamShaktiDao.insertHistory(winEvent)
                soundEngine.playVictoryChime()

                activeBattleFlow.value = battle.copy(
                    enemyHp = 0,
                    warriorShield = newShield,
                    shaktiSpentThisBattle = spentTotal,
                    lastAbilityUsed = ability,
                    strikeEventId = now,
                    victoryAchieved = true,
                    battleLog = "VICTORY! ${battle.mission.enemyName} defeated! +${battle.mission.xpReward} XP ⚡"
                )
                syncLocalStateToCloudIfSignedIn(updatedProfile, winEvent)
                return@launch
            }

            // Check Power Depletion -> Anime KO when Shakti Energy hits ZERO
            if (remainingShakti == 0) {
                val updatedProfile = state.profile.copy(
                    currentShaktiEnergy = 0,
                    lifetimeShaktiSpent = state.profile.lifetimeShaktiSpent + ability.shaktiCost,
                    koRecoveryActive = true,
                    koBaselineAllTimeNaam = state.allTimeNaamCount,
                    battlesPlayed = state.profile.battlesPlayed + 1,
                    lastUpdatedEpoch = now
                )
                naamShaktiDao.upsertProfile(updatedProfile)
                val koEvent = ShaktiHistoryEntity(
                    eventId = "ko_${UUID.randomUUID().toString().take(12)}",
                    eventType = "KO_ENTERED",
                    title = "Anime KO — Recovery Mode",
                    subtitle = "Shakti Energy reached 0. Chant 1,000 new Naam Jap to revive.",
                    shaktiDelta = 0,
                    naamCountSnapshot = state.allTimeNaamCount,
                    warriorId = warrior.id
                )
                naamShaktiDao.insertHistory(koEvent)
                soundEngine.playKoTone()
                activeBattleFlow.value = null
                statusBannerFlow.value = "Shakti Energy Depleted! Enter Recovery Mode: 1,000 new Naam Jap to revive."
                syncLocalStateToCloudIfSignedIn(updatedProfile, koEvent)
                return@launch
            }

            // Persist Shakti spend immediately
            val midProfile = state.profile.copy(
                currentShaktiEnergy = remainingShakti,
                lifetimeShaktiSpent = state.profile.lifetimeShaktiSpent + ability.shaktiCost,
                lastUpdatedEpoch = now
            )
            naamShaktiDao.upsertProfile(midProfile)
            syncLocalStateToCloudIfSignedIn(midProfile, abilityEvent)

            // Show player strike immediately
            val afterPlayerStrike = battle.copy(
                enemyHp = newEnemyHp,
                warriorShield = newShield,
                dodgeReady = newDodge,
                shaktiSpentThisBattle = spentTotal,
                lastAbilityUsed = ability,
                strikeEventId = now,
                battleLog = if (dealtDamage > 0) {
                    "${ability.title} struck for $dealtDamage lightning DMG!"
                } else {
                    "Energy Shield raised (+$gainedShield barrier)!"
                }
            )
            activeBattleFlow.value = afterPlayerStrike

            // Brief cinematic enemy counterattack beat
            delay(360L)
            val currentBattle = activeBattleFlow.value ?: return@launch
            if (currentBattle.victoryAchieved || currentBattle.mission.index != battle.mission.index) return@launch

            if (currentBattle.dodgeReady) {
                activeBattleFlow.value = currentBattle.copy(
                    dodgeReady = false,
                    turnNumber = currentBattle.turnNumber + 1,
                    battleLog = "Lightning Dash evaded ${battle.mission.enemyName}'s strike!"
                )
            } else {
                val incomingDmg = battle.mission.enemyAttack
                val absorbed = minOf(currentBattle.warriorShield, incomingDmg)
                val hpLoss = (incomingDmg - absorbed).coerceAtLeast(0)
                val nextShield = (currentBattle.warriorShield - absorbed).coerceAtLeast(0)
                val nextHp = (currentBattle.warriorHp - hpLoss).coerceAtLeast(0)
                val retreated = nextHp <= 0

                activeBattleFlow.value = currentBattle.copy(
                    warriorHp = nextHp,
                    warriorShield = nextShield,
                    turnNumber = currentBattle.turnNumber + 1,
                    enemyHitEventId = System.currentTimeMillis(),
                    retreatedByHp = retreated,
                    battleLog = if (retreated) {
                        "${battle.mission.enemyName} overwhelmed your guard! Regroup & upgrade Shield."
                    } else if (absorbed > 0) {
                        "Shield absorbed $absorbed DMG; took $hpLoss HP from ${battle.mission.enemyName}."
                    } else {
                        "${battle.mission.enemyName} countered for $hpLoss DMG!"
                    }
                )
            }
        }
    }

    /**
     * Allows a warrior with 1..49 remaining Shakti (below the cheapest 50-cost ability)
     * to unleash their final spark so Shakti cleanly reaches 0 and enters KO Recovery Mode
     * instead of getting stuck.
     */
    fun releaseFinalSparkBelow50() {
        viewModelScope.launch {
            val state = uiState.value
            val remaining = state.profile.currentShaktiEnergy
            if (remaining !in 1..49) return@launch
            val now = System.currentTimeMillis()
            val updated = state.profile.copy(
                currentShaktiEnergy = 0,
                lifetimeShaktiSpent = state.profile.lifetimeShaktiSpent + remaining,
                koRecoveryActive = true,
                koBaselineAllTimeNaam = state.allTimeNaamCount,
                lastUpdatedEpoch = now
            )
            naamShaktiDao.upsertProfile(updated)
            val koEvent = ShaktiHistoryEntity(
                eventId = "ko_spark_${UUID.randomUUID().toString().take(10)}",
                eventType = "KO_ENTERED",
                title = "Final Spark Spent — Recovery Mode",
                subtitle = "Spent final $remaining Shakti. Chant 1,000 new Naam Jap to revive.",
                shaktiDelta = -remaining,
                naamCountSnapshot = state.allTimeNaamCount,
                warriorId = updated.selectedWarriorId
            )
            naamShaktiDao.insertHistory(koEvent)
            soundEngine.playKoTone()
            activeBattleFlow.value = null
            syncLocalStateToCloudIfSignedIn(updated, koEvent)
        }
    }

    fun upgradeWarriorStat(stat: String) {
        viewModelScope.launch {
            val state = uiState.value
            val cost = state.nextUpgradeCostXp
            if (state.profile.warriorXp < cost) {
                statusBannerFlow.value = "Need $cost Warrior XP (win battles to earn XP)."
                return@launch
            }
            val updated = when (stat) {
                "ATTACK" -> state.profile.copy(
                    attackLevel = (state.profile.attackLevel + 1).coerceAtMost(50),
                    warriorXp = state.profile.warriorXp - cost
                )
                "DEFENSE" -> state.profile.copy(
                    defenseLevel = (state.profile.defenseLevel + 1).coerceAtMost(50),
                    warriorXp = state.profile.warriorXp - cost
                )
                else -> state.profile.copy(
                    auraLevel = (state.profile.auraLevel + 1).coerceAtMost(50),
                    warriorXp = state.profile.warriorXp - cost
                )
            }.copy(lastUpdatedEpoch = System.currentTimeMillis())

            naamShaktiDao.upsertProfile(updated)
            val upEvent = ShaktiHistoryEntity(
                eventId = "upg_${UUID.randomUUID().toString().take(10)}",
                eventType = "UPGRADE",
                title = "$stat Upgraded ⚡",
                subtitle = "Spent $cost Battle XP to power up ${state.selectedWarrior.name}",
                shaktiDelta = 0,
                naamCountSnapshot = state.allTimeNaamCount,
                warriorId = updated.selectedWarriorId
            )
            naamShaktiDao.insertHistory(upEvent)
            soundEngine.playVictoryChime()
            statusBannerFlow.value = "$stat upgraded! Your lightning abilities hit harder ⚡"
            syncLocalStateToCloudIfSignedIn(updated, upEvent)
        }
    }

    fun awakenTransformation() {
        viewModelScope.launch {
            val state = uiState.value
            if (state.profile.transformationStage >= 3) {
                statusBannerFlow.value = "Maximum Sovereign Transformation already awakened!"
                return@launch
            }
            val cost = state.nextTransformationCostXp
            if (state.profile.warriorXp < cost) {
                statusBannerFlow.value = "Need $cost Warrior XP to awaken next Transformation."
                return@launch
            }
            val nextStage = state.profile.transformationStage + 1
            val formName = state.selectedWarrior.transformations.getOrElse(nextStage - 1) { "Ascendant" }
            val updated = state.profile.copy(
                transformationStage = nextStage,
                warriorXp = state.profile.warriorXp - cost,
                lastUpdatedEpoch = System.currentTimeMillis()
            )
            naamShaktiDao.upsertProfile(updated)
            val ev = ShaktiHistoryEntity(
                eventId = "tr_${UUID.randomUUID().toString().take(10)}",
                eventType = "UPGRADE",
                title = "Transformation: $formName ⚡",
                subtitle = "${state.selectedWarrior.name} ascended to Stage $nextStage (+16% Lightning Power)",
                shaktiDelta = 0,
                naamCountSnapshot = state.allTimeNaamCount,
                warriorId = updated.selectedWarriorId
            )
            naamShaktiDao.insertHistory(ev)
            soundEngine.playVictoryChime()
            statusBannerFlow.value = "AWAKENED: $formName! ⚡"
            syncLocalStateToCloudIfSignedIn(updated, ev)
        }
    }

    private fun computeUnlockedWarriorsCsv(existingCsv: String, wins: Int, lifetimeShakti: Int): String {
        val current = existingCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        NaamShaktiCatalog.warriors.forEach { w ->
            if (wins >= w.requiredWins || lifetimeShakti >= w.requiredLifetimeShakti) {
                current.add(w.id)
            }
        }
        return current.joinToString(",")
    }

    private fun mergeCsv(a: String, b: String): String =
        (a.split(",") + b.split(","))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
            .joinToString(",")

    class Factory(
        private val context: Context,
        private val naamJapRepository: NaamJapRepository,
        private val naamShaktiDao: NaamShaktiDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NaamShaktiViewModel(
                appContext = context.applicationContext,
                naamJapRepository = naamJapRepository,
                naamShaktiDao = naamShaktiDao,
                cloudRepository = NaamShaktiCloudRepository(context.applicationContext)
            ) as T
        }
    }
}
