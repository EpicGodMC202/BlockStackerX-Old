package me.epicgodmc.blockstackerx.support.holograms;

import org.bukkit.Location;

public interface StackerHologram
{

    void create(String type, int value, Location location);
    void update(String type, int value);
    void delete();

}
