package net.kingdommc.darkages.numinoustreasury.stamina;

public interface StaminaUpdateCallback {
    void invoke(int oldStamina, int newStamina);
}
