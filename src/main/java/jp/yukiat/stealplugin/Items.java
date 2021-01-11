package jp.yukiat.stealplugin;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class Items
{
    public static void addItemSafe(Player p, ItemStack stack)
    {
        PlayerInventory inventory = p.getInventory();
        if (inventory.getItemInMainHand().getType() == Material.AIR)
        {
            inventory.setItemInMainHand(stack);
            return;
        }

        inventory.addItem(inventory.getItemInMainHand());
        inventory.setItemInMainHand(stack);
    }

    public static void confirmThief(Player thief, Player target, boolean shear)
    {

    }

}
