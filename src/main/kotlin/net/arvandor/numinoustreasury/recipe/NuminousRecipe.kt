package net.arvandor.numinoustreasury.recipe

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.NuminousItemStack
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelService
import net.arvandor.numinoustreasury.mixpanel.event.NuminousMixpanelItemCreatedEvent
import net.arvandor.numinoustreasury.profession.NuminousProfession
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES
import org.bukkit.inventory.ItemFlag.HIDE_DESTROYS
import org.bukkit.inventory.ItemFlag.HIDE_DYE
import org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS
import org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON
import org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS
import org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
import org.bukkit.inventory.ItemStack
import java.time.Instant

@SerializableAs("NuminousRecipe")
class NuminousRecipe(
    private val plugin: NuminousTreasury,
    val name: String,
    val ingredients: List<NuminousItemStack>,
    val results: List<NuminousItemStack>,
    val requiredProfessionLevel: Map<NuminousProfession, Int>,
    val experience: Int,
    val stamina: Int,
    val workstation: Material,
    val iconMaterial: Material,
) : ConfigurationSerializable {
    val applicableProfessions: List<NuminousProfession>
        get() = requiredProfessionLevel.keys.toList()

    fun getRequiredProfessionLevel(profession: NuminousProfession): Int? {
        return requiredProfessionLevel[profession]
    }

    fun getIcon(player: Player): ItemStack {
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        val professionRequirementsMet = isProfessionRequirementsMet(player)
        val ingredientRequirementsMet = isIngredientRequirementsMet(player)
        val craftable = professionRequirementsMet && ingredientRequirementsMet
        val icon = ItemStack(iconMaterial)
        val meta = icon.itemMeta
        if (meta != null) {
            meta.setDisplayName((if (craftable) ChatColor.GREEN else ChatColor.RED).toString() + name)
            val lore: MutableList<String> = ArrayList()
            lore.add(ChatColor.BOLD.toString() + ChatColor.WHITE + "Ingredients:")
            for (ingredient in ingredients) {
                var amountOwned = 0
                for (item in player.inventory.contents) {
                    if (ingredient.itemType.isItem(item)) {
                        amountOwned += item.amount
                    }
                }
                lore.add(
                    (
                        if (amountOwned >= ingredient.amount) {
                            ChatColor.GREEN
                        } else {
                            ChatColor.RED
                        }
                    ).toString() +
                        "• " + ingredient.itemType.name + " × " + ingredient.amount,
                )
            }
            lore.add("")
            lore.add(ChatColor.BOLD.toString() + ChatColor.WHITE + "Results:")
            for (result in results) {
                lore.add(ChatColor.GRAY.toString() + "• " + result.itemType.name + " × " + result.amount)
            }
            lore.add("")
            lore.add(ChatColor.BOLD.toString() + ChatColor.WHITE + "Required profession: ")
            applicableProfessions.forEach { profession ->
                val playerProfession = professionService.getProfession(player)
                val isThisProfessionRequirementMet =
                    playerProfession != null && playerProfession.id == profession.id && professionService.getProfessionLevel(
                        player,
                    ) >= getRequiredProfessionLevel(profession)!!
                lore.add(
                    (if (isThisProfessionRequirementMet) ChatColor.GREEN else ChatColor.RED).toString() + "• Lv" +
                        getRequiredProfessionLevel(
                            profession,
                        ) + " " + profession.name,
                )
            }
            meta.lore = lore
            meta.addItemFlags(
                HIDE_ENCHANTS,
                HIDE_ATTRIBUTES,
                HIDE_UNBREAKABLE,
                HIDE_DESTROYS,
                HIDE_PLACED_ON,
                HIDE_POTION_EFFECTS,
                HIDE_DYE,
            )
        }
        icon.setItemMeta(meta)
        return icon
    }

    fun use(player: Player) {
        for (ingredient in ingredients) {
            var remainingAmount = ingredient.amount
            val inventoryContents = player.inventory.contents
            for (itemStack in inventoryContents) {
                if (ingredient.itemType.isItem(itemStack)) {
                    val amount = itemStack.amount
                    if (remainingAmount >= amount) {
                        player.inventory.removeItem(itemStack)
                        remainingAmount -= amount
                    } else {
                        val partialItem = ItemStack(itemStack)
                        partialItem.amount = remainingAmount
                        player.inventory.removeItem(partialItem)
                        remainingAmount = 0
                        break
                    }
                }
            }
            check(
                remainingAmount == 0,
            ) { "Player ${player.name} did not have enough of the required ingredients for recipe $name" }
        }
        val mixpanelService =
            Services.INSTANCE.get(
                NuminousMixpanelService::class.java,
            )
        for (result in results) {
            val resultWithLogEntry =
                result.copy(
                    null,
                    null,
                    null,
                    buildList {
                        if (result.logEntries != null) {
                            addAll(result.logEntries)
                        }
                        add(
                            NuminousLogEntry(
                                Instant.now(),
                                player.uniqueId,
                                true,
                                arrayOf(
                                    TextComponent("Created via crafting"),
                                ),
                            ),
                        )
                    },
                )
            player.inventory.addItem(resultWithLogEntry.toItemStack())

            plugin.server.scheduler.runTaskAsynchronously(
                plugin,
                Runnable {
                    mixpanelService.trackEvent(
                        NuminousMixpanelItemCreatedEvent(
                            player,
                            resultWithLogEntry.itemType,
                            resultWithLogEntry.amount,
                            "Recipe",
                        ),
                    )
                },
            )
        }
    }

    fun isProfessionRequirementsMet(player: Player): Boolean {
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        val profession = professionService.getProfession(player) ?: return false
        val professionLevel = professionService.getProfessionLevel(player)
        return (
            applicableProfessions
                .any { applicableProfession -> applicableProfession.id == profession.id } &&
                professionLevel >= getRequiredProfessionLevel(profession)!!
        )
    }

    fun isIngredientRequirementsMet(player: Player): Boolean {
        return ingredients.all { ingredient ->
            val amount =
                player.inventory.contents
                    .filter { item -> ingredient.itemType.isItem(item) }
                    .sumOf { obj -> obj.amount }
            amount >= ingredient.amount
        }
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "ingredients" to ingredients,
            "results" to results,
            "required-profession-level" to
                requiredProfessionLevel.entries.map {
                    it.key.id to it.value
                },
            "experience" to experience,
            "stamina" to stamina,
            "workstation" to workstation.name,
            "icon" to iconMaterial.name,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): NuminousRecipe {
            val professionService =
                Services.INSTANCE.get(
                    NuminousProfessionService::class.java,
                )
            return NuminousRecipe(
                Bukkit.getServer().pluginManager.getPlugin("numinous-treasury") as NuminousTreasury,
                serialized["name"] as String,
                serialized["ingredients"] as List<NuminousItemStack>,
                serialized["results"] as List<NuminousItemStack>,
                (serialized["required-profession-level"] as Map<String, Int>)
                    .map { (professionId, level) ->
                        professionService.getProfessionById(professionId)!! to level
                    }.toMap(),
                serialized["experience"] as Int,
                serialized["stamina"] as Int,
                Material.valueOf(serialized["workstation"] as String),
                Material.valueOf(serialized["icon"] as String),
            )
        }
    }
}
