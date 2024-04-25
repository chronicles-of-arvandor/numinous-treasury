package net.arvandor.numinoustreasury.mixpanel.event

import net.arvandor.numinoustreasury.item.NuminousItemType
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelEvent
import org.bukkit.OfflinePlayer

class NuminousMixpanelItemCreatedEvent(
    override val player: OfflinePlayer,
    private val itemType: NuminousItemType,
    private val amount: Int,
    private val source: String,
) : NuminousMixpanelEvent {
    override val eventName: String
        get() = "Item Created"

    override val props: Map<String?, Any?>
        get() =
            mapOf(
                "Item Type" to itemType.name,
                "Amount" to amount,
                "Source" to source,
            )
}
