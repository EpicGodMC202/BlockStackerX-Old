package me.epicgodmc.blockstackerx.database;

import me.epicgodmc.blockstackerx.StackerBlock;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;

public interface StackerStorage
{

    StackerBlock getStacker(UUID owner);

    void setStacker(StackerBlock stacker);

    void removeStacker(StackerBlock stackerBlock);

    boolean contains(StackerBlock stacker);

    void loadStackers();

    void saveStackers(Map<Location, StackerBlock> stackers);


}
