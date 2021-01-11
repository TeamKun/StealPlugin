package jp.yukiat.stealplugin.commands;

import com.mojang.authlib.*;
import com.mojang.authlib.properties.*;
import jp.yukiat.stealplugin.*;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;
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
        setSkin(player,  StealPlugin.getPlugin().getDataMap().get(args[0]).getValue(), StealPlugin.getPlugin().getDataMap().get(args[0]).getSignature());
        sender.sendMessage(ChatColor.GREEN + "スキンを変更しました。");

        return true;
    }

    public void setSkin(Player p, String texture, String s){
        GameProfile gp = ((CraftPlayer)p).getProfile();
        gp.getProperties().clear();
        gp.getProperties().put("textures", new Property("textures", texture, s));
        // Update the player

        EntityPlayer ep = ((CraftPlayer) p).getHandle();
        p.getWorld().getPlayers().forEach(player -> {
            System.out.println("BIFO=: " + player.getName());
            System.out.println("Hide Send: " + player.getName());
            player.hidePlayer(StealPlugin.getPlugin(), p);
            System.out.println("Hide Sent: " + player.getName());
            System.out.println("Show Send: " + player.getName());
            player.showPlayer(StealPlugin.getPlugin(), p);
            System.out.println("Show Sent: " + player.getName());
            System.out.println("AFUTA=: " + player.getName());

        });

    }

}
