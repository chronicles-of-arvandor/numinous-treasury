package net.arvandor.numinoustreasury.mixpanel

import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import com.rpkit.core.service.Service
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.OfflinePlayer
import org.json.JSONObject
import java.io.IOException
import java.util.logging.Level

class NuminousMixpanelService(private val plugin: NuminousTreasury) : Service {
    private val token = plugin.config.getString("mixpanel.token")!!
    private val mixpanel = MixpanelAPI()

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }

    fun trackEvent(event: NuminousMixpanelEvent) {
        val messageBuilder = MessageBuilder(token)

        val props = JSONObject()
        val offlinePlayer = event.player
        if (offlinePlayer != null) {
            if (offlinePlayer.isOnline) {
                val ip = offlinePlayer.player?.address?.address?.hostAddress
                props.put("ip", ip)
            }
        }
        event.props.forEach { (key: String?, value: Any?) ->
            props.put(key, JSONObject.wrap(value))
        }

        val distinctId = offlinePlayer?.uniqueId?.toString()
        val jsonEvent = messageBuilder.event(distinctId, event.eventName, props)
        try {
            mixpanel.sendMessage(jsonEvent)
        } catch (exception: IOException) {
            plugin.logger.log(Level.WARNING, "Failed to send Mixpanel event", exception)
        }
    }

    fun updateUserProps(
        player: OfflinePlayer?,
        props: MutableMap<String?, Any?>,
    ) {
        val messageBuilder = MessageBuilder(token)

        val jsonProps = JSONObject()
        if (player != null) {
            if (player.isOnline) {
                val ip = player.player?.address?.address?.hostAddress
                props["ip"] = ip
            }
        }
        props.forEach { (key: String?, value: Any?) ->
            jsonProps.put(key, JSONObject.wrap(value))
        }

        val distinctId = player?.uniqueId?.toString()
        val update = messageBuilder.set(distinctId, jsonProps)
        try {
            mixpanel.sendMessage(update)
        } catch (exception: IOException) {
            plugin.logger.log(Level.WARNING, "Failed to send Mixpanel user update", exception)
        }
    }
}
