package jp.yukiat.stealplugin.config;

import com.google.gson.*;
import jp.yukiat.stealplugin.*;
import org.bukkit.*;

import java.util.*;
import java.util.concurrent.atomic.*;

public class SpecialConfig
{
    private static ArrayList<SpecialCore> specials = new ArrayList<>();
    private static final ArrayList<String> names = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void loadConfig()
    {
        specials = (ArrayList<SpecialCore>) StealPlugin.config.getObject("specials", ArrayList.class);
        Bukkit.getLogger().info(new Gson().toJson(specials));
        ((ArrayList<SpecialCore>)specials).forEach(specialCore -> names.add(specialCore.name));
    }

    public static boolean containsSpecial(String name)
    {
        return names.contains(name);
    } // ばいばいー あんど おやすみー

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
}