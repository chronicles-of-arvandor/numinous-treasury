package net.kingdommc.darkages.numinoustreasury.stamina;

import com.rpkit.characters.bukkit.character.RPKCharacter;

import java.util.Arrays;

import static net.md_5.bungee.api.ChatColor.*;

public enum StaminaTier {

    TIER_1(1, 81, 100, GREEN + "You feel energised.", GREEN + "%s feels energised."),
    TIER_2(2, 61, 80, DARK_AQUA + "You feel fine.", DARK_AQUA + "%s feels fine."),
    TIER_3(3, 41, 60, YELLOW + "You're starting to tire. Perhaps you should take a break.", YELLOW + "%s is starting to tire. Perhaps they should take a break."),
    TIER_4(4, 21, 40, GRAY + "You're absolutely exhausted, you can't carry on for much longer.", GRAY + "%s is absolutely exhausted, they can't carry on for much longer."),
    TIER_5(5, 1, 20, RED + "You feel like you're about to collapse.", RED + "%s feels like they're about to collapse."),
    TIER_6(6, 0, 0, DARK_RED + "You are physically incapable of moving.", DARK_RED + "%s is physically incapable of moving.");

    private final int tier;
    private final int minStaminaPercent;
    private final int maxStaminaPercent;
    private final String messageSelf;
    private final String messageTemplateOther;

    StaminaTier(int tier, int minStaminaPercent, int maxStaminaPercent, String messageSelf, String messageTemplateOther) {
        this.tier = tier;
        this.minStaminaPercent = minStaminaPercent;
        this.maxStaminaPercent = maxStaminaPercent;
        this.messageSelf = messageSelf;
        this.messageTemplateOther = messageTemplateOther;
    }

    public int getTier() {
        return tier;
    }

    public int getMinStaminaPercent() {
        return minStaminaPercent;
    }

    public int getMaxStaminaPercent() {
        return maxStaminaPercent;
    }

    public String getMessageSelf() {
        return messageSelf;
    }

    public String getMessageOther(RPKCharacter character) {
        return String.format(messageTemplateOther, character.getName());
    }

    public static StaminaTier forStaminaPercentage(int staminaPercent) {
        return Arrays.stream(StaminaTier.values())
                .filter(tier -> staminaPercent >= tier.getMinStaminaPercent()
                        && staminaPercent <= tier.getMaxStaminaPercent())
                .findFirst()
                .orElse(null);
    }

    public static StaminaTier forStamina(int stamina, int maxStamina) {
        return forStaminaPercentage((int) Math.round(((double) stamina / (double) maxStamina) * 100.0));
    }

    public static String messageForStaminaTransition(int oldStamina, int newStamina, int maxStamina) {
        StaminaTier oldStaminaTier = forStamina(oldStamina, maxStamina);
        StaminaTier newStaminaTier = forStamina(newStamina, maxStamina);
        if (oldStaminaTier != newStaminaTier) {
            if (newStamina > oldStamina) {
                return GREEN + "You feel yourself recouping a little of your energy. Your stamina has increased.";
            } else {
                return newStaminaTier.getMessageSelf();
            }
        }
        return null;
    }

}
