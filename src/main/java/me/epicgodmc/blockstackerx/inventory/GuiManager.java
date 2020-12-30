package me.epicgodmc.blockstackerx.inventory;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.config.GuiSettings;
import me.epicgodmc.blockstackerx.config.StackerSettings;
import org.bukkit.entity.Player;

public class GuiManager
{

    private final BlockStackerX plugin;
    private final StackerSettings settings;
    private final GuiSettings guiSettings;

    public GuiManager(BlockStackerX plugin) {
        this.plugin = plugin;
        this.settings = plugin.getStackerSettings();
        this.guiSettings = plugin.getGuiSettings();
    }


    public void openStacker(Player requester, StackerBlock stackerBlock)
    {
        String gui = settings.getLinkedGUI(stackerBlock.getType());
        if (guiSettings.guiExists(gui))
        {
            StackerGUI inv = new StackerGUI(plugin, requester, stackerBlock, plugin.getStackerSettings().getLinkedGUI(stackerBlock.getType()));
            inv.open();
        }
    }

}
