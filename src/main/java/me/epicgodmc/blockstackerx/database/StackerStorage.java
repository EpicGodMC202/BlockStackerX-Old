package me.epicgodmc.blockstackerx.database;

import me.epicgodmc.blockstackerx.StackerBlock;
import org.bukkit.Location;

import java.util.Map;


public interface StackerStorage
{

    void setStacker(StackerBlock stacker);

    void removeStacker(StackerBlock stackerBlock);

    boolean contains(StackerBlock stacker);

    void loadStackers();

    void saveStackers(Map<Location, StackerBlock> stackers, boolean onDisable);


}
