package net.arvandor.numinoustreasury.mixpanel.event;

import net.arvandor.numinoustreasury.item.NuminousItemType;
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelEvent;

import java.util.Map;
import java.util.UUID;

public final class NuminousMixpanelItemCreatedEvent implements NuminousMixpanelEvent {

    private final UUID minecraftUuid;
    private final NuminousItemType itemType;
    private final int amount;
    private final String source;

    public NuminousMixpanelItemCreatedEvent(UUID minecraftUuid, NuminousItemType itemType, int amount, String source) {
        this.minecraftUuid = minecraftUuid;
        this.itemType = itemType;
        this.amount = amount;
        this.source = source;
    }

    @Override
    public String getDistinctId() {
        if (minecraftUuid == null) return null;
        return minecraftUuid.toString();
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
