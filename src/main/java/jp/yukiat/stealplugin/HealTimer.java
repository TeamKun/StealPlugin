package jp.yukiat.stealplugin;

import jp.yukiat.stealplugin.config.*;
import jp.yukiat.stealplugin.utils.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;
import org.bukkit.scheduler.*;

import java.util.*;

public class HealTimer extends BukkitRunnable
{

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run()
    {
        ArrayList<UUID> remove = new ArrayList<>();
        StealPlugin.getPlugin().stealed.forEach(uuid -> {
            if (Bukkit.getPlayer(uuid) == null)
            {
                remove.add(uuid);
                return;
            }

            Player player = Bukkit.getPlayer(uuid);
            Optional<MetadataValue> stealed = PlayerUtil.getMetaData(player, "order");

            if (!stealed.isPresent())
            {
                remove.add(uuid);
                return;
            }

            int order = stealed.get().asInt();

            order--;

            if (order >= 0)
            {
                Skin data = SkinContainer.getSkinByOrder(order);
                if (data != null)
                    PlayerUtil.setSkin(player, data);
                else
                    PlayerUtil.setDefaultSkinAsync(player);
                player.getWorld().spawnParticle(
                        Particle.COMPOSTER,
                        player.getLocation().add(0, 1, 0),
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0
                );
                PlayerUtil.setMetaData(player, "order", order);
                return;
            }

            remove.add(uuid);
        });

        remove.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null)
                PlayerUtil.removeMetaData(player, "order");
            StealPlugin.getPlugin().stealed.remove(uuid);
        });
    }

}
