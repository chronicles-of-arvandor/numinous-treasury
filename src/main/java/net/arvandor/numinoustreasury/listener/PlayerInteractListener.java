package net.arvandor.numinoustreasury.listener;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.interaction.NuminousInteractionService;
import net.arvandor.numinoustreasury.interaction.NuminousInteractionStatus;
import net.arvandor.numinoustreasury.item.NuminousItemStack;
import net.arvandor.numinoustreasury.item.action.NuminousOnInteractAir;
import net.arvandor.numinoustreasury.item.action.NuminousOnInteractBlock;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import net.arvandor.numinoustreasury.recipe.NuminousRecipe;
import net.arvandor.numinoustreasury.recipe.NuminousRecipeService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession;
import net.arvandor.numinoustreasury.workstation.WorkstationInterface;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

import static net.md_5.bungee.api.ChatColor.GREEN;
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
        onNodeSetup(event);
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

    private void onNodeSetup(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        if (event.getAction() != RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        NuminousInteractionService interactionService = Services.INSTANCE.get(NuminousInteractionService.class);
        NuminousInteractionStatus status = interactionService.getInteractionStatus(event.getPlayer());
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        if (status == null) return;
        NuminousNodeCreationSession session = nodeService.getNodeCreationSession(event.getPlayer());
        if (session == null) return;
        event.setCancelled(true);
        switch (status) {
            case SELECTING_ENTRANCE_AREA_1 -> {
                session.setEntranceAreaLocation1(event.getClickedBlock().getLocation());
                interactionService.setInteractionStatus(event.getPlayer(), NuminousInteractionStatus.SELECTING_ENTRANCE_AREA_2);
                event.getPlayer().sendMessage(GREEN + "Entrance area location 1 set, please select location 2.");
            }
            case SELECTING_ENTRANCE_AREA_2 -> {
                session.setEntranceAreaLocation2(event.getClickedBlock().getLocation());
                interactionService.setInteractionStatus(event.getPlayer(), null);
                session.display(event.getPlayer());
            }
            case SELECTING_EXIT_AREA_1 -> {
                session.setExitAreaLocation1(event.getClickedBlock().getLocation());
                interactionService.setInteractionStatus(event.getPlayer(), NuminousInteractionStatus.SELECTING_EXIT_AREA_2);
                event.getPlayer().sendMessage(GREEN + "Exit area location 1 set, please select location 2.");
            }
            case SELECTING_EXIT_AREA_2 -> {
                session.setExitAreaLocation2(event.getClickedBlock().getLocation());
                interactionService.setInteractionStatus(event.getPlayer(), null);
                session.display(event.getPlayer());
            }
            case SELECTING_AREA_1 -> {
                session.setAreaLocation1(event.getClickedBlock().getLocation());
                interactionService.setInteractionStatus(event.getPlayer(), NuminousInteractionStatus.SELECTING_AREA_2);
                event.getPlayer().sendMessage(GREEN + "Area location 1 set, please select location 2.");
            }
            case SELECTING_AREA_2 -> {
                session.setAreaLocation2(event.getClickedBlock().getLocation());
                interactionService.setInteractionStatus(event.getPlayer(), null);
                session.display(event.getPlayer());
            }
        }
    }

}
