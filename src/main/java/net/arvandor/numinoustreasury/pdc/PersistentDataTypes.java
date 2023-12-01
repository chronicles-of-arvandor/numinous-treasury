package net.arvandor.numinoustreasury.pdc;

import net.arvandor.numinoustreasury.NuminousTreasury;

public final class PersistentDataTypes {

    private final ItemStackDataType itemStack;

    public PersistentDataTypes(NuminousTreasury plugin) {
        this.itemStack = new ItemStackDataType(plugin);
    }

    public ItemStackDataType itemStack() {
        return itemStack;
    }
}
