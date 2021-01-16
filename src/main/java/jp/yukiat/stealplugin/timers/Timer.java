package jp.yukiat.stealplugin.timers;

import jp.yukiat.stealplugin.config.EffectCore;
import jp.yukiat.stealplugin.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Timer extends BukkitRunnable
{

    private static ArrayList<EffectCore> getEffect(Player player)
    {
        ArrayList<EffectCore> cores = new ArrayList<>();
        try
        {
            Arrays.stream(Objects.requireNonNull(player.getEquipment()).getArmorContents())
                    .forEach(itemStack -> {
                        EffectCore core = getEffectFromItem(itemStack);
                        if (core == null)
                            return;
                        cores.add(core);
                    });
        }
        catch (Exception ignored)
        {
        }

        return cores;
    }

    private static EffectCore getEffectFromItem(ItemStack stack)
    {
        EffectCore core = new EffectCore();
        try
        {
            core.particle = Particle.valueOf(ItemUtil.getMetadata(stack, "particle_name"));
            core.count = Integer.parseInt(ItemUtil.getMetadata(stack, "particle_count"));
            core.offsetX = Double.parseDouble(ItemUtil.getMetadata(stack, "particle_offset_x"));
            core.offsetY = Double.parseDouble(ItemUtil.getMetadata(stack, "particle_offset_y"));
            core.offsetZ = Double.parseDouble(ItemUtil.getMetadata(stack, "particle_offset_z"));
            core.extra = Double.parseDouble(ItemUtil.getMetadata(stack, "particle_extra"));
        }
        catch (Exception ignored)
        {
            return null;
        }

        return core;
    }

    @Override
    public void run()
    {
        Bukkit.getOnlinePlayers()
                .forEach(player -> {
                    ArrayList<EffectCore> cores = getEffect(player);
                    if (cores.size() == 0)
                        return;

                    cores.forEach(core ->
                            player.getWorld().spawnParticle(
                                    core.particle, player.getLocation(), core.count, core.offsetX, core.offsetY, core.offsetZ, core.extra)
                    );
                });
    }
}
