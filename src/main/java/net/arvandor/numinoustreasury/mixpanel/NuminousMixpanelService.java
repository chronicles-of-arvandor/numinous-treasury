package net.arvandor.numinoustreasury.mixpanel;

import static java.util.logging.Level.WARNING;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.rpkit.core.service.Service;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.OfflinePlayer;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public final class NuminousMixpanelService implements Service {

    private final NuminousTreasury plugin;
    private final String token;
    private final MixpanelAPI mixpanel;

    public NuminousMixpanelService(NuminousTreasury plugin) {
        this.plugin = plugin;
        token = plugin.getConfig().getString("mixpanel.token");
        mixpanel = new MixpanelAPI();
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public void trackEvent(NuminousMixpanelEvent event) {
        MessageBuilder messageBuilder = new MessageBuilder(token);

        JSONObject props = new JSONObject();
        if (event.getPlayer() != null) {
            if (event.getPlayer().isOnline()) {
                String ip = event.getPlayer().getPlayer().getAddress().getAddress().getHostAddress();
                props.put("ip", ip);
            }
        }
        event.getProps().forEach((key, value) -> {
            props.put(key, JSONObject.wrap(value));
        });

        String distinctId = event.getPlayer() != null ? event.getPlayer().getUniqueId().toString() : null;
        JSONObject jsonEvent = messageBuilder.event(distinctId, event.getEventName(), props);
        try {
            mixpanel.sendMessage(jsonEvent);
        } catch (IOException exception) {
            plugin.getLogger().log(WARNING, "Failed to send Mixpanel event", exception);
        }
    }

    public void updateUserProps(OfflinePlayer player, Map<String, Object> props) {
        MessageBuilder messageBuilder = new MessageBuilder(token);

        JSONObject jsonProps = new JSONObject();
        if (player != null) {
            if (player.isOnline()) {
                String ip = player.getPlayer().getAddress().getAddress().getHostAddress();
                props.put("ip", ip);
            }
        }
        props.forEach((key, value) -> {
            jsonProps.put(key, JSONObject.wrap(value));
        });

        String distinctId = player != null ? player.getUniqueId().toString() : null;
        JSONObject update = messageBuilder.set(distinctId, jsonProps);
        try {
            mixpanel.sendMessage(update);
        } catch (IOException exception) {
            plugin.getLogger().log(WARNING, "Failed to send Mixpanel user update", exception);
        }
    }

}
