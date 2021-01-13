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
        //右クリックしたやつがプレイヤーじゃないので除外
        if (!(e.getRightClicked() instanceof Player))
            return;

        //スニークしてないので除外
        if (!e.getPlayer().isSneaking())
            return;

        //盗人可能リストにいないので除外
        if (!StealPlugin.config.getList("thief").contains(e.getPlayer().getName()))
            return;

        Player clicked = (Player) e.getRightClicked();

        //ターゲット可能リストにいないので除外
        if (!StealPlugin.config.getList("target").contains(clicked.getName()))
            return;

        // 素手もしくはハサミを持っていなければ除外
        if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
            e.getPlayer().sendMessage(ChatColor.RED + "素手じゃないと服を盗めないよ！");
            return;
        }

        int i = 0;

        if (PlayerUtil.hasMetaData(clicked, "steal"))
        {
            Optional<MetadataValue> mbs = PlayerUtil.getMetaData(clicked, "steal");
            if (mbs.isPresent())
                i = mbs.get().asInt();
        }

        int len = ArmorType.values().length; // 3

        if (i >= len)
        {
            e.getPlayer().sendMessage(ChatColor.RED + "ふくきてないよ！！！");
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                clicked.getInventory().setHelmet(PlayerUtil.getSkullStack(PlayerUtil.getSkin(clicked.getUniqueId())));
            }
        }.runTaskAsynchronously(StealPlugin.getPlugin());

        final int order = i;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Skin skin = SkinContainer.getSkinByOrder(order + 1);

                // もしスキンが見つからなければ
                if (skin == null)
                    skin = SkinContainer.getSkinByOrder(0);
                if (skin != null)
                    PlayerUtil.setSkin(clicked, skin.value, skin.signature);

                clicked.sendMessage(ChatColor.GREEN + ArmorType.values()[order].getDisplayName() + "を盗まれました。");

                ItemStack st = ItemFactory.getThiefItem(clicked, ArmorType.values()[order], MaterialType.LEATHER);
                e.getPlayer().getInventory().setItemInMainHand(st);

                PlayerUtil.setMetaData(clicked, "steal", order + 1);
                if (!StealPlugin.getPlugin().stealed.contains(clicked.getUniqueId()))
                    StealPlugin.getPlugin().stealed.add(clicked.getUniqueId());

            }
        }.runTaskLater(StealPlugin.getPlugin(), 2); //スキン取得のラグを考慮
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e)
    {
        if (StealPlugin.config.getStringList("target").contains(e.getPlayer().getName()))
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    e.getPlayer().getInventory().setHelmet(PlayerUtil.getSkullStack(PlayerUtil.getSkin(e.getPlayer().getUniqueId())));
                }
            }.runTaskAsynchronously(StealPlugin.getPlugin());
            Skin def = SkinContainer.getSkinByOrder(0);
            if (def == null)
                return;
            PlayerUtil.setSkin(e.getPlayer(), def.value, def.signature);
            PlayerUtil.setMetaData(e.getPlayer(), "steal", 0);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        StealPlugin.getPlugin().stealed.remove(e.getPlayer().getUniqueId());
        PlayerUtil.removeMetaData(e.getPlayer(), "steal");
    }

}
