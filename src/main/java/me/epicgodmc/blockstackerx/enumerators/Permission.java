package me.epicgodmc.blockstackerx.enumerators;

import me.epicgodmc.blockstackerx.BlockStackerX;
import org.bukkit.command.CommandSender;

public enum Permission
{

    // base cmd = bs.use

    BASECOMMAND_USE("use"),
    STACKER_GIVE("giveStacker"),
    BYPASS("bypass"),
    STACKER_BREAK("breakStacker"),
    STACKER_ADD_BLOCKS("addBlocks"),
    STACKER_SUB_BLOCKS("subBlocks"),
    TOP_STACKERS("topcmd"),
    STACKER_PLACE("placeStacker");


    public final String node;

    Permission(String node) {
        this.node = node;
    }

    public String getNode() {
        return "dungeons." + node;
    }

    public boolean has(CommandSender sender, BlockStackerX plugin) {
        boolean has = sender.hasPermission(getNode()) || sender.hasPermission("bs.*");
        if (!has) {
            BlockStackerX.inst().getLangManager().getText("cmdNoPermission").send(sender, true);
        }
        return has;
    }

    public boolean has(CommandSender sender, boolean informSender) {
        boolean has = sender.hasPermission(getNode()) || sender.hasPermission("bs.*");
        if (!has && informSender) {
            BlockStackerX.inst().getLangManager().getText("cmdNoPermission").send(sender, true);
        }
        return has;
    }

}
