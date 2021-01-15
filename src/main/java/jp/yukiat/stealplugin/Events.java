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
    public void onClickEvent(PlayerInteractEntityEvent e)
    {
        Player thief = e.getPlayer();

        // オフハンドなら返す
        if (e.getHand().equals(EquipmentSlot.OFF_HAND))
        {
            return;
        }

        //右クリックしたやつがプレイヤーじゃないので除外
        if (!(e.getRightClicked() instanceof Player))
            return;

        //スニークしてないので除外
        if (!thief.isSneaking())
            return;

        //盗人可能リストにいないので除外
        if (!StealPlugin.config.getList("thief").contains(thief.getName()))
        {
            thief.sendMessage("盗めないよ！！！");
            return;
        }

        Player target = (Player) e.getRightClicked();

        //ターゲット可能リストにいないので除外
        if (!StealPlugin.config.getList("target").contains(target.getName()))
            return;

        // 素手もしくはハサミを持っていなければ除外
        if (thief.getInventory().getItemInMainHand().getType() != Material.AIR)
        {
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

        if (order >= len)
        {
            thief.sendMessage(ChatColor.RED + "もうはだかだよ！！！");
            return;
        }

        setOwnSkull(target);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Skin skin;
                World world = target.getWorld();

                // 一撃で脱げる女
                if (StealPlugin.config.getList("oneshots").contains(target.getName()))
                {
                    skin = SkinContainer.getSkinByOrder(len);

                    for (int i = order; i < len; i++)
                    {
                        ItemStack item = ItemFactory.getThiefItem(target, ArmorType.values()[i], MaterialType.LEATHER);

                        Location location = new Location(
                                target.getWorld(),
                                (target.getLocation().getX() + thief.getLocation().getX() * 2.0) / 3.0,
                                (target.getLocation().getY() + thief.getLocation().getY() * 2.0) / 3.0,
                                (target.getLocation().getZ() + thief.getLocation().getZ() * 2.0) / 3.0
                        );
                        world.dropItem(location, item);

                        thief.sendMessage(ChatColor.GOLD + target.getName() +
                                ChatColor.GREEN + "の" + ArmorType.values()[i].getDisplayName() + "を盗みました！");
                        target.sendMessage(ChatColor.GOLD + thief.getName() +
                                ChatColor.GREEN + "に" + ArmorType.values()[i].getDisplayName() + "を盗まれました！");
                    }

                    PlayerUtil.setMetaData(target, "order", len);
                }
                // 一般の女
                else
                {
                    skin = SkinContainer.getSkinByOrder(order + 1);

                    ItemStack item = ItemFactory.getThiefItem(target, ArmorType.values()[order], MaterialType.LEATHER);
                    thief.getInventory().setItemInMainHand(item);

                    thief.sendMessage(ChatColor.GOLD + target.getName() +
                            ChatColor.GREEN + "の" + ArmorType.values()[order].getDisplayName() + "を盗みました！");
                    target.sendMessage(ChatColor.GOLD + thief.getName() +
                            ChatColor.GREEN + "に" + ArmorType.values()[order].getDisplayName() + "を盗まれました！");

                    PlayerUtil.setMetaData(target, "order", order + 1);
                }

                // パーティクル
                // 候補：
                // NAUTILUS->コンジットのやつ
                // HEART->シンプルにハート
                // SPELL_MOB_AMBIENT
                world.spawnParticle(
                        Particle.CLOUD,
                        target.getLocation().add(0, 1, 0),
                        50,
                        0.1,
                        0.3,
                        0.1,
                        0.05
                );

                // サウンド
                world.playSound(target.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                // もしスキンが見つからなければ
                if (skin == null)
                    skin = SkinContainer.getSkinByOrder(0);
                if (skin != null)
                    PlayerUtil.setSkin(target, skin);

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

    private void setOwnSkull(Player player)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.getInventory().setHelmet(PlayerUtil.getSkullStack(PlayerUtil.getSkin(player.getUniqueId())));
            }
        }.runTaskAsynchronously(StealPlugin.getPlugin());
    }

}
