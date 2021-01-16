package jp.yukiat.stealplugin.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jp.yukiat.stealplugin.StealPlugin;

import java.util.ArrayList;

public class SkinContainer
{
    private static ArrayList<Skin> skins = new ArrayList<>();

    @SuppressWarnings("all")
    public static void loadSkin()
    {
        skins = (ArrayList<Skin>) StealPlugin.config.getObject("skins", ArrayList.class);
        skins = new Gson().fromJson(new Gson().toJson(skins), new TypeToken<ArrayList<Skin>>()
        {
        }.getType());
    }

    public static boolean contains(int order)
    {
        return order < skins.size();
    }

    public static Skin getSkinByOrder(int order)
    {
        // そのオーダーレベルのスキンが存在していたら返す
        if (order < skins.size())
            return skins.get(order);
            // 存在していなければオーダー0のスキンを返す
        else if (skins.size() > 1)
            return skins.get(0);
            // それでも存在しなければnullを返す
        else
            return null;
    }

}
