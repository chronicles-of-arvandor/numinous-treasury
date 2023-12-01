package net.arvandor.numinoustreasury.pdc;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemStackDataType implements PersistentDataType<byte[], ItemStack> {

    private final JavaPlugin plugin;

    public ItemStackDataType(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<ItemStack> getComplexType() {
        return ItemStack.class;
    }

    @Override
    public byte[] toPrimitive(ItemStack complex, PersistentDataAdapterContext context) {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)
        ) {
            boos.writeObject(complex);
            return baos.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public ItemStack fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(primitive);
                BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)
        ) {
            return (ItemStack) bois.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }
}
