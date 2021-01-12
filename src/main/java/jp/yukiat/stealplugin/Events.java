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
        if (!(e.getRightClicked() instanceof Player)) //右クリックしたやつがプレイヤーじゃないので除外
            return;

        if (!e.getPlayer().isSneaking()) //スニークしてないので除外
            return;

        if (!StealPlugin.config.getList("thief").contains(e.getPlayer().getName()))
            return; //盗人可能リストにいないので除外

        Player clicked = (Player) e.getRightClicked();

        if (!StealPlugin.config.getList("target").contains(clicked.getName()))
            return; //ターゲット可能リストにいないので除外

        if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR &&
                e.getPlayer().getInventory().getItemInMainHand().getType() != Material.SHEARS)
            return;

        boolean useShears = e.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS;

        int i = 0;

        if (PlayerUtil.hasMetaData(clicked, "steal"))
        {
            Optional<MetadataValue> mbs = PlayerUtil.getMetaData(clicked, "steal");
            if (mbs.isPresent())
                i = mbs.get().asInt();
        }


        int leng = ArmorType.values().length;


        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                clicked.getInventory().setHelmet(PlayerUtil.getSkullStack(PlayerUtil.getSkin(clicked.getUniqueId())));
            }
        }.runTaskAsynchronously(StealPlugin.getPlugin());


        final int[] finalI = {i};
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                String name = clicked.getName();
                Skin skin = SkinContainer.getSkinBy(name, finalI[0]);


                if (skin == null)
                    skin = SkinContainer.getSkinBy("$default", finalI[0]);
                if (skin == null)
                    skin = SkinContainer.getSkinBy("$default", 0);
                if (skin != null && !(leng <= finalI[0]))
                    PlayerUtil.setSkin(clicked, skin.value, skin.signature);

                ItemStack st = ItemFactory.getThiefItem(clicked, leng <= finalI[0] ? RandomUtil.pickRandom(ArmorType.values()): ArmorType.values()[finalI[0]], MaterialType.LEATHER);
                if (useShears)
                {
                    e.getPlayer().getWorld().dropItem(clicked.getLocation().add(0, 1, 0), st);
                    return;
                }
                e.getPlayer().getInventory().setItemInMainHand(st);

                if (!(leng <= finalI[0]))
                {
                    PlayerUtil.setMetaData(clicked, "steal", ++finalI[0]);
                    if (!StealPlugin.getPlugin().stealed.contains(clicked.getUniqueId()))
                        StealPlugin.getPlugin().stealed.add(clicked.getUniqueId());
                }
            }
        }.runTaskLater(StealPlugin.getPlugin(), 2); //スキン取得のラグを考慮
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e)
    {
        if (!PlayerUtil.hasMetaData(e.getPlayer(), "steal"))
            return;
        if (StealPlugin.getPlugin().stealed.contains(e.getPlayer().getUniqueId()))
            return;
        PlayerUtil.removeMetaData(e.getPlayer(), "steal");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        StealPlugin.getPlugin().stealed.remove(e.getPlayer().getUniqueId());
        PlayerUtil.removeMetaData(e.getPlayer(), "steal");
    }

}
