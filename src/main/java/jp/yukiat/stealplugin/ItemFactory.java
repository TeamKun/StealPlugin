package jp.yukiat.stealplugin;

import jp.yukiat.stealplugin.config.*;
import jp.yukiat.stealplugin.enums.*;
import jp.yukiat.stealplugin.utils.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;
import java.util.concurrent.atomic.*;

public class ItemFactory
{

    public static ItemStack getBaseItem(ArmorType type, MaterialType material)
    {
        switch (material)
        {
            case LEATHER:
                switch (type)
                {
                    case HELMET:
                        return new ItemStack(Material.LEATHER_HELMET);
                    case CHESTPLATE:
                        return new ItemStack(Material.LEATHER_CHESTPLATE);
                    case LEGGINGS:
                        return new ItemStack(Material.LEATHER_LEGGINGS);
                    case BOOTS:
                        return new ItemStack(Material.LEATHER_BOOTS);
                }
            case GOLD:
                switch (type)
                {
                    case HELMET:
                        return new ItemStack(Material.GOLDEN_HELMET);
                    case CHESTPLATE:
                        return new ItemStack(Material.GOLDEN_CHESTPLATE);
                    case LEGGINGS:
                        return new ItemStack(Material.GOLDEN_LEGGINGS);
                    case BOOTS:
                        return new ItemStack(Material.GOLDEN_BOOTS);
                }
            case CHAIN:
                switch (type)
                {
                    case HELMET:
                        return new ItemStack(Material.CHAINMAIL_HELMET);
                    case CHESTPLATE:
                        return new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                    case LEGGINGS:
                        return new ItemStack(Material.CHAINMAIL_LEGGINGS);
                    case BOOTS:
                        return new ItemStack(Material.CHAINMAIL_BOOTS);
                }
            case DIAMOND:
                switch (type)
                {
                    case HELMET:
                        return new ItemStack(Material.LEATHER_HELMET);
                    case CHESTPLATE:
                        return new ItemStack(Material.LEATHER_CHESTPLATE);
                    case LEGGINGS:
                        return new ItemStack(Material.LEATHER_LEGGINGS);
                    case BOOTS:
                        return new ItemStack(Material.LEATHER_BOOTS);
                }

        }

        return new ItemStack(Material.GRASS);
    }

    public static ItemStack getThiefItem(Player target, ArmorType type, MaterialType material)
    {
        if (!SpecialConfig.containsSpecial(target.getName()))
            return getRandomItemStack(type, material);
        SpecialCore core = SpecialConfig.getSpecial(target.getName());
        if (core == null)
            return getRandomItemStack(type, material);

        AtomicReference<MaterialType> realMaterial = new AtomicReference<>(material);
        AtomicReference<Color> color = new AtomicReference<>(Color.fromRGB(
                new Random().nextInt(256),
                new Random().nextInt(256),
                new Random().nextInt(256)
        ));
        AtomicReference<String> name = new AtomicReference<>(RandomUtil.pickRandom("綺麗な", "かわいい", "ボーイッシュな", "清楚な", "小洒落た", "あの子の") +
                material.getDisplayName() +
                type.getDisplayName());

        if (core.material != null)
            realMaterial.set(core.material);

        core.items.forEach(item -> {
            if (item.type == null || item.type != type)
                return;


            if (item.color != null && item.color.length() == 7)
            {
                color.set(Color.fromRGB(
                        Integer.valueOf(item.color.substring(1, 3), 16),
                        Integer.valueOf(item.color.substring(3, 5), 16),
                        Integer.valueOf(item.color.substring(5, 7), 16)
                ));
            }

            if (item.material != null)
                realMaterial.set(item.material);

            if (item.name != null)
                name.set(item.name);


        });
        ItemStack baseItem = getBaseItem(type, realMaterial.get());
        ItemMeta meta = baseItem.getItemMeta();
        meta.setDisplayName(name.get());

        if (realMaterial.get() != MaterialType.LEATHER)
        {
            baseItem.setItemMeta(meta);
            return baseItem;
        }

        LeatherArmorMeta lam = (LeatherArmorMeta) meta;

        lam.setColor(color.get());

        baseItem.setItemMeta(meta);
        return baseItem;
    }

    public static ItemStack getRandomItemStack(ArmorType type, MaterialType material)
    {
        ItemStack baseItem = getBaseItem(type, material);

        ItemMeta meta = baseItem.getItemMeta();
        meta.setDisplayName(RandomUtil.pickRandom("綺麗な", "かわいい", "ボーイッシュな", "清楚な", "小洒落た", "あの子の") +
                material.getDisplayName() +
                type.getDisplayName());

        if (material != MaterialType.LEATHER)
        {
            baseItem.setItemMeta(meta);
            return baseItem;
        }

        LeatherArmorMeta lam = (LeatherArmorMeta) meta;

        lam.setColor(Color.fromRGB(
                new Random().nextInt(256),
                new Random().nextInt(256),
                new Random().nextInt(256)
        ));

        baseItem.setItemMeta(meta);
        return baseItem;
    }
}
