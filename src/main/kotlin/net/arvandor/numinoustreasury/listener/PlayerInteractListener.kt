package net.arvandor.numinoustreasury.listener

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.interaction.NuminousInteractionService
import net.arvandor.numinoustreasury.interaction.NuminousInteractionStatus.SELECTING_AREA_1
import net.arvandor.numinoustreasury.interaction.NuminousInteractionStatus.SELECTING_AREA_2
import net.arvandor.numinoustreasury.interaction.NuminousInteractionStatus.SELECTING_ENTRANCE_AREA_1
import net.arvandor.numinoustreasury.interaction.NuminousInteractionStatus.SELECTING_ENTRANCE_AREA_2
import net.arvandor.numinoustreasury.interaction.NuminousInteractionStatus.SELECTING_EXIT_AREA_1
import net.arvandor.numinoustreasury.interaction.NuminousInteractionStatus.SELECTING_EXIT_AREA_2
import net.arvandor.numinoustreasury.item.NuminousItemStack
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.arvandor.numinoustreasury.recipe.NuminousRecipe
import net.arvandor.numinoustreasury.recipe.NuminousRecipeService
import net.arvandor.numinoustreasury.workstation.WorkstationInterface
import net.md_5.bungee.api.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot.HAND

class PlayerInteractListener(private val plugin: NuminousTreasury) : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        onCustomItemInteract(event)
        onWorkstationInteract(event)
        onNodeSetup(event)
    }

    private fun onCustomItemInteract(event: PlayerInteractEvent) {
        val numinousItemStack: NuminousItemStack = NuminousItemStack.Companion.fromItemStack(event.item) ?: return
        if (event.hasBlock()) {
            val onInteractBlock = numinousItemStack.itemType.onInteractBlock
            for (action in onInteractBlock) {
                action.onInteractBlock(event)
            }
        } else {
            val onInteractAir = numinousItemStack.itemType.onInteractAir
            for (action in onInteractAir) {
                action.onInteractAir(event)
            }
        }
    }

    private fun onWorkstationInteract(event: PlayerInteractEvent) {
        if (!event.hasBlock()) return
        if (event.action != RIGHT_CLICK_BLOCK) return
        val recipeService =
            Services.INSTANCE.get(
                NuminousRecipeService::class.java,
            )
        val workstationRecipes =
            recipeService.recipes
                .filter { recipe ->
                    recipe.workstation ==
                        event.clickedBlock!!
                            .type
                }
                .sortedWith { a: NuminousRecipe, b: NuminousRecipe ->
                    val aRequirementsMet =
                        a.isIngredientRequirementsMet(event.player) && a.isProfessionRequirementsMet(event.player)
                    val bRequirementsMet =
                        b.isIngredientRequirementsMet(event.player) && b.isProfessionRequirementsMet(event.player)
                    if (aRequirementsMet && !bRequirementsMet) return@sortedWith -1
                    if (!aRequirementsMet && bRequirementsMet) return@sortedWith 1
                    0
                }
        if (workstationRecipes.isEmpty()) return
        event.isCancelled = true
        val workstationInterface = WorkstationInterface(plugin, event.player, workstationRecipes)
        event.player.openInventory(workstationInterface.inventory)
    }

    private fun onNodeSetup(event: PlayerInteractEvent) {
        if (!event.hasBlock()) return
        if (event.action != RIGHT_CLICK_BLOCK) return
        if (event.hand != HAND) return
        val interactionService =
            Services.INSTANCE.get(
                NuminousInteractionService::class.java,
            )
        val status = interactionService.getInteractionStatus(event.player)
        val nodeService =
            Services.INSTANCE.get(
                NuminousNodeService::class.java,
            )
        if (status == null) return
        val session = nodeService.getNodeCreationSession(event.player) ?: return
        event.isCancelled = true
        when (status) {
            SELECTING_ENTRANCE_AREA_1 -> {
                session.entranceAreaLocation1 = event.clickedBlock!!.location
                interactionService.setInteractionStatus(event.player, SELECTING_ENTRANCE_AREA_2)
                event.player.sendMessage(ChatColor.GREEN.toString() + "Entrance area location 1 set, please select location 2.")
            }

            SELECTING_ENTRANCE_AREA_2 -> {
                session.entranceAreaLocation2 = event.clickedBlock!!.location
                interactionService.setInteractionStatus(event.player, null)
                session.display(event.player)
            }

            SELECTING_EXIT_AREA_1 -> {
                session.exitAreaLocation1 = event.clickedBlock!!.location
                interactionService.setInteractionStatus(event.player, SELECTING_EXIT_AREA_2)
                event.player.sendMessage(ChatColor.GREEN.toString() + "Exit area location 1 set, please select location 2.")
            }

            SELECTING_EXIT_AREA_2 -> {
                session.exitAreaLocation2 = event.clickedBlock!!.location
                interactionService.setInteractionStatus(event.player, null)
                session.display(event.player)
            }

            SELECTING_AREA_1 -> {
                session.areaLocation1 = event.clickedBlock!!.location
                interactionService.setInteractionStatus(event.player, SELECTING_AREA_2)
                event.player.sendMessage(ChatColor.GREEN.toString() + "Area location 1 set, please select location 2.")
            }

            SELECTING_AREA_2 -> {
                session.areaLocation2 = event.clickedBlock!!.location
                interactionService.setInteractionStatus(event.player, null)
                session.display(event.player)
            }
        }
    }
}
