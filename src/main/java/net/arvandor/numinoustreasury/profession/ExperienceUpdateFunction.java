package net.arvandor.numinoustreasury.profession;

import org.jooq.DSLContext;

public interface ExperienceUpdateFunction {
    void invoke(DSLContext ctx, int experience);
}
