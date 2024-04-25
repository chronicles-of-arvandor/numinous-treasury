package net.arvandor.numinoustreasury.workstation

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.arvandor.numinoustreasury.recipe.NuminousRecipe
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import net.arvandor.numinoustreasury.stamina.StaminaTier
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material.PAPER
import org.bukkit.Sound.BLOCK_ANVIL_USE
import org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP
import org.bukkit.Sound.ENTITY_PLAYER_LEVELUP
import org.bukkit.Sound.ENTITY_VILLAGER_NO
import org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.jooq.DSLContext
import java.util.Random

class WorkstationInterface(
    private val plugin: NuminousTreasury,
    private val player: Player,
    private val possibleRecipes: List<NuminousRecipe?>,
) : InventoryHolder {
    private val inventory: Inventory
    private var page = 0

    private val possibleNoStaminaMessages =
        listOf(
            "You look at your tools with a wistful gaze, but cannot muster the energy to pick them up.",
            "Finding yourself utterly exhausted, you yawn loudly.",
            "You think about it, but you find your mind a haze of out-of-order steps and ideas.",
            "You are utterly spent. The idea of making one more thing is unfathomable to you right now.",
            "Your hands feel heavy, unable to even think about lifting the parts.",
            "You try to focus, but your eyes keep closing on their own.",
            "Your energy reserves are depleted, making it impossible to complete the task.",
            "Your mind is cloudy, unable to come up with a plan to make anything else.",
            "You take a deep breath, but it does little to revive your energy",
            "You feel too weak to hold the tools and materials.",
            "You sit down, defeated by your exhaustion.",
            "You lean back, unable to stay awake any longer.",
            "You close your eyes, taking a much-needed break.",
            "Your body refuses to co-operate, leaving you unable to approach the task.",
            "You try to push through, but your mind keeps wandering.",
            "You take a deep sigh, realising that your energy is completely spent.",
            "You struggle to keep your eyes open, feeling the effects of fatigue.",
            "Your thoughts are jumbled, unable to focus on the task at hand.",
            "You rest your head, feeling a wave of exhaustion wash over you.",
            "You take a moment to catch your breath, feeling utterly drained.",
            "You shake your head, unable to continue due to lack of energy.",
            "You sit in silence, trying to regain your energy.",
            "You lean against a wall, feeling completely drained.",
            "You take a deep breath, trying to clear your mind.",
            "You realise you need a break, feeling exhausted.",
            "You close your eyes, taking a moment to rest.",
            "You feel too tired to even stand, much less craft.",
            "You rest your arms, feeling the weight of fatigue.",
            "You take a step back, feeling overwhelmed by exhaustion.",
            "You struggle to keep your balance, and feel it best to leave the task for later.",
        )
    private val random = Random()

    init {
        this.inventory = plugin.server.createInventory(this, INVENTORY_SIZE, "Crafting")
        renderPage(0)
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    fun getPage(): Int {
        return page
    }

    fun setPage(page: Int) {
        this.page = page
        renderPage(page)
    }

    private fun renderPage(page: Int) {
        for (i in 0 until ITEMS_PER_PAGE) {
            val itemIndex = i + (page * ITEMS_PER_PAGE)
            if (itemIndex < possibleRecipes.size) {
                val recipe = possibleRecipes[itemIndex]
                if (recipe != null) {
                    inventory.setItem(i, recipe.getIcon(player))
                }
            } else {
                inventory.setItem(i, null)
            }
        }
        if (page > 0) {
            val previousPageItem = ItemStack(PAPER, 1)
            val previousPageMeta = previousPageItem.itemMeta
            if (previousPageMeta != null) {
                previousPageMeta.setDisplayName("Previous page")
                previousPageItem.setItemMeta(previousPageMeta)
            }
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE, previousPageItem)
        } else {
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE, null)
        }
        if (page < possibleRecipes.size / ITEMS_PER_PAGE) {
            val nextPageItem = ItemStack(PAPER, 1)
            val nextPageMeta = nextPageItem.itemMeta
            if (nextPageMeta != null) {
                nextPageMeta.setDisplayName("Next page")
                nextPageItem.setItemMeta(nextPageMeta)
            }
            inventory.setItem(INVENTORY_SIZE - 1, nextPageItem)
        } else {
            inventory.setItem(INVENTORY_SIZE - 1, null)
        }
    }

    fun onClick(slot: Int) {
        if (slot == INVENTORY_SIZE - ROW_SIZE && page > 0) {
            setPage(getPage() - 1)
        } else if (slot == INVENTORY_SIZE - 1 && page < possibleRecipes.size / ITEMS_PER_PAGE) {
            setPage(getPage() + 1)
        } else if (slot < ITEMS_PER_PAGE) {
            val itemIndex = (page * ITEMS_PER_PAGE) + slot
            if (itemIndex < possibleRecipes.size) {
                val staminaService =
                    Services.INSTANCE.get(
                        NuminousStaminaService::class.java,
                    )
                val recipe = possibleRecipes[itemIndex]
                if (recipe == null) return
                val professionRequirementsMet = recipe.isProfessionRequirementsMet(player)
                val ingredientRequirementsMet = recipe.isIngredientRequirementsMet(player)
                if (professionRequirementsMet && ingredientRequirementsMet) {
                    staminaService.getAndUpdateStamina(
                        player,
                        { dsl: DSLContext, oldStamina: Int ->
                            if (oldStamina < recipe.stamina) {
                                plugin.server.scheduler.runTask(
                                    plugin,
                                    Runnable {
                                        val noStaminaMessage =
                                            possibleNoStaminaMessages[random.nextInt(possibleNoStaminaMessages.size)]
                                        player.sendMessage(ChatColor.RED.toString() + noStaminaMessage)
                                        player.world.playSound(player.location, ENTITY_VILLAGER_NO, 1f, 0.5f)
                                    },
                                )
                                return@getAndUpdateStamina
                            }
                            staminaService.setStamina(dsl, player, oldStamina - recipe.stamina)
                            plugin.server.scheduler.runTask(
                                plugin,
                                Runnable {
                                    recipe.use(player)
                                    player.world.playSound(player.location, BLOCK_ANVIL_USE, 1f, 1f)
                                    player.sendMessage(ChatColor.GREEN.toString() + "Crafted:")
                                    recipe.results.forEach { result ->
                                        player.sendMessage(
                                            ChatColor.GRAY.toString() + "• " + result.itemType.name + " × " + result.amount,
                                        )
                                    }

                                    val professionService =
                                        Services.INSTANCE.get(
                                            NuminousProfessionService::class.java,
                                        )
                                    val maxLevel = professionService.maxLevel
                                    professionService.addProfessionExperience(
                                        player,
                                        recipe.experience,
                                    ) { oldExperience: Int, newExperience: Int ->
                                        plugin.server.scheduler.runTask(
                                            plugin,
                                            Runnable {
                                                val oldLevel = professionService.getLevelAtExperience(oldExperience)
                                                val newLevel = professionService.getLevelAtExperience(newExperience)
                                                val experienceSinceLastLevel =
                                                    newExperience - (
                                                        if (newLevel > 1) {
                                                            professionService.getTotalExperienceForLevel(
                                                                newLevel,
                                                            )
                                                        } else {
                                                            0
                                                        }
                                                    )
                                                val experienceRequiredForNextLevel =
                                                    professionService.getExperienceForLevel(newLevel + 1)
                                                val profession = professionService.getProfession(player)
                                                if (profession != null) {
                                                    if (newLevel < maxLevel) {
                                                        player.sendMessage(
                                                            ChatColor.YELLOW.toString() + "+" +
                                                                (newExperience - oldExperience) + " " +
                                                                profession.name +
                                                                " exp (" +
                                                                experienceSinceLastLevel +
                                                                "/" +
                                                                experienceRequiredForNextLevel +
                                                                ")",
                                                        )
                                                    } else if (oldLevel < maxLevel && newLevel == maxLevel) {
                                                        player.sendMessage(
                                                            ChatColor.YELLOW.toString() + "+" +
                                                                (newExperience - oldExperience) +
                                                                " " +
                                                                profession.name +
                                                                " exp (MAX LEVEL)",
                                                        )
                                                    }
                                                    if (newLevel > oldLevel) {
                                                        if (newLevel == maxLevel) {
                                                            player.world.playSound(
                                                                player.location,
                                                                UI_TOAST_CHALLENGE_COMPLETE,
                                                                1.0f,
                                                                1.0f,
                                                            )
                                                        } else {
                                                            player.world.playSound(
                                                                player.location,
                                                                ENTITY_PLAYER_LEVELUP,
                                                                1.0f,
                                                                1.0f,
                                                            )
                                                        }
                                                        player.sendMessage(
                                                            ChatColor.YELLOW.toString() +
                                                                "Level up! You are now a level " +
                                                                newLevel + " " +
                                                                profession.name,
                                                        )
                                                    } else {
                                                        player.world.playSound(
                                                            player.location,
                                                            ENTITY_EXPERIENCE_ORB_PICKUP,
                                                            1.0f,
                                                            1.0f,
                                                        )
                                                    }
                                                }
                                                renderPage(getPage())
                                            },
                                        )
                                    }
                                },
                            )
                        },
                        { oldStamina: Int, newStamina: Int ->
                            val maxStamina = staminaService.maxStamina
                            val message =
                                StaminaTier.messageForStaminaTransition(oldStamina, newStamina, maxStamina)
                            if (message != null) {
                                player.sendMessage(message)
                            }
                        },
                    )
                } else {
                    if (!professionRequirementsMet) {
                        player.sendMessage(ChatColor.RED.toString() + "This recipe requires you to have one of the following professions:")
                        recipe.applicableProfessions.forEach { profession ->
                            player.sendMessage(
                                ChatColor.RED.toString() + "• Lv" +
                                    recipe.getRequiredProfessionLevel(
                                        profession,
                                    ) + " " + profession.name,
                            )
                        }
                    }
                    if (!ingredientRequirementsMet) {
                        player.sendMessage(ChatColor.RED.toString() + "This recipe requires you to have the following ingredients:")
                        recipe.ingredients.forEach { ingredient ->
                            player.sendMessage(
                                ChatColor.GRAY.toString() + "• " + ingredient.itemType.name + " × " + ingredient.amount,
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val INVENTORY_SIZE = 54
        private const val ROW_SIZE = 9
        private const val ITEMS_PER_PAGE = INVENTORY_SIZE - ROW_SIZE
    }
}
