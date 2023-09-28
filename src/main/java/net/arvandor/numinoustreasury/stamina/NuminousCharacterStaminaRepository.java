package net.arvandor.numinoustreasury.stamina;

import com.rpkit.characters.bukkit.character.RPKCharacterId;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.jooq.DSLContext;

import static net.arvandor.numinoustreasury.jooq.Tables.NUMINOUS_CHARACTER_STAMINA;
import static org.jooq.impl.DSL.least;
import static org.jooq.impl.DSL.value;

public final class NuminousCharacterStaminaRepository {

    private final NuminousTreasury plugin;
    private final DSLContext dsl;
    private final int maxStamina;

    public NuminousCharacterStaminaRepository(NuminousTreasury plugin, DSLContext dsl) {
        this.plugin = plugin;
        this.dsl = dsl;
        this.maxStamina = plugin.getConfig().getInt("stamina.max");
    }

    public int getStamina(RPKCharacterId characterId) {
        return dsl.select(NUMINOUS_CHARACTER_STAMINA.STAMINA)
                .from(NUMINOUS_CHARACTER_STAMINA)
                .where(NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.getValue()))
                .fetchOptional(NUMINOUS_CHARACTER_STAMINA.STAMINA)
                .orElse(maxStamina);
    }

    public void getAndUpdate(RPKCharacterId characterId, StaminaUpdateFunction function, StaminaUpdateCallback callback) {
        dsl.transaction(config -> {
            DSLContext transactionalDsl = config.dsl();
            int stamina = transactionalDsl
                    .select(NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .from(NUMINOUS_CHARACTER_STAMINA)
                    .where(NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.getValue()))
                    .forUpdate()
                    .fetchOptional(NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .orElse(maxStamina);
            function.invoke(transactionalDsl, stamina);
            int newStamina = transactionalDsl
                    .select(NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .from(NUMINOUS_CHARACTER_STAMINA)
                    .where(NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.getValue()))
                    .fetchOptional(NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .orElse(maxStamina);
            callback.invoke(stamina, newStamina);
        });
    }

    public void setStamina(RPKCharacterId characterId, int stamina) {
        setStamina(dsl, characterId, stamina);
    }

    public void setStamina(DSLContext dsl, RPKCharacterId characterId, int stamina) {
        dsl.insertInto(NUMINOUS_CHARACTER_STAMINA)
                .set(NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID, characterId.getValue())
                .set(NUMINOUS_CHARACTER_STAMINA.STAMINA, stamina)
                .onConflict(NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID).doUpdate()
                .set(NUMINOUS_CHARACTER_STAMINA.STAMINA, stamina)
                .where(NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.getValue()))
                .execute();
    }

    public void restoreStamina() {
        dsl.update(NUMINOUS_CHARACTER_STAMINA)
                .set(
                        NUMINOUS_CHARACTER_STAMINA.STAMINA,
                        least(NUMINOUS_CHARACTER_STAMINA.STAMINA.plus(1), value(maxStamina))
                )
                .execute();
    }

}
