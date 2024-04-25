package net.arvandor.numinoustreasury.profession

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Service
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import org.jooq.DSLContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import kotlin.math.min

class NuminousProfessionService(
    private val plugin: NuminousTreasury,
    private val repository: NuminousCharacterProfessionRepository,
) : Service {
    val professions: List<NuminousProfession>
    private val characterProfessions: MutableMap<Int, String>
    private val characterProfessionExperience: MutableMap<Int, Int>

    init {
        val professionsFolder = File(plugin.dataFolder, "professions")
        if (!professionsFolder.exists()) {
            professionsFolder.mkdirs()
            saveDefaultProfessions(professionsFolder)
        }
        this.professions =
            professionsFolder.listFiles().mapNotNull { professionFile ->
                val itemConfiguration =
                    YamlConfiguration.loadConfiguration(
                        professionFile,
                    )
                val profession = itemConfiguration.getObject("profession", NuminousProfession::class.java)
                if (profession == null) {
                    plugin.logger.warning("Failed to load profession from ${professionFile.name}")
                    return@mapNotNull null
                }
                return@mapNotNull profession
            }
        plugin.logger.info("Loaded " + professions.size + " professions")
        this.characterProfessions = ConcurrentHashMap()
        this.characterProfessionExperience = ConcurrentHashMap()
    }

    fun getProfessionById(id: String): NuminousProfession? {
        return professions.singleOrNull { profession -> profession.id == id }
    }

    fun getProfessionByName(name: String): NuminousProfession? {
        return professions.singleOrNull { profession ->
            profession.name.equals(
                name,
                ignoreCase = true,
            )
        }
    }

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }

    fun getProfession(player: OfflinePlayer): NuminousProfession? {
        val character = getRpkCharacter(player) ?: return null
        return getProfession(character)
    }

    fun getProfession(character: RPKCharacter): NuminousProfession? {
        val professionId = characterProfessions[character.id.value] ?: return null
        return getProfessionById(professionId)
    }

    fun setProfession(
        player: OfflinePlayer,
        profession: NuminousProfession,
        callback: Runnable? = null,
    ) {
        val character = getRpkCharacter(player) ?: return
        setProfession(character, profession, callback)
    }

    fun setProfession(
        character: RPKCharacter,
        profession: NuminousProfession,
        callback: Runnable? = null,
    ) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                repository.setProfessionId(character.id, profession.id)
                characterProfessions[character.id.value] = profession.id
                characterProfessionExperience[character.id.value] = 0
                if (callback != null) {
                    plugin.server.scheduler.runTask(plugin, callback)
                }
            },
        )
    }

    fun getProfessionLevel(player: OfflinePlayer): Int {
        val character = getRpkCharacter(player) ?: return 0
        return getProfessionLevel(character)
    }

    fun getProfessionLevel(character: RPKCharacter): Int {
        return getLevelAtExperience(getProfessionExperience(character))
    }

    fun getProfessionExperience(player: OfflinePlayer): Int {
        val character = getRpkCharacter(player) ?: return 0
        return getProfessionExperience(character)
    }

    fun setProfessionExperience(
        player: OfflinePlayer,
        experience: Int,
        callback: Runnable? = null,
    ) {
        val character = getRpkCharacter(player) ?: return
        setProfessionExperience(character, experience, callback)
    }

    fun addProfessionExperience(
        player: OfflinePlayer,
        experience: Int,
        callback: ExperienceUpdateCallback,
    ) {
        val character = getRpkCharacter(player) ?: return
        addProfessionExperience(character, experience, callback)
    }

    fun getProfessionExperience(character: RPKCharacter): Int {
        return characterProfessionExperience[character.id.value] ?: 0
    }

    fun setProfessionExperience(
        character: RPKCharacter,
        experience: Int,
        callback: Runnable? = null,
    ) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                repository.setProfessionExperience(character.id, experience)
                characterProfessionExperience[character.id.value] = experience
                if (callback != null) {
                    plugin.server.scheduler.runTask(plugin, callback)
                }
            },
        )
    }

    fun addProfessionExperience(
        character: RPKCharacter,
        experience: Int,
        callback: ExperienceUpdateCallback,
    ) {
        val experienceForMaxLevel = getTotalExperienceForLevel(maxLevel)
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                repository.getAndUpdate(character.id, { dsl: DSLContext, oldExperience: Int ->
                    repository.setProfessionExperience(
                        dsl,
                        character.id,
                        min((oldExperience + experience).toDouble(), experienceForMaxLevel.toDouble())
                            .toInt(),
                    )
                }, { oldExperience: Int, newExperience: Int ->
                    characterProfessionExperience[character.id.value] = newExperience
                    callback.invoke(oldExperience, newExperience)
                })
            },
        )
    }

    fun getLevelAtExperience(experience: Int): Int {
        var level = 1
        while (plugin.config.contains("experience.levels." + (level + 1)) && experience >=
            getTotalExperienceForLevel(
                level + 1,
            )
        ) {
            level++
        }
        return level
    }

    fun getExperienceForLevel(level: Int): Int {
        if (level <= 1) return getTotalExperienceForLevel(level)
        return getTotalExperienceForLevel(level) - getTotalExperienceForLevel(level - 1)
    }

    fun getTotalExperienceForLevel(level: Int): Int {
        var totalExperienceForLevel = 0
        for (i in 1..level) {
            if (!plugin.config.contains("experience.levels.$i")) break
            totalExperienceForLevel += plugin.config.getInt("experience.levels.$i")
        }
        return totalExperienceForLevel
    }

    val maxLevel: Int
        get() {
            var level = 1
            while (plugin.config.contains("experience.levels." + (level + 1))) {
                level++
            }
            return level
        }

    fun load(character: RPKCharacter) {
        val professionId = repository.getProfessionId(character.id)
        if (professionId != null) {
            characterProfessions[character.id.value] = professionId
        }
        val professionExperience = repository.getProfessionExperience(character.id)
        characterProfessionExperience[character.id.value] = professionExperience
    }

    fun unload(character: RPKCharacter) {
        characterProfessions.remove(character.id.value)
        characterProfessionExperience.remove(character.id.value)
    }

    private fun getRpkCharacter(player: OfflinePlayer?): RPKCharacter? {
        val minecraftProfileService =
            Services.INSTANCE.get(
                RPKMinecraftProfileService::class.java,
            )
        if (minecraftProfileService == null) return null
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player) ?: return null
        val characterService =
            Services.INSTANCE.get(
                RPKCharacterService::class.java,
            )
        if (characterService == null) return null
        return characterService.getPreloadedActiveCharacter(minecraftProfile)
    }

    private fun saveDefaultProfessions(professionsFolder: File) {
        saveCarpenter(professionsFolder)
        saveSmith(professionsFolder)
    }

    private fun saveCarpenter(professionsFolder: File) {
        val carpenterFile = File(professionsFolder, "carpenter.yml")
        val carpenterConfig = YamlConfiguration()
        carpenterConfig["profession"] =
            NuminousProfession(
                "carpenter",
                "Carpenter",
            )
        try {
            carpenterConfig.save(carpenterFile)
        } catch (exception: IOException) {
            plugin.logger.log(Level.SEVERE, "Failed to save carpenter profession", exception)
        }
    }

    private fun saveSmith(professionsFolder: File) {
        val smithFile = File(professionsFolder, "smith.yml")
        val smithConfig = YamlConfiguration()
        smithConfig["profession"] =
            NuminousProfession(
                "smith",
                "Smith",
            )
        try {
            smithConfig.save(smithFile)
        } catch (exception: IOException) {
            plugin.logger.log(Level.SEVERE, "Failed to save smith profession", exception)
        }
    }
}
