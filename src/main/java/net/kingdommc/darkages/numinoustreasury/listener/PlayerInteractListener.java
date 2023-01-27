package net.kingdommc.darkages.numinoustreasury.listener;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import net.kingdommc.darkages.numinoustreasury.item.NuminousItemStack;
import net.kingdommc.darkages.numinoustreasury.item.action.NuminousOnInteractAir;
import net.kingdommc.darkages.numinoustreasury.item.action.NuminousOnInteractBlock;
import net.kingdommc.darkages.numinoustreasury.recipe.NuminousRecipe;
import net.kingdommc.darkages.numinoustreasury.recipe.NuminousRecipeService;
import net.kingdommc.darkages.numinoustreasury.workstation.WorkstationInterface;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public final class PlayerInteractListener implements Listener {

    private final NuminousTreasury plugin;

    public PlayerInteractListener(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        onCustomItemInteract(event);
        onWorkstationInteract(event);
    }

    private void onCustomItemInteract(PlayerInteractEvent event) {
        NuminousItemStack numinousItemStack = NuminousItemStack.fromItemStack(event.getItem());
        if (numinousItemStack == null) return;
        if (event.hasBlock()) {
            List<NuminousOnInteractBlock> onInteractBlock = numinousItemStack.getItemType().getOnInteractBlock();
            for (NuminousOnInteractBlock action : onInteractBlock) {
                action.onInteractBlock(event);
            }
        } else {
            List<NuminousOnInteractAir> onInteractAir = numinousItemStack.getItemType().getOnInteractAir();
            for (NuminousOnInteractAir action : onInteractAir) {
                action.onInteractAir(event);
            }
        }
    }

    private void onWorkstationInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        if (event.getAction() != RIGHT_CLICK_BLOCK) return;
        NuminousRecipeService recipeService = Services.INSTANCE.get(NuminousRecipeService.class);
        List<NuminousRecipe> workstationRecipes = recipeService.getRecipes().stream()
                .filter(recipe -> recipe.getWorkstation() == event.getClickedBlock().getType()).toList();
        if (workstationRecipes.isEmpty()) return;
        event.setCancelled(true);
        WorkstationInterface workstationInterface = new WorkstationInterface(plugin, event.getPlayer(), workstationRecipes);
        event.getPlayer().openInventory(workstationInterface.getInventory());
    }

}
