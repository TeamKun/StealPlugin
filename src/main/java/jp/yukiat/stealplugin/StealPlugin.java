package jp.yukiat.stealplugin;

import jp.yukiat.stealplugin.commands.NBT;
import jp.yukiat.stealplugin.config.Rares;
import jp.yukiat.stealplugin.config.SkinContainer;
import jp.yukiat.stealplugin.config.SpecialConfig;
import jp.yukiat.stealplugin.timers.HealTimer;
import jp.yukiat.stealplugin.timers.Timer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

public final class StealPlugin extends JavaPlugin
{
    public static FileConfiguration config;
    private static StealPlugin plugin;
    public final ArrayList<UUID> stealed = new ArrayList<>();
    public final ArrayList<UUID> stealing = new ArrayList<>();

    public static StealPlugin getPlugin()
    {
        return plugin;
    }

    @Override
    @SuppressWarnings("all")
    public void onEnable()
    {
        plugin = this;
        Bukkit.getPluginCommand("nbt").setExecutor(new NBT());
        Bukkit.getPluginManager().registerEvents(new Events(), this);

        saveDefaultConfig();
        config = getConfig();

        SpecialConfig.loadConfig();
        SkinContainer.loadSkin();
        Rares.loadRare();

        long heal = config.getLong("heal");
        if (heal == 0)
            heal = 1;
        heal = heal * 20;
        new HealTimer().runTaskTimer(this, 0, heal);
        new Timer().runTaskTimer(this, 0, config.getLong("effect"));
        Bukkit.getOnlinePlayers().forEach(player -> stealed.add(player.getUniqueId()));
    }

    @Override
    public void onDisable()
    {
    }

}
