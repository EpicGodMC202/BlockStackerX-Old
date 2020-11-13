package me.epicgodmc.blockstackerx.inventory;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.utils.Settings;
import me.epicgodmc.epicframework.file.FileManager;
import org.bukkit.entity.Player;

public class GuiManager
{

    private final BlockStackerX plugin;
    private final Settings settings;
    private final FileManager.Config guiCfg;

    public GuiManager(BlockStackerX plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
        this.guiCfg = plugin.getFileManager().getConfig("gui.yml");
    }


    public void openStacker(Player requester, StackerBlock stackerBlock)
    {
        String gui = settings.getLinkedGUI(stackerBlock.getType());
        if (guiCfg.get().isSet("guis."+gui))
        {
            String path = "guis."+gui;
            new StackerInventoryProvider(plugin, stackerBlock, path, requester);
        }
    }

}
