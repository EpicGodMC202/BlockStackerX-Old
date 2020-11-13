package me.epicgodmc.blockstackerx;

import me.epicgodmc.blockstackerx.support.skyblock.ASkyblockHook;
import me.epicgodmc.blockstackerx.support.WorldGuardHook;
import me.epicgodmc.blockstackerx.support.holograms.HdHook;
import me.epicgodmc.blockstackerx.support.holograms.Holograms;
import me.epicgodmc.blockstackerx.support.holograms.StackerHologram;
import me.epicgodmc.blockstackerx.support.skyblock.BSkyBlockHook;
import me.epicgodmc.blockstackerx.support.skyblock.SkyblockHook;
import world.bentobox.bentobox.BentoBox;

public class DependencyManager {

    private final BlockStackerX plugin;

    private WorldGuardHook WorldGuardHook;
    private HdHook HdHook;
    private Holograms HologramsHook;
    private ASkyblockHook ASkyblockHook;
    private BSkyBlockHook BSkyblockHook;

    private boolean success = true;

    public DependencyManager(BlockStackerX pl) {
        this.plugin = pl;

        if (isPluginPresent("WorldGuard"))
        {
            this.WorldGuardHook = new WorldGuardHook(pl);
        }
        if (isPluginPresent("HolographicDisplays"))
        {
            this.HdHook = new HdHook(pl);
        }
        if (isPluginPresent("Holograms"))
        {
            this.HologramsHook = new Holograms(pl);
        }

        if (isPluginPresent("BentoBox"))
        {
            if (BentoBox.getInstance().getAddonsManager().getAddonByName("BSkyBlock").isPresent())
            {
                if (BentoBox.getInstance().getAddonsManager().getAddonByName("Level").isPresent())
                {
                    this.BSkyblockHook = new BSkyBlockHook(pl);
                    BlockStackerX.logger.info("Found BSkyBlock addon & Level addon for bentobox");
                }else{
                    BlockStackerX.logger.info("Found Bentobox and BSkyblock, but no Level addons was found");
                }
            }else{
                BlockStackerX.logger.info("Found Bentobox, but no BSkyBlock addon was found");
            }

        }

        if (isPluginPresent("ASkyBlock"))
        {
            this.ASkyblockHook = new ASkyblockHook(pl);
        }

        if (!isASkyBlock() && !isBSkyBlock())
        {
            BlockStackerX.logger.severe("Could not find a Skyblock dependency, disabling plugin");
            success = false;
        }
        if (!isHD() && !isHolograms())
        {
            BlockStackerX.logger.severe("Could not find a Hologram dependency, disabling plugin");
            success = false;
        }
    }

    private boolean isPluginPresent(String pluginId)
    {
        boolean check1 = plugin.getServer().getPluginManager().isPluginEnabled(pluginId);
        boolean check2 = plugin.getServer().getPluginManager().getPlugin(pluginId)!=null;

        if (check1 || check2) return true;
        return false;
    }


    public void logHooks() {

        if (isWorldguard()) {
            BlockStackerX.logger.info("Hook registered: WorldGuard");
        }
        if (isHD()) {
            BlockStackerX.logger.info("Hook registered: HolographicDisplays");

        }
        if (isHolograms()) {
            BlockStackerX.logger.info("Hook registered: Holograms");
        }

        if (isASkyBlock())
        {
            BlockStackerX.logger.info("Hook registered: ASkyBlock");
        }

        if (isBSkyBlock())
        {
            BlockStackerX.logger.info("Hook registered: BSkyBlock");
        }

        if (isASkyBlock() && isBSkyBlock())
        {
            BlockStackerX.logger.info("Since multiple Skyblock dependencies were found, BSkyBlock will have priority");
        }

        if (isHD() && isHolograms())
        {
            BlockStackerX.logger.info("Since multiple hologram dependencies were found, Holographic Displays will have priority");
        }
    }

    public SkyblockHook getSkyblockHook()
    {
        if (isASkyBlock())
        {
            return ASkyblockHook;
        }else if (isBSkyBlock())
        {
            return BSkyblockHook;
        }
        else{
            BlockStackerX.logger.severe("Failed to instantiate skyblock hook!");
            return null;
        }
    }

    public StackerHologram getNewHologram(BlockStackerX pl) {
        if (HdHook != null) {
            return new HdHook(pl);
        } else if (HologramsHook != null) {
            return new Holograms(pl);
        } else {
            BlockStackerX.logger.info("Invalid holograms dependency!!!");
            return null;
        }
    }

    public boolean isASkyBlock()
    {
        return ASkyblockHook != null;
    }

    public boolean isBSkyBlock()
    {
        return BSkyblockHook != null;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isWorldguard() {
        return WorldGuardHook != null;
    }

    public boolean isHolograms() {
        return HologramsHook != null;
    }

    public boolean isHD() {
        return HdHook != null;
    }

    public WorldGuardHook getWorldGuardHook() {
        return WorldGuardHook;
    }

    public HdHook getHD() {
        return HdHook;
    }

    public Holograms getHOLOGRAMS() {
        return HologramsHook;
    }
}
