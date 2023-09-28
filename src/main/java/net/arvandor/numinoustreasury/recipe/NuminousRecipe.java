package net.arvandor.numinoustreasury.recipe;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.item.NuminousItemStack;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;
import static org.bukkit.inventory.ItemFlag.*;

@SerializableAs("NuminousRecipe")
public final class NuminousRecipe implements ConfigurationSerializable {

    private final String name;
    private final List<NuminousItemStack> ingredients;
    private final List<NuminousItemStack> results;
    private final Map<NuminousProfession, Integer> requiredProfessionLevel;
    private final int experience;
    private final int stamina;
    private final Material workstation;
    private final Material iconMaterial;

    public NuminousRecipe(String name,
                          List<NuminousItemStack> ingredients,
                          List<NuminousItemStack> results,
                          Map<NuminousProfession, Integer> requiredProfessionLevel,
                          int experience,
                          int stamina,
                          Material workstation,
                          Material iconMaterial) {
        this.name = name;
        this.ingredients = ingredients;
        this.results = results;
        this.requiredProfessionLevel = requiredProfessionLevel;
        this.experience = experience;
        this.stamina = stamina;
        this.workstation = workstation;
        this.iconMaterial = iconMaterial;
    }

    public String getName() {
        return name;
    }

    public List<NuminousItemStack> getIngredients() {
        return ingredients;
    }

    public List<NuminousItemStack> getResults() {
        return results;
    }

    public List<NuminousProfession> getApplicableProfessions() {
        return requiredProfessionLevel.keySet().stream().toList();
    }

    public Integer getRequiredProfessionLevel(NuminousProfession profession) {
        return requiredProfessionLevel.get(profession);
    }

    public int getExperience() {
        return experience;
    }

    public int getStamina() {
        return stamina;
    }

    public Material getWorkstation() {
        return workstation;
    }

    public ItemStack getIcon(Player player) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        boolean professionRequirementsMet = isProfessionRequirementsMet(player);
        boolean ingredientRequirementsMet = isIngredientRequirementsMet(player);
        boolean craftable = professionRequirementsMet && ingredientRequirementsMet;
        ItemStack icon = new ItemStack(iconMaterial);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((craftable ? GREEN : RED) + getName());
            List<String> lore = new ArrayList<>();
            lore.add(BOLD.toString() + WHITE + "Ingredients:");
            for (NuminousItemStack ingredient : getIngredients()) {
                int amountOwned = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (ingredient.getItemType().isItem(item)) {
                        amountOwned += item.getAmount();
                    }
                }
                lore.add((amountOwned >= ingredient.getAmount() ? GREEN : RED) + "• " + ingredient.getItemType().getName() + " × " + ingredient.getAmount());
            }
            lore.add("");
            lore.add(BOLD.toString() + WHITE + "Results:");
            for (NuminousItemStack result : getResults()) {
                lore.add(GRAY + "• " + result.getItemType().getName() + " × " + result.getAmount());
            }
            lore.add("");
            lore.add(BOLD.toString() + WHITE + "Required profession: ");
            getApplicableProfessions().forEach(profession -> {
                NuminousProfession playerProfession = professionService.getProfession(player);
                boolean isThisProfessionRequirementMet = playerProfession != null
                        && playerProfession.getId().equals(profession.getId())
                        && professionService.getProfessionLevel(player) >= getRequiredProfessionLevel(profession);
                lore.add((isThisProfessionRequirementMet ? GREEN : RED) + "• Lv" + getRequiredProfessionLevel(profession) + " " + profession.getName());
            });
            meta.setLore(lore);
            meta.addItemFlags(
                    HIDE_ENCHANTS,
                    HIDE_ATTRIBUTES,
                    HIDE_UNBREAKABLE,
                    HIDE_DESTROYS,
                    HIDE_PLACED_ON,
                    HIDE_POTION_EFFECTS,
                    HIDE_DYE
            );
        }
        icon.setItemMeta(meta);
        return icon;
    }

    public void use(Player player) {
        for (NuminousItemStack ingredient : getIngredients()) {
            int remainingAmount = ingredient.getAmount();
            ItemStack[] inventoryContents = player.getInventory().getContents();
            for (ItemStack itemStack : inventoryContents) {
                if (ingredient.getItemType().isItem(itemStack)) {
                    int amount = itemStack.getAmount();
                    if (remainingAmount >= amount) {
                        player.getInventory().removeItem(itemStack);
                        remainingAmount -= amount;
                    } else {
                        ItemStack partialItem = new ItemStack(itemStack);
                        partialItem.setAmount(remainingAmount);
                        player.getInventory().removeItem(partialItem);
                        remainingAmount = 0;
                        break;
                    }
                }
            }
            if (remainingAmount != 0) {
                throw new IllegalStateException("Player " + player.getName() + " did not have enough of the required ingredients for recipe " + getName());
            }
        }
        for (NuminousItemStack result : getResults()) {
            player.getInventory().addItem(result.toItemStack());
        }
    }

    public boolean isProfessionRequirementsMet(Player player) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        NuminousProfession profession = professionService.getProfession(player);
        if (profession == null) return false;
        int professionLevel = professionService.getProfessionLevel(player);
        return getApplicableProfessions().stream().anyMatch((applicableProfession) -> applicableProfession.getId().equals(profession.getId()))
                && professionLevel >= getRequiredProfessionLevel(profession);
    }

    public boolean isIngredientRequirementsMet(Player player) {
        return getIngredients().stream().allMatch(ingredient -> {
            int amount = Arrays.stream(player.getInventory().getContents())
                    .filter(item -> ingredient.getItemType().isItem(item))
                    .map(ItemStack::getAmount)
                    .reduce(Integer::sum)
                    .orElse(0);
            return amount >= ingredient.getAmount();
        });
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "name", getName(),
                "ingredients", getIngredients(),
                "results", getResults(),
                "required-profession-level", getApplicableProfessions().stream()
                        .collect(Collectors.toMap(
                                NuminousProfession::getId,
                                this::getRequiredProfessionLevel
                        )),
                "experience", getExperience(),
                "stamina", getStamina(),
                "workstation", getWorkstation().name(),
                "icon", iconMaterial.name()
        );
    }

    public static NuminousRecipe deserialize(Map<String, Object> serialized) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        return new NuminousRecipe(
                (String) serialized.get("name"),
                (List<NuminousItemStack>) serialized.get("ingredients"),
                (List<NuminousItemStack>) serialized.get("results"),
                ((Map<String, Integer>) serialized.get("required-profession-level")).entrySet().stream()
                        .map(entry -> Map.entry(
                                professionService.getProfessionById(entry.getKey()),
                                entry.getValue()
                        )).collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )),
                (Integer) serialized.get("experience"),
                (Integer) serialized.get("stamina"),
                Material.valueOf((String) serialized.get("workstation")),
                Material.valueOf((String) serialized.get("icon"))
        );
    }
}
