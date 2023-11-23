package net.arvandor.numinoustreasury.mixpanel;

import org.bukkit.OfflinePlayer;

import java.util.Map;

public interface NuminousMixpanelEvent {

    OfflinePlayer getPlayer();
    String getEventName();
    Map<String, Object> getProps();

}
