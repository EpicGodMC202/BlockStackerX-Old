package me.epicgodmc.blockstackerx;

import me.epicgodmc.blockstackerx.support.holograms.StackerHologram;
import me.epicgodmc.epicframework.util.SimpleLocation;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class StackerBlock {

    private final BlockStackerX plugin;

    private UUID owner;
    private String type;
    private SimpleLocation location;
    private Material stackMaterial;
    private StackerHologram hologram;
    private boolean hologramVisibility;
    private int value;

    public StackerBlock(BlockStackerX plugin, UUID owner, String type, StackerHologram hologram, SimpleLocation location) {
        this.plugin = plugin;
        this.owner = owner;
        this.type = type;
        this.hologram = hologram;
        this.location = location;
        this.hologramVisibility = true;
        this.value = 0;
    }

    public StackerBlock(BlockStackerX plugin, UUID owner, String type, StackerHologram hologram, SimpleLocation location, Material material, int value)
    {
        this.plugin = plugin;
        this.owner = owner;
        this.type = type;
        this.hologram = hologram;
        this.location = location;
        setStackMaterial(material);
        this.value = value;
        this.hologramVisibility = true;
    }

    public String getType() {
        return type;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setLocation(SimpleLocation location) {
        this.location = location;
    }

    public void setStackMaterial(Material stackMaterial) {
        if (stackMaterial == null) {
            location.toBukkitLoc().getBlock().setType(plugin.getSettings().getDefaultMaterial(type));
            return;
        }
        this.stackMaterial = stackMaterial;
        location.toBukkitLoc().getBlock().setType(stackMaterial);
    }

    public void setValue(int value) {
        this.value = value;
        checkBlockState();
        if (this.hologramVisibility){
            getHologram().update(type, value);
        }

    }

    public UUID getOwner() {
        return owner;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public Material getStackMaterial() {
        if (stackMaterial == null) return location.getBlock().getType();
        return stackMaterial;
    }

    public void delete() {
        Location loc = this.location.toBukkitLoc();
        loc.getBlock().setType(Material.AIR);
        if (this.hologramVisibility) this.getHologram().delete();
        plugin.getStackerStore().removeStack(loc);
    }

    public StackerHologram getHologram() {
        return hologram;
    }

    public void setHologram(StackerHologram hologram) {
        this.hologram = hologram;
    }

    public int getValue() {
        return value;
    }

    public double calculateLevels()
    {
        if (this.stackMaterial != null) {
            double matValue = plugin.getSettings().getWorth(this.stackMaterial);
            return (matValue * this.value);
        }
        return 0.0;
    }

    public double getMaterialValue()
    {
        if (this.stackMaterial != null)
        {
            return plugin.getSettings().getWorth(this.stackMaterial);
        }
        return 0.0;
    }


    public void toggleHologramVisibility() {
        if (hologramVisibility) {
            this.hologram.delete();
            this.hologram = null;
            this.hologramVisibility = false;
        } else {
            this.hologramVisibility = true;
            float[] offset = plugin.getSettings().getDisplayOffset(this.type);
            Location l = this.location.toBukkitLoc().add(offset[0], offset[1], offset[2]);
            StackerHologram hologram = plugin.getDependencyManager().getNewHologram(plugin);
            hologram.create(this.type, this.value, l);
            this.hologram = hologram;
        }
    }

    public boolean isHologramVisible()
    {
        return hologramVisibility;
    }

    public int getStorageLeft() {
        int max = plugin.getSettings().getMaxStorage(type);
        return max - this.value;
    }

    public boolean hasStackMaterial() {
        return stackMaterial != null;
    }

    public boolean canAddValue(int a) {
        int max = plugin.getSettings().getMaxStorage(type);
        return this.value + a <= max;
    }

    public boolean canSubtractValue(int a) {
        return this.value - a >= 0;
    }

    public void subtractValue(int a) {
        this.value -= a;
        checkBlockState();
        if (this.hologramVisibility) this.hologram.update(type, this.value);
    }

    public void incrementValue(int a) {
        this.value += a;
        if (this.hologramVisibility) this.hologram.update(type, this.value);
    }

    private void checkBlockState() {
        if (value <= 0) {
            this.value = 0;
            this.stackMaterial = null;
            location.toBukkitLoc().getBlock().setType(plugin.getSettings().getDefaultMaterial(type));
        }
    }
}
