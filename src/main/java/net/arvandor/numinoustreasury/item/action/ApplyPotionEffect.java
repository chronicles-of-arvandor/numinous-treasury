package net.arvandor.numinoustreasury.item.action;

import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;

@SerializableAs("ApplyPotionEffect")
public final class ApplyPotionEffect implements NuminousOnEat, NuminousOnInteractAir, NuminousOnInteractBlock {

    private final List<PotionEffect> potionEffects;

    public ApplyPotionEffect(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    @Override
    public void onEat(PlayerItemConsumeEvent event) {
        consumeItem(event);
    }

    @Override
    public void onInteractAir(PlayerInteractEvent event) {
        consumeItem(event);
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        consumeItem(event);
    }

    private void consumeItem(PlayerEvent event) {
        if (event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
        Player player = event.getPlayer();
        ItemStack itemInUse = player.getItemInUse();
        if (itemInUse == null) return;
        PlayerInventory inventory = player.getInventory();
        if (itemInUse.getAmount() == 1) {
            if (itemInUse == inventory.getItemInMainHand()) {
                inventory.setItemInMainHand(null);
            } else if (itemInUse == inventory.getItemInOffHand()) {
                inventory.setItemInOffHand(null);
            }
        } else {
            player.getItemInUse().setAmount(player.getItemInUse().getAmount() - 1);
        }
        player.addPotionEffects(getPotionEffects());
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "potion-effects", getPotionEffects()
        );
    }

    public static ApplyPotionEffect deserialize(Map<String, Object> serialized) {
        return new ApplyPotionEffect(
                (List<PotionEffect>) serialized.get("potion-effects")
        );
    }
}
