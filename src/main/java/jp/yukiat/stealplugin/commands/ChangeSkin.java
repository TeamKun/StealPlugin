package jp.yukiat.stealplugin.commands;

import jp.yukiat.stealplugin.*;
import jp.yukiat.stealplugin.utils.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

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
            sender.sendMessage(ChatColor.RED + "エラー：引数の数がおかしいです！使用法：/change <おーだーばんごう>");
            return true;
        }

        if (!StealPlugin.getPlugin().getDataMap().containsKey(args[0]))
        {
            sender.sendMessage(ChatColor.RED + "エラー：認識できないスキン名です。");
            return true;
        }

        Player player = (Player) sender;
        PlayerUtil.setSkin(player, StealPlugin.getPlugin().getDataMap().get(args[0]).getValue(), StealPlugin.getPlugin().getDataMap().get(args[0]).getSignature());
        sender.sendMessage(ChatColor.GREEN + "スキンを変更しました。");

        return true;
    }

}
