package me.epicgodmc.blockstackerx.commands.sub;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.blockstackerx.utils.Utils;
import me.epicgodmc.epicframework.chat.FancyMessage;
import me.epicgodmc.epicframework.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
                        if (plugin.getSettings().stackerTypeExists(type)) {
                            ItemStack stacker = plugin.getSettings().getStacker(type).addStringTag("stackerType", type).amount(amount).build();
                            if (Utils.hasAvailableSlot(target, stacker, amount)) {
                                if (plugin.getSettings().doGlow(type)) {
                                    ItemMeta meta = stacker.getItemMeta();
                                    meta.addEnchant(Enchantment.LUCK, 1, true);
                                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    stacker.setItemMeta(meta);
                                }
                                target.getInventory().addItem(stacker);
                            } else {
                                plugin.getLangManager().getText("targetNoInventorySpace").replaceText("%s", target.getName()).send(player, true);
                            }
                        } else {
                            plugin.getLangManager().getText("stackerNotFound").replaceText("%s", type).send(player, true);
                        }
                    } else {
                        plugin.getLangManager().getText("couldNotParseNumeric").replaceText("%s", args[2]).send(player, true);
                    }
                } else {
                    plugin.getLangManager().getText("targetNotFound").replaceText("%s", args[0]).send(player, true);
                }
            } else {
                new FancyMessage(info(), true).send(commandSender, false);
            }
        } else {
            plugin.getLangManager().getText("onlyPlayers").send(commandSender, false);
        }
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
    public String info() {
        return "&f&l* &d/BlockStackerX give &8<&7Player&8> <&7Type&8> <&7Amount&8>";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
