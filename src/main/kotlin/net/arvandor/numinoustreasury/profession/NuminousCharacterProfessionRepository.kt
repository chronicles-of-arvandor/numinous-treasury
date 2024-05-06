package net.arvandor.numinoustreasury.profession

import com.rpkit.characters.bukkit.character.RPKCharacterId
import net.arvandor.numinoustreasury.jooq.Tables
import org.jooq.Configuration
import org.jooq.DSLContext

class NuminousCharacterProfessionRepository(private val dsl: DSLContext) {
    fun getProfessionId(characterId: RPKCharacterId): String? {
        return dsl.select(Tables.NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID)
            .from(Tables.NUMINOUS_CHARACTER_PROFESSION)
            .where(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.value))
            .fetchOne(Tables.NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID)
    }

    fun setProfessionId(
        characterId: RPKCharacterId,
        professionId: String?,
    ) {
        dsl.insertInto(Tables.NUMINOUS_CHARACTER_PROFESSION)
            .set(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID, characterId.value)
            .set(Tables.NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID, professionId)
            .set(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE, 0)
            .onConflict(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID).doUpdate()
            .set(Tables.NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID, professionId)
            .set(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE, 0)
            .where(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.value))
            .execute()
    }

    fun getProfessionExperience(characterId: RPKCharacterId): Int {
        return dsl.select(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
            .from(Tables.NUMINOUS_CHARACTER_PROFESSION)
            .where(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.value))
            .fetchOptional(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
            .orElse(0)
    }

    fun getAndUpdate(
        characterId: RPKCharacterId,
        function: ExperienceUpdateFunction,
        callback: ExperienceUpdateCallback,
    ) {
        dsl.transaction { config: Configuration ->
            val transactionalDsl = config.dsl()
            val experience =
                transactionalDsl
                    .select(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
                    .from(Tables.NUMINOUS_CHARACTER_PROFESSION)
                    .where(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.value))
                    .forUpdate()
                    .fetchOne(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
            function.invoke(transactionalDsl, experience ?: 0)
            val newExperience =
                transactionalDsl
                    .select(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
                    .from(Tables.NUMINOUS_CHARACTER_PROFESSION)
                    .where(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.value))
                    .fetchOne(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
            callback.invoke(experience ?: 0, newExperience ?: 0)
        }
    }

    fun setProfessionExperience(
        characterId: RPKCharacterId,
        experience: Int,
    ) {
        setProfessionExperience(dsl, characterId, experience)
    }

    fun setProfessionExperience(
        dsl: DSLContext,
        characterId: RPKCharacterId,
        experience: Int,
    ) {
        dsl.update(Tables.NUMINOUS_CHARACTER_PROFESSION)
            .set(Tables.NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE, experience)
            .where(Tables.NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.value))
            .execute()
    }
}
