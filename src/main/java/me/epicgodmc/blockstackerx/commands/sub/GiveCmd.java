package me.epicgodmc.blockstackerx.commands.sub;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.blockstackerx.utils.Utils;

import me.epicgodmc.epicapi.chat.Message;
import me.epicgodmc.epicapi.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCmd extends SubCommand {


    private final BlockStackerX plugin;

    public GiveCmd(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    // bs give <player> <type> <amount>
    @Override
    public void onCommand(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player) {
            if (args.length == 3) {
                Player player = (Player) commandSender;
                Player target = Utils.parsePlayer(args[0]);
                if (target != null) {
                    String type = args[1];
                    int amount = Utils.parseInteger(args[2]);
                    if (amount != -1) {
                        if (plugin.getStackerSettings().stackerExists(type)) {
                            ItemStack stacker = plugin.getStackerSettings().getStacker(type).addStringTag("stackerType", type).setAmount(amount).build();
                            if (Utils.hasAvailableSlot(target, stacker, amount)) {
                                target.getInventory().addItem(stacker);
                            } else plugin.getLangSettings().getText("targetNoInventorySpace", true).addPlaceHolder("%s", target.getName()).send(player);
                        } else plugin.getLangSettings().getText("stackerNotFound", true).addPlaceHolder("%s", type).send(player);
                    } else plugin.getLangSettings().getText("couldNotParseNumeric", true).addPlaceHolder("%s", args[2]).send(player);
                } else plugin.getLangSettings().getText("targetNotFound", true).addPlaceHolder("%s", args[0]).send(player);
            } else new Message(info()).send(commandSender);
        } else plugin.getLangSettings().sendText(commandSender, "onlyPlayers", false);

    }

    @Override
    public String name() {
        return "give";
    }

    @Override
    public String requiredPermission() {
        return Permission.STACKER_GIVE.getNode();
    }

    @Override
    public boolean isPlayerCmd() {
        return false;
    }

    @Override
    public String info() {
        return "&f&l* &d/BlockStackerX give &8<&7Player&8> <&7Type&8> <&7Amount&8>";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
