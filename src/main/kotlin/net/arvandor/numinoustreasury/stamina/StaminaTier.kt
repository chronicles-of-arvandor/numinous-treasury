package net.arvandor.numinoustreasury.stamina

import com.rpkit.characters.bukkit.character.RPKCharacter
import net.md_5.bungee.api.ChatColor

enum class StaminaTier(
    val tier: Int,
    val minStaminaPercent: Int,
    val maxStaminaPercent: Int,
    val messageSelf: String,
    private val messageTemplateOther: String,
) {
    TIER_1(
        1,
        81,
        100,
        ChatColor.GREEN.toString() + "You feel energised.",
        ChatColor.GREEN.toString() + "%s feels energised.",
    ),
    TIER_2(
        2,
        61,
        80,
        ChatColor.DARK_AQUA.toString() + "You feel fine.",
        ChatColor.DARK_AQUA.toString() + "%s feels fine.",
    ),
    TIER_3(
        3,
        41,
        60,
        ChatColor.YELLOW.toString() + "You're starting to tire. Perhaps you should take a break.",
        ChatColor.YELLOW.toString() + "%s is starting to tire. Perhaps they should take a break.",
    ),
    TIER_4(
        4,
        21,
        40,
        ChatColor.GRAY.toString() + "You're absolutely exhausted, you can't carry on for much longer.",
        ChatColor.GRAY.toString() + "%s is absolutely exhausted, they can't carry on for much longer.",
    ),
    TIER_5(
        5,
        1,
        20,
        ChatColor.RED.toString() + "You feel like you're about to collapse.",
        ChatColor.RED.toString() + "%s feels like they're about to collapse.",
    ),
    TIER_6(
        6,
        0,
        0,
        ChatColor.DARK_RED.toString() + "You are physically incapable of moving.",
        ChatColor.DARK_RED.toString() + "%s is physically incapable of moving.",
    ),
    ;

    fun getMessageOther(character: RPKCharacter): String {
        return String.format(messageTemplateOther, character.name)
    }

    companion object {
        fun forStaminaPercentage(staminaPercent: Int): StaminaTier {
            return entries.toTypedArray()
                .first { tier -> staminaPercent >= tier.minStaminaPercent && staminaPercent <= tier.maxStaminaPercent }
        }

        fun forStamina(
            stamina: Int,
            maxStamina: Int,
        ): StaminaTier {
            return forStaminaPercentage(Math.round((stamina.toDouble() / maxStamina.toDouble()) * 100.0).toInt())
        }

        fun messageForStaminaTransition(
            oldStamina: Int,
            newStamina: Int,
            maxStamina: Int,
        ): String? {
            val oldStaminaTier = forStamina(oldStamina, maxStamina)
            val newStaminaTier = forStamina(newStamina, maxStamina)
            if (oldStaminaTier != newStaminaTier) {
                return if (newStamina > oldStamina) {
                    ChatColor.GREEN.toString() + "You feel yourself recouping a little of your energy. Your stamina has increased."
                } else {
                    newStaminaTier.messageSelf
                }
            }
            return null
        }
    }
}
