package me.epicgodmc.blockstackerx;

import lombok.Getter;
import me.epicgodmc.blockstackerx.commands.CommandRoot;
import me.epicgodmc.blockstackerx.config.GuiSettings;
import me.epicgodmc.blockstackerx.config.LangSettings;
import me.epicgodmc.blockstackerx.config.StackerSettings;
import me.epicgodmc.blockstackerx.config.WorthSettings;
import me.epicgodmc.blockstackerx.inventory.GuiManager;
import me.epicgodmc.blockstackerx.listeners.StackerInteractEvent;
import me.epicgodmc.blockstackerx.listeners.StackerPlaceEvent;
import me.epicgodmc.blockstackerx.listeners.StackerRemoveEvent;
import me.epicgodmc.epicapi.command.CommandManager;
import me.epicgodmc.epicapi.inventory.MenuRegistrar;
import me.epicgodmc.epicapi.storage.shaded.jetbrains.annotations.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class BlockStackerX extends JavaPlugin {

    public static Logger logger = Logger.getLogger("Minecraft");

    private static BlockStackerX instance;

    public static BlockStackerX inst() {
        return instance;
    }

    public boolean forceShutdown = false;

    @Getter
    private StackerSettings stackerSettings;
    @Getter
    private WorthSettings worthSettings;
    @Getter
    private GuiSettings guiSettings;
    @Getter
    private LangSettings langSettings;
    @Getter
    private MenuRegistrar menuRegistrar;
    @Getter
    private StackerStore stackerStore;
    @Getter
    private DependencyManager dependencyManager;
    @Getter
    private GuiManager guiManager;
    @Getter
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        logger.info("==================================================================");
        if (preEnable()) {
            logLogo();
            instance = this;
            dependencyManager.logHooks();
            try {
                instantiateClasses();
            } catch (InstantiationException e) {
                logger.log(Level.SEVERE, "Could not instantiate menuRegistrar", e);
                shutDown();
            }
            registerCommands();
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, task ->
            {
                getStackerStore().getStackerStorage().loadStackers();
            }, 40L);

        } else {
            shutDown();
        }
        logger.info("==================================================================");
    }

    private void shutDown() {
        this.forceShutdown = true;
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        if (forceShutdown) {
            logger.info("==================================================================");
            logger.info("Shutting down blockStackerX");
            logger.info("==================================================================");
            return;
        }
        logger.info("==================================================================");
        logger.info("Saving data");
        if (getStackerStore().getStackerStorage() != null) {
            getStackerStore().getStackerStorage().saveStackers(getStackerStore().getStackers(), true);
        }
        logger.info("Finalizing");
        instance = null;
        logger.info("==================================================================");
    }

    private void instantiateClasses() throws InstantiationException {
        this.worthSettings = new WorthSettings(this);
        this.stackerSettings = new StackerSettings(this);
        this.guiSettings = new GuiSettings(this);
        this.langSettings = new LangSettings(this, this.stackerSettings.getLang());
        logger.info("Loaded configurations");
        this.commandManager = new CommandManager(this);
        this.stackerStore = new StackerStore(this);
        this.guiManager = new GuiManager(this);
        this.menuRegistrar = new MenuRegistrar(this);
        new StackerPlaceEvent(this);
        new StackerRemoveEvent(this);
        new StackerInteractEvent(this);
    }

    private boolean preEnable() {
        dependencyManager = new DependencyManager(this);
        return dependencyManager.isSuccess();
    }

    private void registerCommands() {
        commandManager.registerCommand(new CommandRoot(this));
        logger.info("Started command manager");
    }

    public void registerListener(Listener listener, @Nullable String job) {
        if (job != null) logger.info("Registering new listener, Job=(" + job + ")");
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    private void logLogo() {
        logger.info("\n" +
                "  ____    _                  _      ____    _                    _                   __  __\n" +
                " | __ )  | |   ___     ___  | | __ / ___|  | |_    __ _    ___  | | __   ___   _ __  \\ \\/ /\n" +
                " |  _ \\  | |  / _ \\   / __| | |/ / \\___ \\  | __|  / _` |  / __| | |/ /  / _ \\ | '__|  \\  / \n" +
                " | |_) | | | | (_) | | (__  |   <   ___) | | |_  | (_| | | (__  |   <  |  __/ | |     /  \\ \n" +
                " |____/  |_|  \\___/   \\___| |_|\\_\\ |____/   \\__|  \\__,_|  \\___| |_|\\_\\  \\___| |_|    /_/\\_\\\n" +
                "                                                                                           "
                + "\n");
    }
}

