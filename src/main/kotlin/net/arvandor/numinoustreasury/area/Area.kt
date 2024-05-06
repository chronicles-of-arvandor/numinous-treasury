package net.arvandor.numinoustreasury.area

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.max
import kotlin.math.min

class Area(val world: World, location1: Location, location2: Location) {
    val minLocation: Location
    val maxLocation: Location

    init {
        require(!(location1.world !== world || location2.world !== world)) { "Both location worlds must match world" }
        this.minLocation =
            Location(
                location1.world,
                min(location1.x, location2.x),
                min(location1.y, location2.y),
                min(location1.z, location2.z),
            )
        this.maxLocation =
            Location(
                location1.world,
                max(location1.x, location2.x),
                max(location1.y, location2.y),
                max(location1.z, location2.z),
            )
    }

    fun contains(location: Location): Boolean {
        return world === location.world &&
            minLocation.x <= location.x &&
            minLocation.y <= location.y &&
            minLocation.z <= location.z &&
            maxLocation.x + 1 > location.x &&
            maxLocation.y + 1 > location.y &&
            maxLocation.z + 1 > location.z
    }
}
