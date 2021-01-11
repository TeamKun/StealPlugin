package jp.yukiat.stealplugin.commands;

import com.mojang.authlib.*;
import com.mojang.authlib.properties.*;
import jp.yukiat.stealplugin.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.*;
import org.bukkit.entity.*;
import org.inventivetalent.nicknamer.api.*;

public class ChangeSkin implements CommandExecutor
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
        GameProfile profile = ((CraftPlayer) player).getHandle().getProfile();
        profile.getProperties().put("textures", new Property("textures", StealPlugin.getPlugin().getDataMap().get(args[0]).getValue(), StealPlugin.getPlugin().getDataMap().get(args[0]).getSignature()));
        NickNamerAPI.getNickManager().loadCustomSkin("yaju_" + args[0], profile);
        NickNamerAPI.getNickManager().setCustomSkin(player.getUniqueId(), "yaju_" + args[0]);

        sender.sendMessage(ChatColor.GREEN + "スキンを変更しました。");

        return true;
    }
}
