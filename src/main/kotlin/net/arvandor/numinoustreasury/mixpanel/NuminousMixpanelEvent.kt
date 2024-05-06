package net.arvandor.numinoustreasury.mixpanel

import org.bukkit.OfflinePlayer

interface NuminousMixpanelEvent {
    val player: OfflinePlayer?
    val eventName: String
    val props: Map<String?, Any?>
}
