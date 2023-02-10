package net.kingdommc.darkages.numinoustreasury.stamina;

import org.jooq.DSLContext;

public interface StaminaUpdateFunction {
    void invoke(DSLContext ctx, int stamina);
}
