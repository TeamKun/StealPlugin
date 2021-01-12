package jp.yukiat.stealplugin.config;

import com.google.gson.*;
import com.google.gson.reflect.*;
import jp.yukiat.stealplugin.*;

import java.util.*;
import java.util.stream.*;

public class SkinContainer
{
    private static final ArrayList<String> names = new ArrayList<>();
    private static ArrayList<Skin> skins = new ArrayList<>();

    @SuppressWarnings("all")
    public static void loadSkin()
    {
        skins = (ArrayList<Skin>) StealPlugin.config.getObject("skins", ArrayList.class);
        skins = new Gson().fromJson(new Gson().toJson(skins), new TypeToken<ArrayList<Skin>>()
        {
        }.getType());
        skins.forEach(skin -> names.add(skin.name));
    }

    public static boolean contains(String name)
    {
        return names.contains(name);
    }

    @SuppressWarnings("unchecked")
    public static Skin getSkinBy(String name, int index)
    {
        ArrayList<Skin> skins = (ArrayList<Skin>) SkinContainer.skins.clone();

        skins = skins.stream().filter(skin -> skin.name.equals(name)).collect(Collectors.toCollection(ArrayList::new));

        if (skins.size() > index)
            return skins.get(index);
        else if (skins.size() > 1)
            return skins.get(0);
        else
            return null;
    }

}
