package net.kingdommc.darkages.numinoustreasury.profession;

import com.rpkit.characters.bukkit.character.RPKCharacterId;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Objects;

import static net.kingdommc.darkages.numinoustreasury.jooq.Tables.NUMINOUS_CHARACTER_PROFESSION;

public final class NuminousCharacterProfessionRepository {

    private final DSLContext dsl;

    public NuminousCharacterProfessionRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public String getProfessionId(RPKCharacterId characterId) {
        return dsl.select(NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID)
                .from(NUMINOUS_CHARACTER_PROFESSION)
                .where(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.getValue()))
                .fetchOne(NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID);
    }

    public void setProfessionId(RPKCharacterId characterId, String professionId) {
        dsl.insertInto(NUMINOUS_CHARACTER_PROFESSION)
                .set(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID, characterId.getValue())
                .set(NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID, professionId)
                .set(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE, 0)
                .onConflict(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID).doUpdate()
                .set(NUMINOUS_CHARACTER_PROFESSION.PROFESSION_ID, professionId)
                .set(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE, 0)
                .where(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.getValue()))
                .execute();
    }

    public int getProfessionExperience(RPKCharacterId characterId) {
        Integer experience = dsl.select(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
                .from(NUMINOUS_CHARACTER_PROFESSION)
                .where(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.getValue()))
                .fetchOne(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE);
        if (experience == null) return 0;
        return experience;
    }

    public void getAndUpdate(RPKCharacterId characterId, ExperienceUpdateFunction function, ExperienceUpdateCallback callback) {
        dsl.transaction(config -> {
            Integer experience = DSL.using(config)
                    .select(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
                    .from(NUMINOUS_CHARACTER_PROFESSION)
                    .where(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.getValue()))
                    .forUpdate()
                    .fetchOne(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE);
            function.invoke(DSL.using(config), Objects.requireNonNullElse(experience, 0));
            Integer newExperience = DSL.using(config)
                    .select(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE)
                    .from(NUMINOUS_CHARACTER_PROFESSION)
                    .where(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.getValue()))
                    .fetchOne(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE);
            callback.invoke(Objects.requireNonNullElse(newExperience, 0));
        });
    }

    public void setProfessionExperience(RPKCharacterId characterId, int experience) {
        setProfessionExperience(dsl, characterId, experience);
    }

    public void setProfessionExperience(DSLContext dsl, RPKCharacterId characterId, int experience) {
        dsl.update(NUMINOUS_CHARACTER_PROFESSION)
                .set(NUMINOUS_CHARACTER_PROFESSION.EXPERIENCE, experience)
                .where(NUMINOUS_CHARACTER_PROFESSION.CHARACTER_ID.eq(characterId.getValue()))
                .execute();
    }

}
