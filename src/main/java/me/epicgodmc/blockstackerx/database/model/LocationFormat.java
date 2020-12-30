package me.epicgodmc.blockstackerx.database.model;

import me.epicgodmc.epicapi.sqlibrary.model.utils.FieldFormat;
import me.epicgodmc.epicapi.util.SimpleLocation;
import org.bukkit.Location;

public class LocationFormat extends FieldFormat<String, Location>
{

    public LocationFormat()
    {
        super(String.class, Location.class);
    }

    @Override
    public Location toModel(String s) {
        return s == null ? null : SimpleLocation.fromRoundDataString(s).toBukkitLoc();
    }

    @Override
    public String fromModel(Location location) {
        return location == null ? null : SimpleLocation.getRoundDataString(location);
    }
}
