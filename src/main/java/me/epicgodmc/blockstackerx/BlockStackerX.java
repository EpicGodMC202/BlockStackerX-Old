package me.epicgodmc.blockstackerx;

import jdk.internal.jline.internal.Nullable;
import me.epicgodmc.blockstackerx.commands.BaseCommand;
import me.epicgodmc.blockstackerx.inventory.GuiManager;
import me.epicgodmc.blockstackerx.listeners.StackerInteractEvent;
import me.epicgodmc.blockstackerx.listeners.StackerPlaceEvent;
import me.epicgodmc.blockstackerx.listeners.StackerRemoveEvent;
import me.epicgodmc.blockstackerx.utils.Settings;
import me.epicgodmc.epicframework.EpicFramework;
import me.epicgodmc.epicframework.command.CommandManager;
import me.epicgodmc.epicframework.file.FileManager;
import me.epicgodmc.epicframework.file.LangManager;
import me.epicgodmc.epicframework.inventory.GuiPage;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class BlockStackerX extends JavaPlugin {

    public static Logger logger = Logger.getLogger("Minecraft");

    private static BlockStackerX instance;

    public static BlockStackerX inst() {
        return instance;
    }

    private boolean forceShutdown = false;

    private FileManager fileManager;
    private Settings settings;
    private LangManager langManager;
    private StackerStore stackerStore;
    private DependencyManager dependencyManager;
    private EpicFramework framework;
    private CommandManager commandManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        logger.info("==================================================================");
        if (preEnable()) {
            logLogo();
            instance = this;
            dependencyManager.logHooks();
            saveDefaultConfig();
            instantiateClasses();
            saveConfigDefaults();
            registerCommands();
            getStackerStore().getStackerStorage().loadStackers();
            framework.setPluginPrefix(fileManager.getLangFile().get().getString("prefix"));
        } else {
            Bukkit.getScheduler().cancelTasks(this);
            Bukkit.getPluginManager().disablePlugin(this);
        }
        logger.info("==================================================================");
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
        getStackerStore().getStackerStorage().saveStackers(getStackerStore().getStackers());
        logger.info("Finalizing");
        GuiPage.onDisable();
        instance = null;
        logger.info("==================================================================");
    }

    private void saveConfigDefaults() {
        getFileManager().getConfig("gui.yml").saveDefaultConfig();
        getFileManager().getConfig("worth.yml").saveDefaultConfig();
    }

    private void instantiateClasses() {
        framework = new EpicFramework(this);
        this.fileManager = framework.getFileManager();
        this.commandManager = framework.getCommandManager();
        this.settings = new Settings(this, this.getConfig());
        this.langManager = new LangManager(this);
        this.stackerStore = new StackerStore(this);
        this.guiManager = new GuiManager(this);
        new StackerPlaceEvent(this);
        new StackerRemoveEvent(this);
        new StackerInteractEvent(this);
    }

    private boolean preEnable() {
        dependencyManager = new DependencyManager(this);
        return dependencyManager.isSuccess();
    }

    private void registerCommands() {
        commandManager.registerCommand(new BaseCommand(this));
        logger.info("Started command manager");
    }

    public void registerListener(Listener listener, @Nullable String job) {
        if (job != null) logger.info("Registering new listener, Job=(" + job + ")");
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    public EpicFramework getFramework() {
        return framework;
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public StackerStore getStackerStore() {
        return stackerStore;
    }

    public Settings getSettings() {
        return settings;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
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

