package net.arvandor.numinoustreasury.mixpanel.event

import com.rpkit.characters.bukkit.character.RPKCharacterId
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelEvent
import net.arvandor.numinoustreasury.profession.NuminousProfession
import org.bukkit.OfflinePlayer

class NuminousMixpanelProfessionSetEvent(
    override val player: OfflinePlayer,
    private val characterId: RPKCharacterId,
    private val profession: NuminousProfession,
) : NuminousMixpanelEvent {
    override val eventName: String
        get() = "Profession Set"

    override val props: Map<String?, Any?>
        get() =
            mapOf(
                "Character ID" to characterId.value,
                "Profession" to profession.name,
            )
}
