package net.arvandor.numinoustreasury.stamina

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Service
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jooq.DSLContext
import java.util.concurrent.ConcurrentHashMap

class NuminousStaminaService(
    private val plugin: NuminousTreasury,
    private val staminaRepository: NuminousCharacterStaminaRepository,
) : Service {
    private val characterStamina: MutableMap<Int, Int> = ConcurrentHashMap()
    val maxStamina: Int = plugin.config.getInt("stamina.max")

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }

    fun getStamina(character: RPKCharacter): Int {
        return characterStamina[character.id.value] ?: 0
    }

    fun getStamina(player: Player): Int {
        val character = getRpkCharacter(player) ?: return 0
        return getStamina(character)
    }

    fun getAndUpdateStamina(
        player: OfflinePlayer,
        function: StaminaUpdateFunction,
        callback: StaminaUpdateCallback,
    ) {
        val character = getRpkCharacter(player) ?: return
        getAndUpdateStamina(character, function, callback)
    }

    fun getAndUpdateStamina(
        character: RPKCharacter,
        function: StaminaUpdateFunction,
        callback: StaminaUpdateCallback,
    ) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                staminaRepository.getAndUpdate(character.id, function) { oldStamina: Int, newStamina: Int ->
                    characterStamina[character.id.value] = newStamina
                    plugin.server.scheduler.runTask(
                        plugin,
                        Runnable {
                            callback.invoke(oldStamina, newStamina)
                        },
                    )
                }
            },
        )
    }

    fun setStamina(
        player: OfflinePlayer,
        stamina: Int,
        callback: Runnable?,
    ) {
        val character = getRpkCharacter(player) ?: return
        setStamina(character, stamina, callback)
    }

    fun setStamina(
        character: RPKCharacter,
        stamina: Int,
        callback: Runnable? = null,
    ) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                staminaRepository.setStamina(character.id, stamina)
                characterStamina[character.id.value] = stamina
                if (callback != null) {
                    plugin.server.scheduler.runTask(plugin, callback)
                }
            },
        )
    }

    fun setStamina(
        dsl: DSLContext,
        player: OfflinePlayer,
        stamina: Int,
    ) {
        val character = getRpkCharacter(player) ?: return
        setStamina(dsl, character, stamina)
    }

    fun setStamina(
        dsl: DSLContext,
        character: RPKCharacter,
        stamina: Int,
    ) {
        staminaRepository.setStamina(dsl, character.id, stamina)
        characterStamina[character.id.value] = stamina
    }

    fun restoreStamina(callback: Runnable? = null) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                staminaRepository.restoreStamina()
                characterStamina.clear()
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val onlineCharacters =
                            plugin.server.onlinePlayers.mapNotNull { player: OfflinePlayer ->
                                this.getRpkCharacter(
                                    player,
                                )
                            }
                        plugin.server.scheduler.runTaskAsynchronously(
                            plugin,
                            Runnable {
                                onlineCharacters.forEach { character -> this.load(character) }
                                if (callback != null) {
                                    plugin.server.scheduler.runTask(plugin, callback)
                                }
                            },
                        )
                    },
                )
            },
        )
    }

    private fun getRpkCharacter(player: OfflinePlayer): RPKCharacter? {
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

    fun load(character: RPKCharacter) {
        characterStamina[character.id.value] = staminaRepository.getStamina(character.id)
    }

    fun unload(character: RPKCharacter) {
        characterStamina.remove(character.id.value)
    }
}
