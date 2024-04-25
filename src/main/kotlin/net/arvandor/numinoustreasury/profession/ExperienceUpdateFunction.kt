package net.arvandor.numinoustreasury.profession

import org.jooq.DSLContext

fun interface ExperienceUpdateFunction {
    operator fun invoke(
        ctx: DSLContext,
        experience: Int,
    )
}
