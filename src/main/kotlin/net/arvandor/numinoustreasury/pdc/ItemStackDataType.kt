package net.arvandor.numinoustreasury.pdc

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class ItemStackDataType(private val plugin: JavaPlugin) : PersistentDataType<ByteArray, ItemStack> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<ItemStack> {
        return ItemStack::class.java
    }

    override fun toPrimitive(
        complex: ItemStack,
        context: PersistentDataAdapterContext,
    ): ByteArray {
        try {
            ByteArrayOutputStream().use { baos ->
                BukkitObjectOutputStream(baos).use { boos ->
                    boos.writeObject(complex)
                    return baos.toByteArray()
                }
            }
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }

    override fun fromPrimitive(
        primitive: ByteArray,
        context: PersistentDataAdapterContext,
    ): ItemStack {
        try {
            ByteArrayInputStream(primitive).use { bais ->
                BukkitObjectInputStream(bais).use { bois ->
                    return bois.readObject() as ItemStack
                }
            }
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        } catch (exception: ClassNotFoundException) {
            throw RuntimeException(exception)
        }
    }
}
