package net.arvandor.numinoustreasury.listener

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelService
import net.arvandor.numinoustreasury.mixpanel.event.NuminousMixpanelItemCreatedEvent
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import net.arvandor.numinoustreasury.stamina.StaminaTier
import net.arvandor.numinoustreasury.stamina.StaminaUpdateCallback
import net.arvandor.numinoustreasury.stamina.StaminaUpdateFunction
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP
import org.bukkit.Sound.ENTITY_PLAYER_LEVELUP
import org.bukkit.Sound.ENTITY_VILLAGER_NO
import org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.SELF
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOW
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.time.Instant
import java.util.Random

class BlockBreakListener(private val plugin: NuminousTreasury) : Listener {
    private val possibleNoStaminaMessages =
        listOf(
            "Your arms ache with fatigue, making it impossible to harvest the land.",
            "You feel the weight of exhaustion upon your back, unable to reap what the earth has sown.",
            "Your weary hands cannot grasp the tools necessary for the task.",
            "You find yourself too tired to pluck the bounty from the earth.",
            "Your mind is muddled, making it difficult to focus on the task.",
            "You struggle against the burden of weariness, unable to harvest what you seek.",
            "You take a deep breath, trying to gather your strength, but it is not enough.",
            "Your knees buckle under the strain of fatigue, making it impossible to go on.",
            "Your eyes are heavy with weariness, a haze enveloping the scene in front of you. Perhaps you should stop for now.",
            "You try to push through, but your body refuses to cooperate.",
            "You rest your head, feeling the effects of fatigue wash over you.",
            "You take a moment to catch your breath, feeling too tired to continue.",
            "You feel the exhaustion deep in your bones. Perhaps you shouldn't push yourself any further.",
            "Your hands feel unsteady, your grip on your tools weakening.",
            "You feel too drained to even make an attempt.",
            "You take a step back, feeling the full weight of weariness.",
        )
    private val random = Random()

    @EventHandler(priority = LOW, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val nodeService =
            Services.INSTANCE.get(
                NuminousNodeService::class.java,
            )
        val node = nodeService.getNodeWithAreaAtLocation(event.block.location)
        if (node != null) {
            event.isCancelled = true
            val professionService =
                Services.INSTANCE.get(
                    NuminousProfessionService::class.java,
                )
            val playerProfession = professionService.getProfession(event.player)
            if (playerProfession == null || !node.applicableProfessions.contains(playerProfession) || node.getRequiredProfessionLevel(
                    playerProfession,
                )!! > professionService.getProfessionLevel(event.player)
            ) {
                event.player.sendMessage(ChatColor.RED.toString() + "To retrieve anything here, you need one of the following professions:")
                node.applicableProfessions.forEach { profession ->
                    event.player.sendMessage(
                        ChatColor.RED.toString() + "• Lv" + node.getRequiredProfessionLevel(profession) + " " + profession.name,
                    )
                }
                return
            }
            val staminaService =
                Services.INSTANCE.get(
                    NuminousStaminaService::class.java,
                )
            staminaService.getAndUpdateStamina(
                event.player,
                StaminaUpdateFunction { dsl, oldStamina ->
                    if (oldStamina < node.staminaCost) {
                        plugin.server.scheduler.runTask(
                            plugin,
                            Runnable {
                                val noStaminaMessage =
                                    possibleNoStaminaMessages[random.nextInt(possibleNoStaminaMessages.size)]
                                event.player.sendMessage(ChatColor.RED.toString() + noStaminaMessage)
                                event.player.world.playSound(event.player.location, ENTITY_VILLAGER_NO, 1f, 0.5f)
                            },
                        )
                        return@StaminaUpdateFunction
                    }
                    staminaService.setStamina(dsl, event.player, oldStamina - node.staminaCost)
                    plugin.server.scheduler.runTask(
                        plugin,
                        Runnable {
                            val drop = node.dropTable.chooseItem()
                            val lastTwoTargetBlocks = event.player.getLastTwoTargetBlocks(null, 100)
                            var face =
                                if (lastTwoTargetBlocks.size != 2) {
                                    SELF
                                } else {
                                    lastTwoTargetBlocks[1].getFace(
                                        lastTwoTargetBlocks[0],
                                    )
                                }
                            if (face == null) face = SELF
                            val finalFace: BlockFace = face
                            if (drop != null) {
                                if (drop.items.isEmpty()) {
                                    event.player.sendMessage(
                                        ChatColor.RED.toString() + "Try as you might, you weren't able to retrieve " +
                                            "anything yet. Perhaps if you try again?",
                                    )
                                } else {
                                    event.player.sendMessage(ChatColor.GREEN.toString() + "You got:")
                                    val mixpanelService =
                                        Services.INSTANCE.get(
                                            NuminousMixpanelService::class.java,
                                        )
                                    drop.items.forEach { item ->
                                        event.block.world.dropItemNaturally(
                                            event.block.getRelative(finalFace).location,
                                            item.copy(
                                                null,
                                                null,
                                                null,
                                                listOf(
                                                    NuminousLogEntry(
                                                        Instant.now(),
                                                        event.player.uniqueId,
                                                        true,
                                                        arrayOf(
                                                            TextComponent("Harvested from node \"" + node.name + "\""),
                                                        ),
                                                    ),
                                                ),
                                            ).toItemStack(),
                                        )
                                        event.player.sendMessage(
                                            ChatColor.GRAY.toString() + "• " + item.amount + " × " + item.itemType.name,
                                        )
                                        plugin.server.scheduler.runTaskAsynchronously(
                                            plugin,
                                            Runnable {
                                                mixpanelService.trackEvent(
                                                    NuminousMixpanelItemCreatedEvent(
                                                        event.player,
                                                        item.itemType,
                                                        item.amount,
                                                        "Node",
                                                    ),
                                                )
                                            },
                                        )
                                    }
                                }
                            } else {
                                event.player.sendMessage(
                                    ChatColor.RED.toString() + "Try as you might, you weren't able to retrieve " +
                                        "anything yet. Perhaps if you try again?",
                                )
                            }
                            val maxLevel = professionService.maxLevel
                            professionService.addProfessionExperience(
                                event.player,
                                node.experience,
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
                                        val profession = professionService.getProfession(event.player)
                                        if (profession != null) {
                                            if (newLevel < maxLevel) {
                                                event.player.sendMessage(
                                                    ChatColor.YELLOW.toString() + "+" +
                                                        (newExperience - oldExperience) +
                                                        " " + profession.name +
                                                        " exp (" +
                                                        experienceSinceLastLevel +
                                                        "/" +
                                                        experienceRequiredForNextLevel +
                                                        ")",
                                                )
                                            } else if (oldLevel < maxLevel && newLevel == maxLevel) {
                                                event.player.sendMessage(
                                                    ChatColor.YELLOW.toString() + "+" +
                                                        (newExperience - oldExperience) +
                                                        " " + profession.name + " exp (MAX LEVEL)",
                                                )
                                            }
                                            if (newLevel > oldLevel) {
                                                if (newLevel == maxLevel) {
                                                    event.player.world.playSound(
                                                        event.player.location,
                                                        UI_TOAST_CHALLENGE_COMPLETE,
                                                        1.0f,
                                                        1.0f,
                                                    )
                                                } else {
                                                    event.player.world.playSound(
                                                        event.player.location,
                                                        ENTITY_PLAYER_LEVELUP,
                                                        1.0f,
                                                        1.0f,
                                                    )
                                                }
                                                event.player.sendMessage(
                                                    ChatColor.YELLOW.toString() + "Level up!" +
                                                        "You are now a level " + newLevel + " " + profession.name,
                                                )
                                            } else {
                                                event.player.world.playSound(
                                                    event.player.location,
                                                    ENTITY_EXPERIENCE_ORB_PICKUP,
                                                    1.0f,
                                                    1.0f,
                                                )
                                            }
                                        }
                                    },
                                )
                            }
                        },
                    )
                },
                StaminaUpdateCallback { oldStamina: Int, newStamina: Int ->
                    val maxStamina = staminaService.maxStamina
                    val message = StaminaTier.messageForStaminaTransition(oldStamina, newStamina, maxStamina)
                    if (message != null) {
                        event.player.sendMessage(message)
                    }
                },
            )
        }
    }
}
