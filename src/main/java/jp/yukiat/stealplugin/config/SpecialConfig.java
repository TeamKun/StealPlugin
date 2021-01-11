package jp.yukiat.stealplugin.config;

import com.google.gson.*;
import jp.yukiat.stealplugin.*;
import org.bukkit.*;

import java.util.*;
import java.util.concurrent.atomic.*;

public class SpecialConfig
{
    private static final ArrayList<String> applies = new ArrayList<>();
    private static ArrayList<SpecialCore> specials = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void loadConfig()
    {
        specials = (ArrayList<SpecialCore>) StealPlugin.config.get("specials");
        Bukkit.getLogger().info(new Gson().toJson(specials));
        specials.forEach(specialCore -> applies.add(specialCore.name));
    }

    public static ArrayList<SpecialCore> getSpecials()
    {
        return specials;
    }

    public static SpecialCore getSpecial(String name)
    {
        AtomicReference<SpecialCore> core = new AtomicReference<>();
        specials.forEach(specialCore -> {
            if (specialCore.name.equals(name))
                core.set(specialCore);
        });
        return core.get();
    }

    public static boolean contains(String name)
    {
        return applies.contains(name);
    }
}
