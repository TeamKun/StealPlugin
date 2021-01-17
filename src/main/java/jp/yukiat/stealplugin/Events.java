package jp.yukiat.stealplugin;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import jp.yukiat.stealplugin.config.Skin;
import jp.yukiat.stealplugin.config.SkinContainer;
import jp.yukiat.stealplugin.enums.ArmorType;
import jp.yukiat.stealplugin.enums.MaterialType;
import jp.yukiat.stealplugin.timers.HealTimer;
import jp.yukiat.stealplugin.utils.Decorations;
import jp.yukiat.stealplugin.utils.PlayerUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

        if (StealPlugin.config.getList("remote").contains("*"))
        {
            if (StealPlugin.config.getList("remote").contains(thief.getName()))
                return;
        }
        else
        {
            if (!StealPlugin.config.getList("remote").contains(thief.getName()))
                return;
        }

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
        List<?> thiefList = StealPlugin.config.getList("thief");
        boolean isThiefBlackListed = thiefList.contains("*");

        BaseComponent[] a =  new ComponentBuilder(ChatColor.RED + "盗めないよ！！！").create();

        if (isThiefBlackListed && thiefList.contains(thief.getName()))
        {
            thief.spigot().sendMessage(a);
            return false;
        }

        if (!isThiefBlackListed && !thiefList.contains(thief.getName()))
        {
            thief.spigot().sendMessage(a);
            return false;
        }

        if (StealPlugin.getPlugin().stealing.contains(thief.getUniqueId()))
        {
            thief.spigot().sendMessage(a);
            return false;
        }

        List<?> targetList = StealPlugin.config.getList("target");
        boolean isTargetBlackListed = targetList.contains("*");

        if (isTargetBlackListed && targetList.contains(target.getName()))
        {
            thief.spigot().sendMessage(a);
            return false;
        }

        if (!isTargetBlackListed && !targetList.contains(target.getName()))
        {
            thief.spigot().sendMessage(a);
            return false;
        }

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

        int order = 0;

        if (PlayerUtil.getMetaData(target, "order").isPresent())
            order = PlayerUtil.getMetaData(target, "order").get().asInt();

        if (order >= 3)
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

        int order = 0;
        if (PlayerUtil.getMetaData(target, "order").isPresent())
            order = PlayerUtil.getMetaData(target, "order").get().asInt();

        int len = 3;

        StealPlugin.getPlugin().stealing.add(thief.getUniqueId());

        setOwnSkull(target);

        int finalOrder = order;
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
                    for (int i = finalOrder; i < len; i++)
                    {
                        ItemStack item = ItemFactory.getThiefItem(target, ArmorType.values()[i], MaterialType.LEATHER);

                        //ItemStack[] aa = target.getInventory().getArmorContents();
                        //aa[order] = new ItemStack(Material.AIR);

                        //target.getInventory().setArmorContents(aa);
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
                            .filter(player -> !player.getName().equals(target.getName()) && !target.getName().equals(thief.getName()))
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
                    skin = SkinContainer.getSkinByOrder(finalOrder + 1);

                    //ItemStack[] aa = target.getInventory().getArmorContents();
                    //aa[order] = new ItemStack(Material.AIR);

                    //target.getInventory().setArmorContents(aa);

                    ItemStack item = ItemFactory.getThiefItem(target, ArmorType.values()[finalOrder], MaterialType.LEATHER);
                    thief.getInventory().setItemInMainHand(item);

                    Bukkit.getOnlinePlayers().stream()
                            .filter(player -> !(player.getName().equals(target.getName()) || target.getName().equals(thief.getName())))
                            .forEach(
                                    player -> {
                                        player.sendMessage(ChatColor.GOLD + thief.getName() + ChatColor.GREEN + "が" +
                                                ChatColor.GOLD + target.getName() + ChatColor.GREEN + "の" +
                                                ChatColor.GOLD + ArmorType.values()[finalOrder].getDisplayName() +
                                                ChatColor.GREEN + "を盗みました！");
                                    }
                            );
                    thief.sendMessage(ChatColor.GOLD + target.getName() +
                            ChatColor.GREEN + "の" + ArmorType.values()[finalOrder].getDisplayName() + "を盗みました！");
                    target.sendMessage(ChatColor.GOLD + thief.getName() +
                            ChatColor.GREEN + "に" + ArmorType.values()[finalOrder].getDisplayName() + "を盗まれました！");

                    PlayerUtil.setMetaData(target, "order", finalOrder + 1);
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

    @EventHandler
    public void onEquipment(PlayerArmorChangeEvent event)
    {
        try
        {
            if (event.getSlotType() == PlayerArmorChangeEvent.SlotType.HEAD || Objects.requireNonNull(event.getOldItem()).getType() != Material.AIR ||
                    Objects.requireNonNull(event.getNewItem()).getType() == Material.AIR)
                return;

            Optional<MetadataValue> stealed = PlayerUtil.getMetaData(event.getPlayer(), "order");

            if (!stealed.isPresent())
                return;

            int order = stealed.get().asInt();

            if (order <= 0)
                return;

            HealTimer.heal(event.getPlayer().getUniqueId());


            ArrayList<ItemStack> aa = new ArrayList<>(Arrays.asList(event.getPlayer().getInventory().getArmorContents()));
            aa.remove(event.getNewItem());
            aa.add(0, new ItemStack(Material.AIR));
            event.getPlayer().getInventory().setArmorContents(aa.toArray(new ItemStack[0]));

        }
        catch (Exception e)
        {
        }
    }
}
