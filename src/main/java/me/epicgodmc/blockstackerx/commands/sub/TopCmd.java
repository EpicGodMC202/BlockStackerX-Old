package me.epicgodmc.blockstackerx.commands.sub;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.epicapi.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class TopCmd extends SubCommand {


    private final BlockStackerX plugin;

    public TopCmd(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        sendTopTen(commandSender, plugin.getStackerStore().sort());
    }

    private void sendTopTen(CommandSender sender, ArrayList<StackerBlock> sorted)
    {
        plugin.getLangSettings().sendText(sender, "stackerTopHeader", false);
        if (sorted.size() != 0) {
            for (int i = 0; i < 10; i++) {
                if (i <= sorted.size()) {
                    plugin.getLangSettings().getText("stackerTopFormat", false)
                            .addPlaceHolder("%1", i +1)
                            .addPlaceHolder("%2", Bukkit.getOfflinePlayer(sorted.get(i).getOwner()).getName())
                            .addPlaceHolder("%3", sorted.get(i).getValue())
                            .send(sender);
                } else break;
            }
        }
        plugin.getLangSettings().sendText(sender, "stackerTopFooter", false);
    }

    @Override
    public String name() {
        return "top";
    }

    @Override
    public String requiredPermission() {
        return Permission.TOP_STACKERS.getNode();
    }

    @Override
    public boolean isPlayerCmd() {
        return false;
    }

    @Override
    public String info() {
        return "&f&l* &d/BlockStackerX top";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
