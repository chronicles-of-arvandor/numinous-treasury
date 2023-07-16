package net.arvandor.numinoustreasury.listener;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.stamina.StaminaTier;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.droptable.NuminousDropTableItem;
import net.arvandor.numinoustreasury.node.NuminousNode;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.Random;

import static net.md_5.bungee.api.ChatColor.*;

public final class BlockBreakListener implements Listener {

    private final NuminousTreasury plugin;

    private final List<String> possibleNoStaminaMessages = List.of(
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
            "You take a step back, feeling the full weight of weariness."
    );
    private final Random random = new Random();

    public BlockBreakListener(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        NuminousNode node = nodeService.getNodeWithAreaAtLocation(event.getBlock().getLocation());
        if (node != null) {
            event.setCancelled(true);
            NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
            NuminousProfession playerProfession = professionService.getProfession(event.getPlayer());
            if (playerProfession == null || !node.getApplicableProfessions().contains(playerProfession) || node.getRequiredProfessionLevel(playerProfession) > professionService.getProfessionLevel(event.getPlayer())) {
                event.getPlayer().sendMessage(RED + "To retrieve anything here, you need one of the following professions:");
                node.getApplicableProfessions().forEach(profession -> {
                    event.getPlayer().sendMessage(RED + "• Lv" + node.getRequiredProfessionLevel(profession) + " " + profession.getName());
                });
                return;
            }
            NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
            staminaService.getAndUpdateStamina(
                    event.getPlayer(),
                    (dsl, oldStamina) -> {
                        if (oldStamina < node.getStaminaCost()) {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                String noStaminaMessage = possibleNoStaminaMessages.get(random.nextInt(possibleNoStaminaMessages.size()));
                                event.getPlayer().sendMessage(RED + noStaminaMessage);
                                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.5f);
                            });
                            return;
                        }
                        staminaService.setStamina(dsl, event.getPlayer(), oldStamina - node.getStaminaCost());
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            NuminousDropTableItem drop = node.getDropTable().chooseItem();
                            List<Block> lastTwoTargetBlocks = event.getPlayer().getLastTwoTargetBlocks(null, 100);
                            BlockFace face = lastTwoTargetBlocks.size() != 2 ? BlockFace.SELF : lastTwoTargetBlocks.get(1).getFace(lastTwoTargetBlocks.get(0));
                            if (face == null) face = BlockFace.SELF;
                            final BlockFace finalFace = face;
                            if (drop != null) {
                                if (drop.getItems().isEmpty()) {
                                    event.getPlayer().sendMessage(RED + "Try as you might, you weren't able to retrieve anything yet. Perhaps if you try again?");
                                } else {
                                    event.getPlayer().sendMessage(GREEN + "You got:");
                                    drop.getItems().forEach(item -> {
                                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getRelative(finalFace).getLocation(), item.toItemStack());
                                        event.getPlayer().sendMessage(GRAY + "• " + item.getAmount() + " × " + item.getItemType().getName());
                                    });
                                }
                            } else {
                                event.getPlayer().sendMessage(RED + "Try as you might, you weren't able to retrieve anything yet. Perhaps if you try again?");
                            }
                            int maxLevel = professionService.getMaxLevel();
                            int oldExperience = professionService.getProfessionExperience(event.getPlayer());
                            int oldLevel = professionService.getLevelAtExperience(oldExperience);
                            professionService.addProfessionExperience(event.getPlayer(), node.getExperience(), (newExperience) -> {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    int newLevel = professionService.getLevelAtExperience(newExperience);
                                    int experienceSinceLastLevel = newExperience - (newLevel > 1 ? professionService.getTotalExperienceForLevel(newLevel) : 0);
                                    int experienceRequiredForNextLevel = professionService.getExperienceForLevel(newLevel + 1);
                                    NuminousProfession profession = professionService.getProfession(event.getPlayer());
                                    if (profession != null) {
                                        if (newLevel < maxLevel) {
                                            event.getPlayer().sendMessage(YELLOW + "+" + (newExperience - oldExperience) + " " + profession.getName() + " exp (" + experienceSinceLastLevel + "/" + experienceRequiredForNextLevel + ")");
                                        } else if (oldLevel < maxLevel && newLevel == maxLevel) {
                                            event.getPlayer().sendMessage(YELLOW + "+" + (newExperience - oldExperience) + " " + profession.getName() + "exp (MAX LEVEL)");
                                        }
                                        if (newLevel > oldLevel) {
                                            if (newLevel == maxLevel) {
                                                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                                            } else {
                                                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                            }
                                            event.getPlayer().sendMessage(YELLOW + "Level up! You are now a level " + newLevel + " " + profession.getName());
                                        } else {
                                            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                        }
                                    }
                                });
                            });
                        });
                    },
                    (oldStamina, newStamina) -> {
                        int maxStamina = plugin.getConfig().getInt("stamina.max");
                        String message = StaminaTier.messageForStaminaTransition(oldStamina, newStamina, maxStamina);
                        if (message != null) {
                            event.getPlayer().sendMessage(message);
                        }
                    }
            );
        }
    }

}
