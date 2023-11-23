package net.arvandor.numinoustreasury.mixpanel;

import static java.util.logging.Level.WARNING;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.rpkit.core.service.Service;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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
        event.getProps().forEach((key, value) -> {
            props.put(key, JSONObject.wrap(value));
        });

        JSONObject jsonEvent = messageBuilder.event(event.getDistinctId(), event.getEventName(), props);
        try {
            mixpanel.sendMessage(jsonEvent);
        } catch (IOException exception) {
            plugin.getLogger().log(WARNING, "Failed to send Mixpanel event", exception);
        }
    }

    public void updateUserProps(UUID minecraftUuid, Map<String, Object> props) {
        MessageBuilder messageBuilder = new MessageBuilder(token);

        JSONObject jsonProps = new JSONObject();
        props.forEach((key, value) -> {
            jsonProps.put(key, JSONObject.wrap(value));
        });

        JSONObject update = messageBuilder.set(minecraftUuid.toString(), jsonProps);
        try {
            mixpanel.sendMessage(update);
        } catch (IOException exception) {
            plugin.getLogger().log(WARNING, "Failed to send Mixpanel user update", exception);
        }
    }

}
