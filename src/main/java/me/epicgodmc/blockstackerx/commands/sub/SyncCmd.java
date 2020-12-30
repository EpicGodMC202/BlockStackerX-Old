package me.epicgodmc.blockstackerx.commands.sub;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.database.StackerStorage;
import me.epicgodmc.blockstackerx.database.sqlite.SQLite;
import me.epicgodmc.epicapi.command.SubCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Map;

public class SyncCmd extends SubCommand
{

    private final BlockStackerX plugin;

    public SyncCmd(BlockStackerX plugin) {
        this.plugin = plugin;
    }


    // bs syncdata - confirm

    @Override
    public void onCommand(CommandSender commandSender, String[] args) {
        if (commandSender instanceof ConsoleCommandSender)
        {
            if (args.length == 0)
            {
                BlockStackerX.logger.info("WARNING! - this command will synchronize the database you are currently connected to with the other database type, this means that any data that is in the other database will be lost!, type '/bs SyncData -confirm' to confirm");
                return;
            }
            StackerStorage currentStorage = plugin.getStackerStore().getStackerStorage();
            String type = currentStorage.getStorageType();
            if (type.equals("mysql"))
            {
                // create sqlite connection
                BlockStackerX.logger.info("Attempting to create sqlite connection");
                SQLite sqLite = new SQLite(plugin);
                if (sqLite.load())
                {
                    BlockStackerX.logger.info("Connected to sqlite successfully");
                    BlockStackerX.logger.info("Attempting to synchronize data");
                    //Map<Location, StackerBlock> data = currentStorage.getStackers();
                    BlockStackerX.logger.info("Attempting to push data to sqlite database");
                    sqLite.clearData();
                    //sqLite.saveStackers(data, false);
                    BlockStackerX.logger.info("Finished synchronizing");
                }


            }else{
                // create mysql connection
            }

        }else{
            plugin.getLangSettings().sendText(commandSender, "consoleNotFound", true);
        }
    }

    @Override
    public String name() {
        return "syncdata";
    }

    @Override
    public String requiredPermission() {
        return "console";
    }

    @Override
    public boolean isPlayerCmd() {
        return false;
    }

    @Override
    public String info() {
        return "&f&l* &d/BlockStackerX syncdata";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
