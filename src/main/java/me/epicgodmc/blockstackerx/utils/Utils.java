package me.epicgodmc.blockstackerx.utils;

import me.epicgodmc.epicframework.chat.FancyMessage;
import me.epicgodmc.epicframework.item.XMaterial;
import me.epicgodmc.epicframework.nbt.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static String getType(ItemStack stack) {
        if (stack == null || stack.getType().equals(Material.AIR)) return "n/a";
        NBTItem nbtItem = new NBTItem(stack);
        if (nbtItem.hasNBTData() && nbtItem.hasKey("stackerType")) return nbtItem.getString("stackerType");

        return "n/a";
    }

    public static String getStackerState(ItemStack stack)
    {
        if (stack == null || stack.getType().equals(Material.AIR)) return "n/a";
        NBTItem nbtItem = new NBTItem(stack);

        if (nbtItem.hasNBTData())
        {
            if (nbtItem.hasKey("stackerType")) return "BLOCK";
            if (nbtItem.hasKey("stackerItemType")) return "ITEM";
        }
        return "n/a";
    }

    public static Map<String, Object> getStackerItemData(ItemStack stack)
    {
        NBTItem nbtItem = new NBTItem(stack);
        Map<String, Object> output = new HashMap<>();

        output.put("type", nbtItem.getString("stackerItemType"));
        output.put("material", Material.valueOf(nbtItem.getString("StackerMaterial").toUpperCase()));
        output.put("value", nbtItem.getInteger("stackerValue"));

        return output;
    }

    public static int subtractPlayerHandAmount(Player player, int a) {
        int handAmt = player.getItemInHand().getAmount();
        if (a >= handAmt) {
            player.setItemInHand(new ItemStack(Material.AIR));
            return handAmt;
        }
        ItemStack newAmt = new ItemStack(player.getItemInHand().getType(), handAmt - a);
        player.setItemInHand(newAmt);
        return a;
    }

    public static void removeMaterials(Player player, Material mat, int a) {
        int i = a;

        PlayerInventory inv = player.getInventory();
        for (int slot = 0; slot < 36; slot++) {
            if (i <= 0) break;

            ItemStack stack = inv.getItem(slot);
            if (stack != null && !stack.getType().equals(Material.AIR) && !isStacker(stack)) {
                if (stack.getType().equals(mat)) {
                    if (i >= 64) {
                        i -= decrementSlot(player, slot, 64);
                    } else i -= decrementSlot(player, slot, i);
                }
            }
        }

    }

    private static int decrementSlot(Player player, int slot, int amount) {

        ItemStack stack = player.getInventory().getItem(slot);
        if (amount > stack.getAmount()) {
            stack.setAmount(stack.getAmount() - amount);
            player.getInventory().setItem(slot, stack);
            return amount;
        } else {
            player.getInventory().clear(slot);
            return amount;
        }
    }

    public static int countMaterials(ItemStack[] inv, Material mat) {
        int materials = 0;
        int slotsCounted = 0;
        for (ItemStack stack : inv) {
            if (slotsCounted > 25) {
                break;
            }
            if (stack == null || stack.getType().equals(Material.AIR)) continue;
            if (stack.getType().equals(mat)) {
                if (!isStacker(stack)) {
                    materials += stack.getAmount();
                }
            }
            slotsCounted++;
        }
        return materials;
    }

    public static int countOpenStorage(Player player, Material mat) {
        int space = 0;


        PlayerInventory inv = player.getInventory();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = inv.getItem(slot);

            if (stack != null && !stack.getType().equals(Material.AIR)) {
                if (stack.getType().equals(mat)) {
                    space += 64 - stack.getAmount();
                }
            } else {
                space += 64;
            }
        }
        return space;
    }

    public static boolean isStacker(ItemStack stack) {
        if (stack == null || stack.getType().equals(Material.AIR)) return false;
        NBTItem nbtItem = new NBTItem(stack);
        if (nbtItem.hasNBTData()) return nbtItem.hasKey("stackerType") || nbtItem.hasKey("stackerItemType");

        return false;
    }

    public static boolean isBlock(ItemStack stack) {
        return stack != null && !stack.getType().equals(Material.AIR);
    }


    public static Player parsePlayer(String player) {
        try {
            return Bukkit.getPlayer(player);
        } catch (Exception e) {
            return null;
        }
    }

    public static int parseInteger(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isPickaxe(Material mat) {
        return mat.equals(XMaterial.WOODEN_PICKAXE.parseMaterial())
                || mat.equals(XMaterial.STONE_PICKAXE.parseMaterial())
                || mat.equals(XMaterial.GOLDEN_PICKAXE.parseMaterial())
                || mat.equals(XMaterial.IRON_PICKAXE.parseMaterial())
                || mat.equals(XMaterial.DIAMOND_PICKAXE.parseMaterial());
    }

    public static boolean hasAvailableSlot(Player player, Material mat, int amt) {
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType().equals(mat) && item.getAmount() <= 64 - amt) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAvailableSlot(Player player, ItemStack stack, int amt) {
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.equals(stack) && item.getAmount() <= 64 - amt) {
                return true;
            }
        }
        return false;
    }


    public static String getCmdUsage() {
        return new FancyMessage("&e&l<----------> &d&lBlockStackerX &e&l<---------->\n" +
                "&f&l* &d/BlockStackerX give &8<&7Player&8> <&7Type&8> <&7Amount&8>", true).get();
    }

}
