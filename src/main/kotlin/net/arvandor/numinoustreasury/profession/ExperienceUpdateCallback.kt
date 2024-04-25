package net.arvandor.numinoustreasury.profession

fun interface ExperienceUpdateCallback {
    operator fun invoke(
        oldExperience: Int,
        newExperience: Int,
    )
}
