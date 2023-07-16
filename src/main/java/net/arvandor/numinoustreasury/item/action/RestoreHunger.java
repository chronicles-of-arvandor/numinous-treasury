package net.arvandor.numinoustreasury.item.action;

import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

@SerializableAs("RestoreHunger")
public final class RestoreHunger implements NuminousOnEat, NuminousOnInteractAir, NuminousOnInteractBlock {

    private final int hunger;
    private final float saturation;

    public RestoreHunger(int hunger, float saturation) {
        this.hunger = hunger;
        this.saturation = saturation;
    }

    public int getHunger() {
        return hunger;
    }

    public float getSaturation() {
        return saturation;
    }

    @Override
    public void onEat(PlayerItemConsumeEvent event) {
        consumeItem(event, event.getHand());
    }

    @Override
    public void onInteractAir(PlayerInteractEvent event) {
        consumeItem(event, event.getHand());
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        consumeItem(event, event.getHand());
    }

    private void consumeItem(PlayerEvent event, EquipmentSlot hand) {
        if (event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
        Player player = event.getPlayer();
        ItemStack itemInUse = player.getItemInUse();
        if (itemInUse == null) return;
        PlayerInventory inventory = player.getInventory();
        if (itemInUse.getAmount() == 1) {
            inventory.setItem(hand, null);
        } else {
            player.getItemInUse().setAmount(player.getItemInUse().getAmount() - 1);
        }
        player.setFoodLevel(player.getFoodLevel() + getHunger());
        player.setSaturation(player.getSaturation() + getSaturation());
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "hunger", getHunger(),
                "saturation", getSaturation()
        );
    }

    public static RestoreHunger deserialize(Map<String, Object> serialized) {
        return new RestoreHunger(
                (Integer) serialized.get("hunger"),
                (float) (double) serialized.get("saturation")
        );
    }
}
