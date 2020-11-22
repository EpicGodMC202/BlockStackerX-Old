package me.epicgodmc.blockstackerx;

import jdk.internal.jline.internal.Nullable;
import me.epicgodmc.blockstackerx.database.MySqlStorage;
import me.epicgodmc.blockstackerx.database.StackerStorage;
import me.epicgodmc.blockstackerx.database.sqlite.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;

public class StackerStore
{

    private StackerStorage stackerStorage;
    private final BlockStackerX plugin;
    public StackerStore(BlockStackerX plugin)
    {
        this.plugin = plugin;
        String storageMethod = plugin.getConfig().getString("storage.method");
        if (storageMethod != null) {
            switch (storageMethod) {
                case "sqlite":
                    SQLite sqLite = new SQLite(plugin);
                    if (sqLite.load())
                    {
                        this.stackerStorage = sqLite;
                        BlockStackerX.logger.info("Storage Method was set to sqlite successfully");
                    }
                    break;
                case "mysql":
                    try {
                        MySqlStorage sql = new MySqlStorage(plugin);
                        boolean success = sql.connect();
                        if (success) {
                            this.stackerStorage = sql;
                            BlockStackerX.logger.info("Connected to mysql database successfully");
                        }
                        else storageSelectErrorCallback("Failed to connect to database");
                    }catch (Exception e)
                    {
                        BlockStackerX.logger.info("Failed to connect to mysql database, using default json storage method");
                    }
                    break;
            }
        }
        if (plugin.getConfig().getBoolean("storage.autosave.enabled"))
        {
            startAutoSave();
            BlockStackerX.logger.info("Initiated autosave clock");
        }

    }


   private void storageSelectErrorCallback(String error)
   {
       BlockStackerX.logger.info(error+", Using SQLite Storage");
       SQLite sqLite = new SQLite(plugin);
       if (sqLite.load())
       {
           this.stackerStorage = sqLite;
           BlockStackerX.logger.info("Storage Method was set to sqlite successfully");
       }else{
           BlockStackerX.logger.info("Failed to select a storage method, disabling plugin");
           plugin.forceShutdown = true;
           plugin.getPluginLoader().disablePlugin(plugin);
       }
   }

    private final HashMap<Location, StackerBlock> stackers = new HashMap<>();

    public void setStack(Location l, StackerBlock b)
    {
        stackers.putIfAbsent(l, b);
    }

    public void removeStack(Location l)
    {
        getStackerStorage().removeStacker(stackers.get(l));
        stackers.remove(l);
    }

    public boolean contains(Location l)
    {
        return stackers.containsKey(l);
    }

    public StackerBlock getStacker(Location l)
    {
        return stackers.get(l);
    }

    public HashMap<Location, StackerBlock> getStackers() {
        return stackers;
    }

    public List<StackerBlock> getStackersOf(UUID uuid) {
        return stackers.values().stream().filter(e -> e.getOwner().equals(uuid)).collect(Collectors.toList());
    }

    public StackerStorage getStackerStorage() {
        return stackerStorage;
    }

    public HashMap<Location, StackerBlock> collectStackers(@Nullable UUID requester, Collection<UUID> team)
    {
        HashMap<Location, StackerBlock> stackerBlocks = new HashMap<>();

        if (requester != null) {
            for (StackerBlock ownerStacks : getStackersOf(requester)) {
                stackerBlocks.putIfAbsent(ownerStacks.getLocation().toBukkitLoc(), ownerStacks);
            }
        }
        for (UUID uuid : team)
        {
            for (StackerBlock stacker : getStackersOf(uuid))
            {
                stackerBlocks.putIfAbsent(stacker.getLocation().toBukkitLoc(), stacker);
            }
        }
        return stackerBlocks;
    }

    public ArrayList<StackerBlock> sort()
    {
        ArrayList<StackerBlock> array = new ArrayList<>(stackers.values());
        Collections.sort(array);
        return array;
    }

    private void startAutoSave()
    {
        long delay = (plugin.getConfig().getLong("storage.autosave.interval") * 20) * 60;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task -> {
            getStackerStorage().saveStackers(this.stackers, false);
            BlockStackerX.logger.info("Saved stackers!");
        }, delay, delay);
    }
}
