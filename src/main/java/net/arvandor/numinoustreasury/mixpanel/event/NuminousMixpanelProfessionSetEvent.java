package net.arvandor.numinoustreasury.mixpanel.event;

import com.rpkit.characters.bukkit.character.RPKCharacterId;
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelEvent;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import org.bukkit.OfflinePlayer;

import java.util.Map;

public final class NuminousMixpanelProfessionSetEvent implements NuminousMixpanelEvent {

    private final OfflinePlayer player;
    private final RPKCharacterId characterId;
    private final NuminousProfession profession;

    public NuminousMixpanelProfessionSetEvent(OfflinePlayer player, RPKCharacterId characterId, NuminousProfession profession) {
        this.player = player;
        this.characterId = characterId;
        this.profession = profession;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public String getEventName() {
        return "Profession Set";
    }

    @Override
    public Map<String, Object> getProps() {
        return Map.of(
                "Character ID", characterId.getValue(),
                "Profession", profession.getName()
        );
    }
}
