package com.example.naamshakti

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

object NaamShaktiRules {
    const val UNLOCK_NAAM_TARGET: Int = 10_000
    const val REVIVE_NAAM_TARGET: Int = 1_000
}

enum class LightningAbility(
    val id: String,
    val title: String,
    val shortName: String,
    val shaktiCost: Int,
    val baseDamage: Int,
    val shieldGain: Int,
    val grantsDodge: Boolean,
    val subtitle: String
) {
    LIGHTNING_ATTACK(
        id = "lightning_attack",
        title = "Lightning Attack",
        shortName = "Bolt Slash",
        shaktiCost = 50,
        baseDamage = 140,
        shieldGain = 0,
        grantsDodge = false,
        subtitle = "Fast crimson-white electric slash"
    ),
    THUNDER_STRIKE(
        id = "thunder_strike",
        title = "Thunder Strike",
        shortName = "Thunder",
        shaktiCost = 100,
        baseDamage = 320,
        shieldGain = 0,
        grantsDodge = false,
        subtitle = "Sky-splitting high-voltage bolt"
    ),
    ENERGY_SHIELD(
        id = "energy_shield",
        title = "Energy Shield",
        shortName = "Shield",
        shaktiCost = 150,
        baseDamage = 0,
        shieldGain = 340,
        grantsDodge = false,
        subtitle = "Sacred Shakti barrier absorbing damage"
    ),
    LIGHTNING_DASH(
        id = "lightning_dash",
        title = "Lightning Dash",
        shortName = "Volt Dash",
        shaktiCost = 200,
        baseDamage = 440,
        shieldGain = 80,
        grantsDodge = true,
        subtitle = "Blink strike that dodges next counter"
    ),
    MEGA_THUNDER(
        id = "mega_thunder",
        title = "Mega Thunder",
        shortName = "Mega Storm",
        shaktiCost = 500,
        baseDamage = 1_220,
        shieldGain = 0,
        grantsDodge = false,
        subtitle = "Multi-bolt storm cataclysm"
    ),
    ULTIMATE_POWER(
        id = "ultimate_power",
        title = "Ultimate Power",
        shortName = "Vajra Nova",
        shaktiCost = 1_000,
        baseDamage = 2_750,
        shieldGain = 220,
        grantsDodge = true,
        subtitle = "Supreme anime finisher + aura shield"
    )
}

data class AnimeWarrior(
    val id: String,
    val name: String,
    val title: String,
    val realmOrigin: String,
    val baseHealth: Int,
    val attackPowerMultiplier: Float,
    val shieldMultiplier: Float,
    val specialPowerName: String,
    val ultimateName: String,
    val transformations: List<String>,
    val unlockRequirementText: String,
    val requiredWins: Int,
    val requiredLifetimeShakti: Int,
    @DrawableRes val portraitRes: Int,
    val accentHex: Long
)

data class StoryMission(
    val index: Int,
    val kingdomName: String,
    val arenaName: String,
    val missionTitle: String,
    val enemyName: String,
    val enemyTitle: String,
    val isBoss: Boolean,
    val enemyMaxHp: Int,
    val enemyAttack: Int,
    val xpReward: Int,
    val loreLine: String
)

data class ShaktiAchievement(
    val id: String,
    val title: String,
    val subtitle: String,
    val unlocked: Boolean
)

object NaamShaktiCatalog {
    val warriors: List<AnimeWarrior> = listOf(
        AnimeWarrior(
            id = "vajra_arjun",
            name = "Vajra Arjun",
            title = "Crimson Storm Swordsman",
            realmOrigin = "Obsidian Storm Citadel",
            baseHealth = 1_200,
            attackPowerMultiplier = 1.05f,
            shieldMultiplier = 1.0f,
            specialPowerName = "Crimson Rail Slash",
            ultimateName = "Sovereign Vajra Nova",
            transformations = listOf("Awakened Spark", "Crimson Tempest", "Vajra Overdrive"),
            unlockRequirementText = "Starter Warrior (Unlocked)",
            requiredWins = 0,
            requiredLifetimeShakti = 0,
            portraitRes = R.drawable.img_warrior_vajra_1795308862804,
            accentHex = 0xFFE51932
        ),
        AnimeWarrior(
            id = "rudra_sen",
            name = "Rudra Sen",
            title = "Sacred Mala Thunder Sage",
            realmOrigin = "Celestial Vajra Throne",
            baseHealth = 1_450,
            attackPowerMultiplier = 1.0f,
            shieldMultiplier = 1.30f,
            specialPowerName = "108-Bead Volt Halo",
            ultimateName = "Rudra Cosmic Storm",
            transformations = listOf("Mala Resonance", "White Lightning Sage", "Mahashakti Ascendant"),
            unlockRequirementText = "Win 2 Battles OR 12,000 Shakti",
            requiredWins = 2,
            requiredLifetimeShakti = 12_000,
            portraitRes = R.drawable.img_warrior_rudra_1795308860327,
            accentHex = 0xFFFFFFFF
        ),
        AnimeWarrior(
            id = "agni_kavi",
            name = "Agni Kavi",
            title = "Blazing Volt Striker",
            realmOrigin = "Crimson Thunder Spire",
            baseHealth = 1_300,
            attackPowerMultiplier = 1.25f,
            shieldMultiplier = 1.05f,
            specialPowerName = "Plasma Surge Flash",
            ultimateName = "Inferno Thunder Cataclysm",
            transformations = listOf("Red Spark", "Solar Volt Form", "Crimson Eclipse"),
            unlockRequirementText = "Win 4 Battles OR 18,000 Shakti",
            requiredWins = 4,
            requiredLifetimeShakti = 18_000,
            portraitRes = R.drawable.img_warrior_vajra_1795308862804,
            accentHex = 0xFFFF5252
        ),
        AnimeWarrior(
            id = "kala_indra",
            name = "Kala Indra",
            title = "Sovereign Lightning Emperor",
            realmOrigin = "Shattered Void Sanctum",
            baseHealth = 1_650,
            attackPowerMultiplier = 1.35f,
            shieldMultiplier = 1.35f,
            specialPowerName = "Void Breaker Bolt",
            ultimateName = "Infinite Naam Judgement",
            transformations = listOf("Storm Crown", "Astral Thunder Form", "Supreme Shakti Avatar"),
            unlockRequirementText = "Win 7 Battles OR 25,000 Shakti",
            requiredWins = 7,
            requiredLifetimeShakti = 25_000,
            portraitRes = R.drawable.img_warrior_rudra_1795308860327,
            accentHex = 0xFFFFD54F
        )
    )

    val missions: List<StoryMission> = listOf(
        StoryMission(
            index = 1,
            kingdomName = "Obsidian Storm Citadel",
            arenaName = "Gate of Sparks",
            missionTitle = "Awaken the Lightning",
            enemyName = "Volt Stalker",
            enemyTitle = "Storm Sentinel",
            isBoss = false,
            enemyMaxHp = 950,
            enemyAttack = 110,
            xpReward = 180,
            loreLine = "Channel your first 10,000 Naam Shakti to shatter the gatekeeper."
        ),
        StoryMission(
            index = 2,
            kingdomName = "Obsidian Storm Citadel",
            arenaName = "Thunder Sanctum",
            missionTitle = "Citadel Boss Clash",
            enemyName = "Lord Meghnad",
            enemyTitle = "KINGDOM BOSS",
            isBoss = true,
            enemyMaxHp = 1_900,
            enemyAttack = 175,
            xpReward = 420,
            loreLine = "Cloud-forged overlord wielding twin black-lightning spears."
        ),
        StoryMission(
            index = 3,
            kingdomName = "Crimson Thunder Spire",
            arenaName = "Skybridge of Ash",
            missionTitle = "Blades of Voltage",
            enemyName = "Plasma Ronin",
            enemyTitle = "Crimson Duelist",
            isBoss = false,
            enemyMaxHp = 2_400,
            enemyAttack = 215,
            xpReward = 500,
            loreLine = "A rogue samurai powered by stolen crimson storms."
        ),
        StoryMission(
            index = 4,
            kingdomName = "Crimson Thunder Spire",
            arenaName = "Crown of Storms",
            missionTitle = "Spire Sovereign",
            enemyName = "Empress Vidyut",
            enemyTitle = "SPIRE BOSS",
            isBoss = true,
            enemyMaxHp = 3_600,
            enemyAttack = 265,
            xpReward = 820,
            loreLine = "Commands hurricanes of scarlet thunder across the spire."
        ),
        StoryMission(
            index = 5,
            kingdomName = "Shattered Void Sanctum",
            arenaName = "Abyssal Ruin",
            missionTitle = "Through the Dark Rift",
            enemyName = "Abyssal Stormguard",
            enemyTitle = "Void Knight",
            isBoss = false,
            enemyMaxHp = 4_400,
            enemyAttack = 310,
            xpReward = 950,
            loreLine = "Armored in shadow; use Energy Shield & Dash wisely."
        ),
        StoryMission(
            index = 6,
            kingdomName = "Shattered Void Sanctum",
            arenaName = "Altar of Eclipse",
            missionTitle = "Titan of Darkness",
            enemyName = "Demon King Vritra",
            enemyTitle = "VOID BOSS",
            isBoss = true,
            enemyMaxHp = 6_200,
            enemyAttack = 370,
            xpReward = 1_400,
            loreLine = "Ancient devourer of thunder; unleash Mega Thunder & Ultimate Power."
        ),
        StoryMission(
            index = 7,
            kingdomName = "Celestial Vajra Throne",
            arenaName = "Astral Steps",
            missionTitle = "Trial of Discipline",
            enemyName = "Astral Warden",
            enemyTitle = "Celestial Guardian",
            isBoss = false,
            enemyMaxHp = 7_500,
            enemyAttack = 420,
            xpReward = 1_650,
            loreLine = "Only unwavering Naam Jap discipline survives the Astral Steps."
        ),
        StoryMission(
            index = 8,
            kingdomName = "Celestial Vajra Throne",
            arenaName = "Throne of Infinite Thunder",
            missionTitle = "Final Sovereign Trial",
            enemyName = "Eclipse Sovereign",
            enemyTitle = "FINAL MYTHIC BOSS",
            isBoss = true,
            enemyMaxHp = 10_500,
            enemyAttack = 490,
            xpReward = 2_800,
            loreLine = "The ultimate test of pure Shakti mastery and strategy."
        )
    )

    fun warriorById(id: String): AnimeWarrior =
        warriors.firstOrNull { it.id == id } ?: warriors.first()
}

// ---------- Firestore POJO Models (No is-prefixed Booleans!) ----------

@Keep
@IgnoreExtraProperties
data class FirestoreNaamShaktiProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val selectedWarriorId: String = "vajra_arjun",
    val unlockedWarriorsCsv: String = "vajra_arjun",
    val unlockedBattlefield: Boolean = false,
    val koRecoveryActive: Boolean = false,
    val currentShaktiEnergy: Int = 0,
    val lifetimeShaktiEarned: Int = 0,
    val lifetimeShaktiSpent: Int = 0,
    val syncedAllTimeNaam: Int = 0,
    val koBaselineAllTimeNaam: Int = 0,
    val battlesWon: Int = 0,
    val battlesPlayed: Int = 0,
    val highestMissionCleared: Int = 0,
    val attackLevel: Int = 1,
    val defenseLevel: Int = 1,
    val auraLevel: Int = 1,
    val transformationStage: Int = 0,
    val warriorXp: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FirestoreNaamJapDay(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val displayDate: String = "",
    val dayName: String = "",
    val count: Int = 0,
    val goalAtDay: Int = 10_000,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FirestoreShaktiHistoryEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventType: String = "NAAM_EARN",
    val title: String = "",
    val subtitle: String = "",
    val shaktiDelta: Int = 0,
    val naamCountSnapshot: Int = 0,
    val warriorId: String = "vajra_arjun",
    @ServerTimestamp val createdAt: Timestamp? = null
)
