package net.arvandor.numinoustreasury

import com.rpkit.core.service.Services
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.arvandor.numinoustreasury.command.booktitle.BookTitleCommand
import net.arvandor.numinoustreasury.command.node.NodeCommand
import net.arvandor.numinoustreasury.command.numinousitem.NuminousItemCommand
import net.arvandor.numinoustreasury.command.numinouslog.NuminousLogCommand
import net.arvandor.numinoustreasury.command.numinousmodify.NuminousModifyCommand
import net.arvandor.numinoustreasury.command.profession.ProfessionCommand
import net.arvandor.numinoustreasury.command.stamina.StaminaCommand
import net.arvandor.numinoustreasury.droptable.NuminousDropTable
import net.arvandor.numinoustreasury.droptable.NuminousDropTableItem
import net.arvandor.numinoustreasury.droptable.NuminousDropTableService
import net.arvandor.numinoustreasury.interaction.NuminousInteractionService
import net.arvandor.numinoustreasury.item.NuminousItemService
import net.arvandor.numinoustreasury.item.NuminousItemStack
import net.arvandor.numinoustreasury.item.NuminousItemType
import net.arvandor.numinoustreasury.item.action.ApplyPotionEffect
import net.arvandor.numinoustreasury.item.action.Blocked
import net.arvandor.numinoustreasury.item.action.OpenInventory
import net.arvandor.numinoustreasury.item.action.RestoreHunger
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry
import net.arvandor.numinoustreasury.listener.AsyncPlayerPreLoginListener
import net.arvandor.numinoustreasury.listener.BlockBreakListener
import net.arvandor.numinoustreasury.listener.InventoryClickListener
import net.arvandor.numinoustreasury.listener.InventoryCloseListener
import net.arvandor.numinoustreasury.listener.PlayerInteractListener
import net.arvandor.numinoustreasury.listener.PlayerItemConsumeListener
import net.arvandor.numinoustreasury.listener.PlayerMoveListener
import net.arvandor.numinoustreasury.listener.PlayerQuitListener
import net.arvandor.numinoustreasury.listener.RPKCharacterSwitchListener
import net.arvandor.numinoustreasury.listener.VoteListener
import net.arvandor.numinoustreasury.measurement.Weight
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelService
import net.arvandor.numinoustreasury.node.NuminousNodeRepository
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.arvandor.numinoustreasury.pdc.NamespacedKeys
import net.arvandor.numinoustreasury.pdc.PersistentDataTypes
import net.arvandor.numinoustreasury.profession.NuminousCharacterProfessionRepository
import net.arvandor.numinoustreasury.profession.NuminousProfession
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.arvandor.numinoustreasury.recipe.NuminousRecipe
import net.arvandor.numinoustreasury.recipe.NuminousRecipeService
import net.arvandor.numinoustreasury.stamina.NuminousCharacterStaminaRepository
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import net.arvandor.numinoustreasury.stamina.StaminaTier
import net.arvandor.numinoustreasury.web.WebServer
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.flywaydb.core.Flyway
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.time.Duration
import javax.sql.DataSource

class NuminousTreasury : JavaPlugin() {
    lateinit var keys: NamespacedKeys
    lateinit var persistentDataTypes: PersistentDataTypes

    private var dataSource: DataSource? = null

    override fun onEnable() {
        keys = NamespacedKeys(this)
        persistentDataTypes = PersistentDataTypes(this)

        // Actions
        ConfigurationSerialization.registerClass(ApplyPotionEffect::class.java, "ApplyPotionEffect")
        ConfigurationSerialization.registerClass(Blocked::class.java, "Blocked")
        ConfigurationSerialization.registerClass(RestoreHunger::class.java, "RestoreHunger")
        ConfigurationSerialization.registerClass(OpenInventory::class.java, "OpenInventory")

        // Measurements
        ConfigurationSerialization.registerClass(Weight::class.java, "Weight")

        // Items
        ConfigurationSerialization.registerClass(NuminousLogEntry::class.java, "NuminousLogEntry")
        ConfigurationSerialization.registerClass(NuminousItemStack::class.java, "NuminousItemStack")
        ConfigurationSerialization.registerClass(NuminousItemType::class.java, "NuminousItemType")

        // Professions
        ConfigurationSerialization.registerClass(NuminousProfession::class.java, "NuminousProfession")

        // Recipes
        ConfigurationSerialization.registerClass(NuminousRecipe::class.java, "NuminousRecipe")

        // Drop tables
        ConfigurationSerialization.registerClass(NuminousDropTable::class.java, "NuminousDropTable")
        ConfigurationSerialization.registerClass(NuminousDropTableItem::class.java, "NuminousDropTableItem")

        saveDefaultConfig()

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = config.getString("database.url")
        val databaseUsername = config.getString("database.username")
        if (databaseUsername != null) {
            hikariConfig.username = databaseUsername
        }
        val databasePassword = config.getString("database.password")
        if (databasePassword != null) {
            hikariConfig.password = databasePassword
        }
        dataSource = HikariDataSource(hikariConfig)
        val flyway =
            Flyway.configure(classLoader)
                .dataSource(dataSource)
                .locations("classpath:net/arvandor/numinoustreasury/db/migration")
                .table("numinous_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(false)
                .load()
        flyway.migrate()

        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")

        val dialect = SQLDialect.valueOf(config.getString("database.dialect")!!)
        val jooqSettings = Settings().withRenderSchema(false)
        val dsl =
            DSL.using(
                dataSource,
                dialect,
                jooqSettings,
            )

        val characterProfessionRepository = NuminousCharacterProfessionRepository(dsl)
        val characterStaminaRepository = NuminousCharacterStaminaRepository(this, dsl)
        val nodeRepository = NuminousNodeRepository(this, dsl)

        Services.INSTANCE.set(NuminousItemService::class.java, NuminousItemService(this))
        Services.INSTANCE.set(
            NuminousProfessionService::class.java,
            NuminousProfessionService(
                this,
                characterProfessionRepository,
            ),
        )
        Services.INSTANCE.set(NuminousRecipeService::class.java, NuminousRecipeService(this))
        Services.INSTANCE.set(
            NuminousStaminaService::class.java,
            NuminousStaminaService(this, characterStaminaRepository),
        )
        Services.INSTANCE.set(NuminousDropTableService::class.java, NuminousDropTableService(this))
        Services.INSTANCE.set(NuminousNodeService::class.java, NuminousNodeService(this, nodeRepository))
        Services.INSTANCE.set(
            NuminousInteractionService::class.java,
            NuminousInteractionService(
                this,
            ),
        )
        Services.INSTANCE.set(NuminousMixpanelService::class.java, NuminousMixpanelService(this))

        registerListeners(
            AsyncPlayerPreLoginListener(this),
            BlockBreakListener(this),
            InventoryClickListener(),
            InventoryCloseListener(),
            PlayerInteractListener(this),
            PlayerItemConsumeListener(),
            PlayerMoveListener(),
            PlayerQuitListener(this),
            RPKCharacterSwitchListener(),
        )

        if (server.pluginManager.getPlugin("Votifier") != null) {
            logger.info("Detected Votifier, listening for votes.")
            server.pluginManager.registerEvents(VoteListener(this), this)
        } else {
            logger.info("Did not detect Votifier, vote support disabled.")
        }

        getCommand("profession")?.setExecutor(ProfessionCommand(this))
        getCommand("numinousitem")?.setExecutor(NuminousItemCommand(this))
        getCommand("numinousmodify")?.setExecutor(NuminousModifyCommand(this))
        getCommand("stamina")?.setExecutor(StaminaCommand(this))
        getCommand("node")?.setExecutor(NodeCommand(this))
        getCommand("numinouslog")?.setExecutor(NuminousLogCommand(this))
        getCommand("booktitle")?.setExecutor(BookTitleCommand())

        val staminaRestorationInterval = Duration.parse(config.getString("stamina.restoration-interval"))
        server.scheduler.scheduleSyncRepeatingTask(this, {
            val staminaService =
                Services.INSTANCE.get(
                    NuminousStaminaService::class.java,
                )
            val previousStamina: MutableMap<String, Int> = HashMap()
            server.onlinePlayers.forEach { player: Player ->
                previousStamina[player.uniqueId.toString()] = staminaService.getStamina(player)
            }
            staminaService.restoreStamina {
                server.onlinePlayers.forEach { player: Player ->
                    val oldStamina = previousStamina[player.uniqueId.toString()]!!
                    val newStamina = staminaService.getStamina(player)
                    val transitionMessage: String? =
                        StaminaTier.messageForStaminaTransition(
                            oldStamina,
                            newStamina,
                            staminaService.maxStamina,
                        )
                    if (transitionMessage != null) {
                        player.sendMessage(transitionMessage)
                    }
                }
            }
        }, (staminaRestorationInterval.toSeconds() * 20L) / 2, staminaRestorationInterval.toSeconds() * 20L)

        server.scheduler.runTaskAsynchronously(
            this,
            Runnable {
                WebServer(this).start()
            },
        )
    }

    private fun registerListeners(vararg listeners: Listener) {
        for (listener in listeners) {
            server.pluginManager.registerEvents(listener, this)
        }
    }
}
