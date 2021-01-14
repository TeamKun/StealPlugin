package jp.yukiat.stealplugin;

import jp.yukiat.stealplugin.config.*;
import jp.yukiat.stealplugin.enums.*;
import jp.yukiat.stealplugin.utils.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.metadata.*;
import org.bukkit.scheduler.*;

import java.util.*;

public class Events implements Listener
{
    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("ConstantConditions")
    public void onClickEvent(PlayerInteractEntityEvent  e)
    {
        Player thief = e.getPlayer();

        //右クリックしたやつがプレイヤーじゃないので除外
        if (!(e.getRightClicked() instanceof Player))
            return;

        //スニークしてないので除外
        if (!thief.isSneaking())
            return;

        //盗人可能リストにいないので除外
        if (!StealPlugin.config.getList("thief").contains(thief.getName()))
            return;

        Player target = (Player) e.getRightClicked();

        //ターゲット可能リストにいないので除外
        if (!StealPlugin.config.getList("target").contains(target.getName()))
            return;

        // 素手もしくはハサミを持っていなければ除外
        if (thief.getInventory().getItemInMainHand().getType() != Material.AIR) {
            thief.sendMessage(ChatColor.RED + "素手じゃないと服を盗めないよ！");
            return;
        }

        int temp = 0;

        if (PlayerUtil.hasMetaData(target, "order"))
        {
            Optional<MetadataValue> mbs = PlayerUtil.getMetaData(target, "order");
            if (mbs.isPresent())
                temp = mbs.get().asInt();
        }
        final int order = temp;

        int len = ArmorType.values().length; // 3

        if (order >= len) {
            thief.sendMessage(ChatColor.RED + "ふくきてないよ！！！");
            return;
        }

        setOwnSkull(target);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Skin skin;

                // 一撃で脱げる人は一発で全裸のスキンに変更
                if (StealPlugin.config.getList("oneshots").contains(target.getName())) {
                    skin = SkinContainer.getSkinByOrder(len);
                    for (int i = order; i < len; i++) {
                        thief.sendMessage(ChatColor.RED + target.getName() + "の" +
                                ChatColor.GREEN + ArmorType.values()[i].getDisplayName() + "を盗みました！");
                        target.sendMessage(ChatColor.RED + thief.getName() + "に" +
                                ChatColor.GREEN + ArmorType.values()[i].getDisplayName() + "を盗まれました！");
                    }
                } else {
                    skin = SkinContainer.getSkinByOrder(order + 1);
                    thief.sendMessage(ChatColor.RED + target.getName() + "の" +
                            ChatColor.GREEN + ArmorType.values()[order + 1].getDisplayName() + "を盗みました！");
                    target.sendMessage(ChatColor.RED + thief.getName() + "に" +
                            ChatColor.GREEN + ArmorType.values()[order + 1].getDisplayName() + "を盗まれました！");
                }

                // もしスキンが見つからなければ
                if (skin == null)
                    skin = SkinContainer.getSkinByOrder(0);
                if (skin != null)
                    PlayerUtil.setSkin(target, skin);

                ItemStack st = ItemFactory.getThiefItem(target, ArmorType.values()[order], MaterialType.LEATHER);
                thief.getInventory().setItemInMainHand(st);

                PlayerUtil.setMetaData(target, "order", order + 1);
                if (!StealPlugin.getPlugin().stealed.contains(target.getUniqueId()))
                    StealPlugin.getPlugin().stealed.add(target.getUniqueId());

            }
        }.runTaskLater(StealPlugin.getPlugin(), 2); //スキン取得のラグを考慮
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e)
    {
        if (StealPlugin.config.getStringList("target").contains(e.getPlayer().getName()))
        {
            setOwnSkull(e.getPlayer());

            Skin defaultSkin = SkinContainer.getSkinByOrder(0);
            if (defaultSkin == null)
                return;
            PlayerUtil.setSkin(e.getPlayer(), defaultSkin);
            PlayerUtil.setMetaData(e.getPlayer(), "order", 0);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        StealPlugin.getPlugin().stealed.remove(e.getPlayer().getUniqueId());
        PlayerUtil.removeMetaData(e.getPlayer(), "order");
    }

    private void setOwnSkull(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.getInventory().setHelmet(PlayerUtil.getSkullStack(PlayerUtil.getSkin(player.getUniqueId())));
            }
        }.runTaskAsynchronously(StealPlugin.getPlugin());
    }

}
