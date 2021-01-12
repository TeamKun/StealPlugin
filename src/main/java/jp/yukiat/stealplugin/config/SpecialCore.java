package jp.yukiat.stealplugin.config;

import jp.yukiat.stealplugin.enums.*;

import java.util.*;

public class SpecialCore
{
    public String name;
    public ArrayList<Item> items;
    public MaterialType material = MaterialType.LEATHER;

    public static class Item
    {
        public ArmorType type;
        public String name;
        public String color;
        public MaterialType material;
    }

}
