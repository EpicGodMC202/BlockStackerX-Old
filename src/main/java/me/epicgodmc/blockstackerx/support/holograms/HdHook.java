package me.epicgodmc.blockstackerx.support.holograms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.epicapi.util.ColorAPI;
import org.bukkit.Location;

public class HdHook implements StackerHologram {

    private final BlockStackerX plugin;

    private Object hologramObj;

    public HdHook(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void create(String type, int value, Location location) {
        String name = ColorAPI.color(plugin.getStackerSettings().getValueFormat(type, value));
        Hologram hologram = HologramsAPI.createHologram(plugin, location);
        hologram.appendTextLine(name);
        this.hologramObj = hologram;
    }


    @Override
    public void update(String type, int value) {
        Hologram hologram = (Hologram) hologramObj;
        String name = plugin.getStackerSettings().getValueFormat(type, value);
        hologram.insertTextLine(0, name);
        hologram.getLine(1).removeLine();
    }

    @Override
    public void delete() {
        Hologram hologram = (Hologram) hologramObj;
        hologram.delete();
    }
}
