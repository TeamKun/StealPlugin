package jp.yukiat.stealplugin.commands;

import com.mojang.authlib.*;
import com.mojang.authlib.properties.*;
import jp.yukiat.stealplugin.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.*;
import java.util.*;

public class Skull implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "エラー：プレイヤーから実行してください！");
            return true;
        }

        if (args.length != 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー：引数の数がおかしいです！使用法：/skull <なまえ>");
            return true;
        }

        if (!StealPlugin.getPlugin().getDataMap().containsKey(args[0]))
        {
            sender.sendMessage(ChatColor.RED + "エラー：認識できないスキン名です。");
            return true;
        }

        Player player = (Player) sender;

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);//あーね
        profile.getProperties().put("textures", new Property("textures", StealPlugin.getPlugin().getDataMap().get(args[0]).getValue(), StealPlugin.getPlugin().getDataMap().get(args[0]).getSignature()));

        try
        {
            Method mtd = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(meta, profile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        skull.setItemMeta(meta);

        player.getInventory().addItem(skull);
        sender.sendMessage(ChatColor.GREEN + "ヘッドを入手しました。");

        return true;
    }
}
