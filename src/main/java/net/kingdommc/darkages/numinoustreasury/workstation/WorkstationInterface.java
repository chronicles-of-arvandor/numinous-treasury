package net.kingdommc.darkages.numinoustreasury.workstation;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfession;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfessionService;
import net.kingdommc.darkages.numinoustreasury.recipe.NuminousRecipe;
import net.kingdommc.darkages.numinoustreasury.stamina.NuminousStaminaService;
import net.kingdommc.darkages.numinoustreasury.stamina.StaminaTier;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.ChatColor.YELLOW;
import static org.bukkit.Material.PAPER;

public final class WorkstationInterface implements InventoryHolder {

    private static final int INVENTORY_SIZE = 54;
    private static final int ROW_SIZE = 9;
    private static final int ITEMS_PER_PAGE = INVENTORY_SIZE - ROW_SIZE;

    private final NuminousTreasury plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<NuminousRecipe> possibleRecipes;
    private int page;

    private final List<String> possibleNoStaminaMessages = List.of(
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
            "You struggle to keep your balance, and feel it best to leave the task for later."
    );
    private final Random random = new Random();

    public WorkstationInterface(NuminousTreasury plugin, Player player, List<NuminousRecipe> possibleRecipes) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = plugin.getServer().createInventory(this, INVENTORY_SIZE, "Crafting");
        this.possibleRecipes = possibleRecipes;
        renderPage(0);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
        renderPage(page);
    }

    private void renderPage(int page) {
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int itemIndex = i + (page * ITEMS_PER_PAGE);
            if (itemIndex < possibleRecipes.size()) {
                NuminousRecipe recipe = possibleRecipes.get(itemIndex);
                inventory.setItem(itemIndex, recipe.getIcon(player));
            } else {
                inventory.setItem(itemIndex, null);
            }
        }
        if (page > 0) {
            ItemStack previousPageItem = new ItemStack(PAPER, 1);
            ItemMeta previousPageMeta = previousPageItem.getItemMeta();
            if (previousPageMeta != null) {
                previousPageMeta.setDisplayName("Previous page");
                previousPageItem.setItemMeta(previousPageMeta);
            }
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE - 1, previousPageItem);
        } else {
            inventory.setItem(INVENTORY_SIZE - ROW_SIZE - 1, null);
        }
        if (page < possibleRecipes.size() / ITEMS_PER_PAGE) {
            ItemStack nextPageItem = new ItemStack(PAPER, 1);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            if (nextPageMeta != null) {
                nextPageMeta.setDisplayName("Next page");
                nextPageItem.setItemMeta(nextPageMeta);
            }
            inventory.setItem(INVENTORY_SIZE - 1, nextPageItem);
        } else {
            inventory.setItem(INVENTORY_SIZE - 1, null);
        }
    }

    public void onClick(int slot) {
        if (slot == INVENTORY_SIZE - ROW_SIZE - 1 && page > 0) {
            setPage(getPage() - 1);
        } else if (slot == INVENTORY_SIZE - 1 && page < possibleRecipes.size() / ITEMS_PER_PAGE) {
            setPage(getPage() + 1);
        } else if (slot < ITEMS_PER_PAGE) {
            int itemIndex = (page * ITEMS_PER_PAGE) + slot;
            if (itemIndex < possibleRecipes.size()) {
                NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
                NuminousRecipe recipe = possibleRecipes.get(itemIndex);
                boolean professionRequirementsMet = recipe.isProfessionRequirementsMet(player);
                boolean ingredientRequirementsMet = recipe.isIngredientRequirementsMet(player);
                if (professionRequirementsMet && ingredientRequirementsMet) {
                    staminaService.getAndUpdateStamina(
                            player,
                            (dsl, oldStamina) -> {
                                if (oldStamina < recipe.getStamina()) {
                                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                                        String noStaminaMessage = possibleNoStaminaMessages.get(random.nextInt(possibleNoStaminaMessages.size()));
                                        player.sendMessage(RED + noStaminaMessage);
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.5f);
                                    });
                                    return;
                                }
                                staminaService.setStamina(dsl, player, oldStamina - recipe.getStamina());
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    recipe.use(player);
                                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                                    player.sendMessage(GREEN + "Crafted:");
                                    recipe.getResults().forEach(result ->
                                            player.sendMessage(GRAY + "• " + result.getItemType().getName() + " × " + result.getAmount())
                                    );

                                    NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
                                    int maxLevel = professionService.getMaxLevel();
                                    int oldExperience = professionService.getProfessionExperience(player);
                                    int oldLevel = professionService.getLevelAtExperience(oldExperience);
                                    professionService.addProfessionExperience(player, recipe.getExperience(), (newExperience) -> {
                                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                                            int newLevel = professionService.getLevelAtExperience(newExperience);
                                            int experienceSinceLastLevel = newExperience - (newLevel > 1 ? professionService.getTotalExperienceForLevel(newLevel) : 0);
                                            int experienceRequiredForNextLevel = professionService.getExperienceForLevel(newLevel + 1);
                                            NuminousProfession profession = professionService.getProfession(player);
                                            if (profession != null) {
                                                if (newLevel < maxLevel) {
                                                    player.sendMessage(YELLOW + "+" + (newExperience - oldExperience) + " " + profession.getName() + " exp (" + experienceSinceLastLevel + "/" + experienceRequiredForNextLevel + ")");
                                                } else if (oldLevel < maxLevel && newLevel == maxLevel) {
                                                    player.sendMessage(YELLOW + "+" + (newExperience - oldExperience) + " " + profession.getName() + "exp (MAX LEVEL)");
                                                }
                                                if (newLevel > oldLevel) {
                                                    if (newLevel == maxLevel) {
                                                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                                                    } else {
                                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                                    }
                                                    player.sendMessage(YELLOW + "Level up! You are now a level " + newLevel + " " + profession.getName());
                                                } else {
                                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                                }
                                            }
                                            renderPage(getPage());
                                        });
                                    });
                                });
                            },
                            (oldStamina, newStamina) -> {
                                int maxStamina = plugin.getConfig().getInt("stamina.max");
                                String message = StaminaTier.messageForStaminaTransition(oldStamina, newStamina, maxStamina);
                                if (message != null) {
                                    player.sendMessage(message);
                                }
                            }
                    );
                } else {
                    if (!professionRequirementsMet) {
                        player.sendMessage(RED + "This recipe requires you to have one of the following professions:");
                        recipe.getApplicableProfessions().forEach(profession -> {
                            player.sendMessage(RED + "• Lv" + recipe.getRequiredProfessionLevel(profession) + " " + profession.getName());
                        });
                    }
                    if (!ingredientRequirementsMet) {
                        player.sendMessage(RED + "This recipe requires you to have the following ingredients:");
                        recipe.getIngredients().forEach(ingredient ->
                                player.sendMessage(GRAY + "• " + ingredient.getItemType().getName() + " × " + ingredient.getAmount())
                        );
                    }
                }
            }
        }
    }
}
