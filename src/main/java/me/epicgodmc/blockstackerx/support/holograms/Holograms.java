package me.epicgodmc.blockstackerx.support.holograms;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.HologramPlugin;
import com.sainttx.holograms.api.line.HologramLine;
import com.sainttx.holograms.api.line.TextLine;
import me.epicgodmc.blockstackerx.BlockStackerX;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class Holograms implements StackerHologram {

    private final BlockStackerX plugin;
    private final HologramManager hologramManager;
    private Object hologramObject;

    public Holograms(BlockStackerX plugin) {
        this.plugin = plugin;
        this.hologramManager = JavaPlugin.getPlugin(HologramPlugin.class).getHologramManager();
    }

    @Override
    public void create(String type, int value, Location location) {
        String name = plugin.getSettings().getValueFormat(type, value);
        Hologram hologram = new Hologram("BlockStackerX", location);
        hologram.setPersistent(false);
        HologramLine line = new TextLine(hologram, name);
        hologram.addLine(line);
        this.hologramObject = hologram;
        hologramManager.addActiveHologram(hologram);
        hologram.spawn();
    }

    @Override
    public void update(String type, int value) {
        Hologram hologram = (Hologram) hologramObject;
        String name = plugin.getSettings().getValueFormat(type, value);
        HologramLine line = new TextLine(hologram, name);
        hologram.removeLine(hologram.getLine(0));
        hologram.addLine(line, 0);
    }

    @Override
    public void delete() {
        Hologram hologram = (Hologram) hologramObject;
        hologramManager.deleteHologram(hologram);
    }
}
