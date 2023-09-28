package net.arvandor.numinoustreasury.area;

import org.bukkit.Location;
import org.bukkit.World;

public final class Area {

    private final World world;
    private final Location minLocation;
    private final Location maxLocation;

    public Area(World world, Location location1, Location location2) {
        this.world = world;
        if (location1.getWorld() != world || location2.getWorld() != world) {
            throw new IllegalArgumentException("Both location worlds must match world");
        }
        this.minLocation = new Location(
                location1.getWorld(),
                Math.min(location1.getX(), location2.getX()),
                Math.min(location1.getY(), location2.getY()),
                Math.min(location1.getZ(), location2.getZ())
        );
        this.maxLocation = new Location(
                location1.getWorld(),
                Math.max(location1.getX(), location2.getX()),
                Math.max(location1.getY(), location2.getY()),
                Math.max(location1.getZ(), location2.getZ())
        );
    }

    public World getWorld() {
        return world;
    }

    public Location getMinLocation() {
        return minLocation;
    }

    public Location getMaxLocation() {
        return maxLocation;
    }
    public boolean contains(Location location) {
        return getWorld() == location.getWorld()
                && getMinLocation().getX() <= location.getX()
                && getMinLocation().getY() <= location.getY()
                && getMinLocation().getZ() <= location.getZ()
                && getMaxLocation().getX() >= location.getX()
                && getMaxLocation().getY() >= location.getY()
                && getMaxLocation().getZ() >= location.getZ();
    }
}
