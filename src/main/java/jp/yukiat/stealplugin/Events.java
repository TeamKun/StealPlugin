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

        Integer seed = null;
        if (StealPlugin.config.getBoolean("same"))
            seed = e.getPlayer().getName().hashCode();

        int i = 0;

        if (PlayerUtil.hasMetaData(e.getPlayer(), "steal"))
        {
            Optional<MetadataValue> mbs =  PlayerUtil.getMetaData(e.getPlayer(), "steal");
            if (mbs.isPresent())
                i = mbs.get().asInt();
        }

        Integer finalSeed = seed;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                clicked.getInventory().setHelmet(PlayerUtil.getSkullStack(PlayerUtil.getSkin(clicked.getUniqueId())));
            }
        }.runTaskAsynchronously(StealPlugin.getPlugin());


        int finalI = i;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Skin skin = SkinContainer.getSkinBy(clicked.getName(), finalI);
                PlayerUtil.setSkin(clicked, skin.value, skin.signature);

                ItemStack st = ItemFactory.getThiefItem(e.getPlayer(), null, MaterialType.LEATHER);
            }
        }.runTaskLater(StealPlugin.getPlugin(), 100); //スキン取得のラグを考慮
    }

}//返還大暴走
