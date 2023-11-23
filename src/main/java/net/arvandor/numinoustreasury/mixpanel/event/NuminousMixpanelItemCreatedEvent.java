package net.arvandor.numinoustreasury.mixpanel.event;

import net.arvandor.numinoustreasury.item.NuminousItemType;
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelEvent;
import org.bukkit.OfflinePlayer;

import java.util.Map;

public final class NuminousMixpanelItemCreatedEvent implements NuminousMixpanelEvent {

    private final OfflinePlayer player;
    private final NuminousItemType itemType;
    private final int amount;
    private final String source;

    public NuminousMixpanelItemCreatedEvent(OfflinePlayer player, NuminousItemType itemType, int amount, String source) {
        this.player = player;
        this.itemType = itemType;
        this.amount = amount;
        this.source = source;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public String getEventName() {
        return "Item Created";
    }

    @Override
    public Map<String, Object> getProps() {
        return Map.of(
                "Item Type", itemType.getName(),
                "Amount", amount,
                "Source", source
        );
    }

}
