package jp.yukiat.stealplugin.config;

import jp.yukiat.stealplugin.enums.ArmorType;
import jp.yukiat.stealplugin.enums.MaterialType;

import java.util.ArrayList;

public class SpecialCore
{
    public String name;
    public String color;
    public String itemName;
    public EffectCore effect;
    public ArrayList<Item> items;
    public MaterialType material = MaterialType.LEATHER;

    public static class Item
    {
        public ArmorType type;
        public String name;
        public String color;
        public MaterialType material;
        public EffectCore effect;
    }

}
