package net.arvandor.numinoustreasury.stamina

import com.rpkit.characters.bukkit.character.RPKCharacterId
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.jooq.Tables
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DSL

class NuminousCharacterStaminaRepository(private val plugin: NuminousTreasury, private val dsl: DSLContext) {
    private val maxStamina = plugin.config.getInt("stamina.max")

    fun getStamina(characterId: RPKCharacterId): Int {
        return dsl.select(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA)
            .from(Tables.NUMINOUS_CHARACTER_STAMINA)
            .where(Tables.NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.value))
            .fetchOptional(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA)
            .orElse(maxStamina)
    }

    fun getAndUpdate(
        characterId: RPKCharacterId,
        function: StaminaUpdateFunction,
        callback: StaminaUpdateCallback,
    ) {
        dsl.transaction { config: Configuration ->
            val transactionalDsl = config.dsl()
            val stamina =
                transactionalDsl
                    .select(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .from(Tables.NUMINOUS_CHARACTER_STAMINA)
                    .where(Tables.NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.value))
                    .forUpdate()
                    .fetchOptional(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .orElse(maxStamina)
            function.invoke(transactionalDsl, stamina)
            val newStamina =
                transactionalDsl
                    .select(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .from(Tables.NUMINOUS_CHARACTER_STAMINA)
                    .where(Tables.NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.value))
                    .fetchOptional(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA)
                    .orElse(maxStamina)
            callback.invoke(stamina, newStamina)
        }
    }

    fun setStamina(
        characterId: RPKCharacterId,
        stamina: Int,
    ) {
        setStamina(dsl, characterId, stamina)
    }

    fun setStamina(
        dsl: DSLContext,
        characterId: RPKCharacterId,
        stamina: Int,
    ) {
        dsl.insertInto(Tables.NUMINOUS_CHARACTER_STAMINA)
            .set(Tables.NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID, characterId.value)
            .set(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA, stamina)
            .onConflict(Tables.NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID).doUpdate()
            .set(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA, stamina)
            .where(Tables.NUMINOUS_CHARACTER_STAMINA.CHARACTER_ID.eq(characterId.value))
            .execute()
    }

    fun restoreStamina() {
        dsl.update(Tables.NUMINOUS_CHARACTER_STAMINA)
            .set(
                Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA,
                DSL.least(Tables.NUMINOUS_CHARACTER_STAMINA.STAMINA.plus(1), DSL.value(maxStamina)),
            )
            .execute()
    }
}
