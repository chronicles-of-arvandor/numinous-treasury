package net.arvandor.numinoustreasury.mixpanel.event;

import com.rpkit.characters.bukkit.character.RPKCharacterId;
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelEvent;
import net.arvandor.numinoustreasury.profession.NuminousProfession;

import java.util.Map;
import java.util.UUID;

public final class NuminousMixpanelProfessionSetEvent implements NuminousMixpanelEvent {

    private final UUID minecraftUuid;
    private final RPKCharacterId characterId;
    private final NuminousProfession profession;

    public NuminousMixpanelProfessionSetEvent(UUID minecraftUuid, RPKCharacterId characterId, NuminousProfession profession) {
        this.minecraftUuid = minecraftUuid;
        this.characterId = characterId;
        this.profession = profession;
    }

    @Override
    public String getDistinctId() {
        if (minecraftUuid == null) return null;
        return minecraftUuid.toString();
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
