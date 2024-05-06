package net.arvandor.numinoustreasury.stamina

fun interface StaminaUpdateCallback {
    operator fun invoke(
        oldStamina: Int,
        newStamina: Int,
    )
}
