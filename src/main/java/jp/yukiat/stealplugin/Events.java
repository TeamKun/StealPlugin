package jp.yukiat.stealplugin;

import jp.yukiat.stealplugin.config.Skin;
import jp.yukiat.stealplugin.config.SkinContainer;
import jp.yukiat.stealplugin.enums.ArmorType;
import jp.yukiat.stealplugin.enums.MaterialType;
import jp.yukiat.stealplugin.utils.Decorations;
import jp.yukiat.stealplugin.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class Events implements Listener
{
    private final double maxDistance = 50;

    @EventHandler(ignoreCancelled = true)
    public void onClickEvent(PlayerInteractEntityEvent e)
    {
        Player thief = e.getPlayer();

        // オフハンドなら返す
        if (e.getHand().equals(EquipmentSlot.OFF_HAND))
            return;

        //右クリックしたやつがプレイヤーじゃないので除外
        if (!(e.getRightClicked() instanceof Player))
            return;

        Player target = (Player) e.getRightClicked();

        if (steal(thief, target))
            StealPlugin.getPlugin().stealing.remove(thief.getUniqueId());
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
    public void onRightClick(PlayerInteractEvent e)
    {
        Bukkit.getLogger().info(e.getAction() + ": " + e.getPlayer());
        // 右クリックじゃなかったらはじく
        if (!e.getAction().equals(Action.LEFT_CLICK_AIR))
            return;

        Player thief = e.getPlayer();
        Player target = PlayerUtil.getLookingEntity(thief);

        // そもそもターゲットがいない場合ははじく
        if (target == null)
            return;

        if (StealPlugin.getPlugin().stealing.contains(thief.getUniqueId()))
        {
            thief.sendMessage(ChatColor.RED + "そんなにすぐにれんぞくではとれないよ！！！");
            return;
        }

        double distance = thief.getLocation().distance(target.getLocation());

        // 距離が近すぎる or 遠すぎる場合ははじく
        if (distance > maxDistance || distance < 3.25)
            return;

        if (!canSteal(thief, target))
            return;

        Decorations.magic(target, 60);
        Decorations.secLinesPlayer(thief, target, 60);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (steal(thief, target))
                    StealPlugin.getPlugin().stealing.remove(thief.getUniqueId());
            }
        }.runTaskLater(StealPlugin.getPlugin(), 60);

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

    private boolean canSteal(Player thief, Player target)
    {
        if (StealPlugin.config.getList("thief").contains("*"))
        { //ブラックリストになる
            if (StealPlugin.config.getList("thief").contains(thief.getName()))
            {
                thief.sendMessage(ChatColor.RED + "盗めないよ！！！");
                return false;
            }
        }
        else
        {
            if (!StealPlugin.config.getList("thief").contains(thief.getName()))
            {
                thief.sendMessage(ChatColor.RED + "盗めないよ！！！");
                return false;
            }
        }

        if (StealPlugin.getPlugin().stealing.contains(thief.getUniqueId()))
        {
            thief.sendMessage(ChatColor.RED + "そんなにすぐにれんぞくではとれないよ！！！");
            return false;
        }

        if (StealPlugin.config.getList("target").contains("*"))
        {
            if (StealPlugin.config.getList("target").contains(thief.getName()))
            {
                thief.sendMessage(ChatColor.RED + "盗めないよ！！！");
                return false;
            }
        }
        else
        {
            if (!StealPlugin.config.getList("target").contains(thief.getName()))
            {
                thief.sendMessage(ChatColor.RED + "盗めないよ！！！");
                return false;
            }
        }

        // 素手じゃなければ除外
        if (thief.getInventory().getItemInMainHand().getType() != Material.AIR)
        {
            thief.sendMessage(ChatColor.RED + "素手じゃないと服を盗めないよ！");
            return false;
        }

        if (StealPlugin.getPlugin().stealing.contains(thief.getUniqueId()))
        {
            thief.sendMessage(ChatColor.RED + "そんなにすぐにれんぞくではとれないよ！！！");
            return false;
        }

        int temp = 0;

        if (PlayerUtil.hasMetaData(target, "order"))
        {
            Optional<MetadataValue> mbs = PlayerUtil.getMetaData(target, "order");
            if (mbs.isPresent())
                temp = mbs.get().asInt();
        }
        final int order = temp;

        int len = ArmorType.values().length - 1; // 3

        if (order >= len)
        {
            thief.sendMessage(ChatColor.RED + "もうはだかだよ！！！");
            return false;
        }

        return true;
    }

    private boolean steal(Player thief, Player target)
    {
        if (!canSteal(thief, target))
            return false;

        int temp = 0;

        if (PlayerUtil.hasMetaData(target, "order"))
        {
            Optional<MetadataValue> mbs = PlayerUtil.getMetaData(target, "order");
            if (mbs.isPresent())
                temp = mbs.get().asInt();
        }
        final int order = temp;

        int len = ArmorType.values().length - 1; // 3

        StealPlugin.getPlugin().stealing.add(thief.getUniqueId());


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

                    Bukkit.getOnlinePlayers().stream()
                            .filter(player -> !(player.getName().equals(target.getName()) || target.getName().equals(thief.getName())))
                            .forEach(
                                    player -> {
                                        player.sendMessage(ChatColor.GOLD + thief.getName() + ChatColor.GREEN + "が" +
                                                ChatColor.GOLD + target.getName() + ChatColor.GREEN + "を" +
                                                ChatColor.RED + ChatColor.BOLD + "全裸" +
                                                ChatColor.RESET + ChatColor.GREEN + "にしました！");
                                    }
                            );

                    PlayerUtil.setMetaData(target, "order", len);
                }
                // 一般の女
                else
                {
                    skin = SkinContainer.getSkinByOrder(order + 1);

                    ItemStack item = ItemFactory.getThiefItem(target, ArmorType.values()[order], MaterialType.LEATHER);
                    thief.getInventory().setItemInMainHand(item);

                    Bukkit.getOnlinePlayers().stream()
                            .filter(player -> !(player.getName().equals(target.getName()) || target.getName().equals(thief.getName())))
                            .forEach(
                                    player -> {
                                        player.sendMessage(ChatColor.GOLD + thief.getName() + ChatColor.GREEN + "が" +
                                                ChatColor.GOLD + target.getName() + ChatColor.GREEN + "の" +
                                                ChatColor.GOLD + ArmorType.values()[order].getDisplayName() +
                                                ChatColor.GREEN + "を盗みました！");
                                    }
                            );
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

        return true;
    }

}
