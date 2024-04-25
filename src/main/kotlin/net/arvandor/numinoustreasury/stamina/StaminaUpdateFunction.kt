package net.arvandor.numinoustreasury.stamina

import org.jooq.DSLContext

fun interface StaminaUpdateFunction {
    operator fun invoke(
        ctx: DSLContext,
        stamina: Int,
    )
}
