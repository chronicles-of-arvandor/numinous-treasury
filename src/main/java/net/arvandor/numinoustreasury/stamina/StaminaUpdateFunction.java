package net.arvandor.numinoustreasury.stamina;

import org.jooq.DSLContext;

public interface StaminaUpdateFunction {
    void invoke(DSLContext ctx, int stamina);
}
