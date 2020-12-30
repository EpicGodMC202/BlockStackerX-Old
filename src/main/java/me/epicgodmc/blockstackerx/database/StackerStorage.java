package me.epicgodmc.blockstackerx.database;

import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.database.model.StackerModel;
import me.epicgodmc.epicapi.sqlibrary.database.DatabaseConnection;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;


public interface StackerStorage
{

    DatabaseConnection getSqlConnection();

    void setStacker(StackerBlock stacker);

    void removeStacker(StackerBlock stackerBlock);

    boolean contains(StackerBlock stacker);

    List<StackerModel> getStackers();

    void disconnect();

    void clearData();

    void loadStackers();

    void saveStackers(Map<Location, StackerBlock> stackers, boolean onDisable);

    String getStorageType();
}
