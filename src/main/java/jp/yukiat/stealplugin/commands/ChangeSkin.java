package jp.yukiat.stealplugin.commands;

import jp.yukiat.stealplugin.*;
import jp.yukiat.stealplugin.config.Skin;
import jp.yukiat.stealplugin.config.SkinContainer;
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

        int order;

        try {
            order = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "エラー：引数の指定がおかしいです！使用法：/change <おーだー番号>");
            return true;
        }

        Skin skin = SkinContainer.getSkinByOrder(order);

        if (skin == null) {
            sender.sendMessage(ChatColor.RED + "エラー：スキンが存在しません！");
            return true;
        }

        Player player = (Player) sender;
        PlayerUtil.setSkin(player, skin.value, skin.signature);
        sender.sendMessage(ChatColor.GREEN + "スキンを変更しました。");

        return true;
    }

}
