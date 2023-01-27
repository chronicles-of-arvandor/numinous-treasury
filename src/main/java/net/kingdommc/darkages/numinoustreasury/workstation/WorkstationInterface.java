package net.kingdommc.darkages.numinoustreasury.workstation;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfession;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfessionService;
import net.kingdommc.darkages.numinoustreasury.recipe.NuminousRecipe;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static net.md_5.bungee.api.ChatColor.*;
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

    public WorkstationInterface(NuminousTreasury plugin, Player player, List<NuminousRecipe> possibleRecipes) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = plugin.getServer().createInventory(this, INVENTORY_SIZE);
        this.possibleRecipes = possibleRecipes;
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
                NuminousRecipe recipe = possibleRecipes.get(itemIndex);
                boolean professionRequirementsMet = recipe.isProfessionRequirementsMet(player);
                boolean ingredientRequirementsMet = recipe.isIngredientRequirementsMet(player);
                if (professionRequirementsMet && ingredientRequirementsMet) {
                    recipe.use(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                    player.sendMessage(GREEN + "Crafted:");
                    recipe.getResults().forEach(result ->
                            player.sendMessage(GRAY + "• " + result.getItemType().getName() + " × " + result.getAmount())
                    );
                    NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
                    int maxLevel = professionService.getMaxLevel();
                    int experienceForMaxLevel = professionService.getTotalExperienceForLevel(maxLevel);
                    int oldExperience = professionService.getProfessionExperience(player);
                    int oldLevel = professionService.getLevelAtExperience(oldExperience);
                    int newExperience = Math.min(professionService.getProfessionExperience(player) + recipe.getExperience(), experienceForMaxLevel);
                    int newLevel = professionService.getLevelAtExperience(newExperience);
                    int experienceSinceLastLevel = newExperience - professionService.getTotalExperienceForLevel(newLevel);
                    int experienceRequiredForNextLevel = professionService.getExperienceForLevel(newLevel + 1);
                    professionService.setProfessionExperience(player, newExperience, () -> {
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
                    });
                } else {
                    if (!professionRequirementsMet) {
                        player.sendMessage(RED + "This recipe requires you to be a lv" + recipe.getRequiredProfessionLevel() + " " + recipe.getRequiredProfession().getName());
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
