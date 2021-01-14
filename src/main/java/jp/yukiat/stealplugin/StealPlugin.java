package jp.yukiat.stealplugin;

import jp.yukiat.stealplugin.commands.*;
import jp.yukiat.stealplugin.config.*;
import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.plugin.java.*;

import java.util.*;

public final class StealPlugin extends JavaPlugin
{
    public static FileConfiguration config;
    private static StealPlugin plugin;
    public final ArrayList<UUID> stealed = new ArrayList<>();

    public static StealPlugin getPlugin()
    {
        return plugin;
    }

    @Override
    @SuppressWarnings("all")
    public void onEnable()
    {
        plugin = this;
        Bukkit.getPluginCommand("change").setExecutor(new ChangeSkin());
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
    }

    @Override
    public void onDisable()
    {
    }

}
