package jp.yukiat.stealplugin.commands;

import jp.yukiat.stealplugin.utils.ItemUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                                    ChatColor.LIGHT_PURPLE + "   " + s2.replace("§", "&").replace("\n", "\n" + ChatColor.LIGHT_PURPLE)
                            );
                    builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s2.replace("\n", "")));

                    p.spigot().sendMessage(builder.create());
                    flag[0] = true;
                });
        if (!flag[0])
            p.sendMessage(ChatColor.LIGHT_PURPLE + "No metadata(s) founded.");
        return true;
    }
}
