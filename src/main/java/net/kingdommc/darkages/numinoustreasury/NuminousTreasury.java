package net.kingdommc.darkages.numinoustreasury;

import com.rpkit.core.service.Services;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kingdommc.darkages.numinoustreasury.command.numinousitem.NuminousItemCommand;
import net.kingdommc.darkages.numinoustreasury.command.profession.ProfessionCommand;
import net.kingdommc.darkages.numinoustreasury.command.stamina.StaminaCommand;
import net.kingdommc.darkages.numinoustreasury.item.NuminousItemService;
import net.kingdommc.darkages.numinoustreasury.item.NuminousItemStack;
import net.kingdommc.darkages.numinoustreasury.item.NuminousItemType;
import net.kingdommc.darkages.numinoustreasury.item.action.ApplyPotionEffect;
import net.kingdommc.darkages.numinoustreasury.item.action.Blocked;
import net.kingdommc.darkages.numinoustreasury.item.action.RestoreHunger;
import net.kingdommc.darkages.numinoustreasury.listener.*;
import net.kingdommc.darkages.numinoustreasury.measurement.Weight;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousCharacterProfessionRepository;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfession;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfessionService;
import net.kingdommc.darkages.numinoustreasury.recipe.NuminousRecipe;
import net.kingdommc.darkages.numinoustreasury.recipe.NuminousRecipeService;
import net.kingdommc.darkages.numinoustreasury.stamina.NuminousCharacterStaminaRepository;
import net.kingdommc.darkages.numinoustreasury.stamina.NuminousStaminaService;
import net.kingdommc.darkages.numinoustreasury.stamina.StaminaTier;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class NuminousTreasury extends JavaPlugin {

    private NamespacedKeys keys;

    private DataSource dataSource;

    @Override
    public void onEnable() {
        keys = new NamespacedKeys(this);

        // Actions
        ConfigurationSerialization.registerClass(ApplyPotionEffect.class, "ApplyPotionEffect");
        ConfigurationSerialization.registerClass(Blocked.class, "Blocked");
        ConfigurationSerialization.registerClass(RestoreHunger.class, "RestoreHunger");

        // Measurements
        ConfigurationSerialization.registerClass(Weight.class, "Weight");

        // Items
        ConfigurationSerialization.registerClass(NuminousItemStack.class, "NuminousItemStack");
        ConfigurationSerialization.registerClass(NuminousItemType.class, "NuminousItemType");

        // Professions
        ConfigurationSerialization.registerClass(NuminousProfession.class, "NuminousProfession");

        // Recipes
        ConfigurationSerialization.registerClass(NuminousRecipe.class, "NuminousRecipe");

        saveDefaultConfig();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getConfig().getString("database.url"));
        String databaseUsername = getConfig().getString("database.username");
        if (databaseUsername != null) {
            hikariConfig.setUsername(databaseUsername);
        }
        String databasePassword = getConfig().getString("database.password");
        if (databasePassword != null) {
            hikariConfig.setPassword(databasePassword);
        }
        dataSource = new HikariDataSource(hikariConfig);
        Flyway flyway = Flyway.configure(getClassLoader())
                .dataSource(dataSource)
                .locations("classpath:net/kingdommc/darkages/numinoustreasury/db/migration")
                .table("numinous_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(false)
                .load();
        flyway.migrate();

        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        SQLDialect dialect = SQLDialect.valueOf(getConfig().getString("database.dialect"));
        Settings jooqSettings = new Settings().withRenderSchema(false);
        DSLContext dsl = DSL.using(
                dataSource,
                dialect,
                jooqSettings
        );

        NuminousCharacterProfessionRepository characterProfessionRepository = new NuminousCharacterProfessionRepository(dsl);
        NuminousCharacterStaminaRepository characterStaminaRepository = new NuminousCharacterStaminaRepository(this, dsl);

        Services.INSTANCE.set(NuminousItemService.class, new NuminousItemService(this));
        Services.INSTANCE.set(NuminousProfessionService.class, new NuminousProfessionService(this, characterProfessionRepository));
        Services.INSTANCE.set(NuminousRecipeService.class, new NuminousRecipeService(this));
        Services.INSTANCE.set(NuminousStaminaService.class, new NuminousStaminaService(this, characterStaminaRepository));

        registerListeners(
                new AsyncPlayerPreLoginListener(this),
                new InventoryClickListener(),
                new PlayerInteractListener(this),
                new PlayerItemConsumeListener(),
                new PlayerQuitListener(this),
                new RPKCharacterSwitchListener()
        );

        getCommand("profession").setExecutor(new ProfessionCommand());
        getCommand("numinousitem").setExecutor(new NuminousItemCommand());
        getCommand("stamina").setExecutor(new StaminaCommand(this));

        Duration staminaRestorationInterval = Duration.parse(getConfig().getString("stamina.restoration-interval"));
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
            Map<String, Integer> previousStamina = new HashMap<>();
            getServer().getOnlinePlayers().forEach(player -> previousStamina.put(player.getUniqueId().toString(), staminaService.getStamina(player)));
            staminaService.restoreStamina(() -> getServer().getOnlinePlayers().forEach(player -> {
                int oldStamina = previousStamina.get(player.getUniqueId().toString());
                int newStamina = staminaService.getStamina(player);
                String transitionMessage = StaminaTier.messageForStaminaTransition(oldStamina, newStamina, getConfig().getInt("stamina.max"));
                if (transitionMessage != null) {
                    player.sendMessage(transitionMessage);
                }
            }));
        }, (staminaRestorationInterval.toSeconds() * 20L) / 2, staminaRestorationInterval.toSeconds() * 20L);
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public NamespacedKeys keys() {
        return keys;
    }

}
