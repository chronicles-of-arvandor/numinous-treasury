package net.arvandor.numinoustreasury.node

import net.arvandor.numinoustreasury.area.Area
import net.arvandor.numinoustreasury.droptable.NuminousDropTable
import net.arvandor.numinoustreasury.profession.NuminousProfession
import org.bukkit.Location

class NuminousNode(
    val id: String,
    val name: String,
    private val requiredProfessionLevel: Map<NuminousProfession, Int>,
    val experience: Int,
    val staminaCost: Int,
    val dropTable: NuminousDropTable,
    val entranceArea: Area,
    val entranceWarpDestination: Location,
    val exitArea: Area,
    val exitWarpDestination: Location,
    val area: Area,
) {
    val applicableProfessions: List<NuminousProfession>
        get() = requiredProfessionLevel.keys.toList()

    fun getRequiredProfessionLevel(profession: NuminousProfession): Int? {
        return requiredProfessionLevel[profession]
    }
}
