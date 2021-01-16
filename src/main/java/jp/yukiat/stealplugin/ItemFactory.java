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
        SpecialCore core = Rares.selectAsSpecial(type);
        if (core == null)
            core = SpecialConfig.getSpecial(target.getName());
        if (core == null)
            return getRandomItemStack(target.getName(), type, material);

        AtomicReference<MaterialType> realMaterial = new AtomicReference<>(material);
        AtomicReference<Color> color = new AtomicReference<>(Color.fromRGB(
                new Random().nextInt(256),
                new Random().nextInt(256),
                new Random().nextInt(256)
        ));
        AtomicReference<String> name = new AtomicReference<>(
                ChatColor.GOLD + target.getName() + ChatColor.AQUA + "の" + type.getDisplayName());
        AtomicReference<EffectCore> effect = new AtomicReference<>();

        if (core.material != null)
            realMaterial.set(core.material);
        if (core.color != null)
            color.set(Color.fromRGB(
                    Integer.valueOf(core.color.substring(1, 3), 16),
                    Integer.valueOf(core.color.substring(3, 5), 16),
                    Integer.valueOf(core.color.substring(5, 7), 16)
            ));
        if (core.itemName != null)
            name.set(core.itemName);
        if (core.effect != null)
            effect.set(core.effect);

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
                name.set(item.name
                        .replace("%%name%%", target.getName())
                        .replace("%%armor_type%%", type.getDisplayName())
                        .replace("%%material%%", material.getDisplayName()));

            if (item.effect != null)
                effect.set(item.effect);

        });
        ItemStack baseItem = getBaseItem(type, realMaterial.get()).clone();
        ItemMeta meta = baseItem.getItemMeta();
        meta.setDisplayName(name.get());

        if (realMaterial.get() != MaterialType.LEATHER)
        {
            baseItem.setItemMeta(meta);
            if (effect.get() != null)
                baseItem = addEffect(baseItem, effect.get());
            return baseItem;
        }

        LeatherArmorMeta lam = (LeatherArmorMeta) meta;

        lam.setColor(color.get());

        baseItem.setItemMeta(meta);
        if (effect.get() != null)
            baseItem = addEffect(baseItem, effect.get());
        return baseItem;
    }

    private static ItemStack addEffect(ItemStack ss, EffectCore core)
    {
        ItemStack stack = ss.clone();
        stack = ItemUtil.addMetaData(stack, "particle_name", core.particle.name());
        stack = ItemUtil.addMetaData(stack, "particle_count", Integer.toString(core.count));
        stack = ItemUtil.addMetaData(stack, "particle_offset_x", Double.toString(core.offsetX));
        stack = ItemUtil.addMetaData(stack, "particle_offset_y", Double.toString(core.offsetY));
        stack = ItemUtil.addMetaData(stack, "particle_offset_z", Double.toString(core.offsetZ));
        stack = ItemUtil.addMetaData(stack, "particle_extra", Double.toString(core.extra));
        return stack;
    }

    public static ItemStack getRandomItemStack(String playerName, ArmorType type, MaterialType material)
    {
        ItemStack baseItem = getBaseItem(type, material);

        ItemMeta meta = baseItem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + playerName + ChatColor.AQUA + "の" + type.getDisplayName());

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
