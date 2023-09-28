package net.arvandor.numinoustreasury.stamina;

public interface StaminaUpdateCallback {
    void invoke(int oldStamina, int newStamina);
}
