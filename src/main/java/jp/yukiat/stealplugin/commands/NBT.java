package jp.yukiat.stealplugin.commands;

import jp.yukiat.stealplugin.utils.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public class NBT implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String g, String[] string)
    {
        if (!commandSender.hasPermission("steal.nbt"))
            return true;
        if (!(commandSender instanceof Player))
            return true;
        Player p = (Player) commandSender;
        boolean[] flag = {false};
        ItemUtil.getMetadataList(p.getInventory().getItemInMainHand())
                .forEach((s, s2) -> {

                    ComponentBuilder builder =
                            new ComponentBuilder(ChatColor.GREEN + s + "   " + ChatColor.AQUA + "->" +
                                    ChatColor.LIGHT_PURPLE + "   " + s2.replace("\n", "\n" + ChatColor.LIGHT_PURPLE));
                    builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s2.replace("\n", "")));

                    p.spigot().sendMessage(builder.create());
                    flag[0] = true;
                });
        if (!flag[0])
            p.sendMessage(ChatColor.LIGHT_PURPLE + "No metadata(s) founded.");
        return true;
    }
}
