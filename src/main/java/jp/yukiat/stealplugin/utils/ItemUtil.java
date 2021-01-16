package jp.yukiat.stealplugin.utils;

import com.google.common.util.concurrent.*;
import com.google.gson.*;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.*;
import org.bukkit.craftbukkit.v1_15_R1.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.*;
import java.util.*;

public class ItemUtil
{
    public static ItemStack cantEnderChest(ItemStack b)
    {
        return addMetaData(b, "enderChest", "false");
    }

    public static ItemStack unDisplayName(ItemStack b, String append)
    {
        ItemStack copy = b.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta.getDisplayName() == null || meta.getDisplayName().equals(""))
            meta.setDisplayName(append);
        else
            meta.setDisplayName(append + meta.getDisplayName());
        copy.setItemMeta(meta);
        return copy;
    }

    public static ItemStack setDisplayName(ItemStack b, String name)
    {
        ItemStack copy = b.clone();
        ItemMeta meta = copy.getItemMeta();
        meta.setDisplayName(name);
        copy.setItemMeta(meta);
        return copy;
    }

    public static ItemStack removeAttribute(ItemStack b)
    {
        ItemStack copy = b.clone();
        ItemMeta meta = copy.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        copy.setItemMeta(meta);
        return b;
    }

    public static ItemStack lore(ItemStack b, List<String> t)
    {
        ItemMeta meta = b.getItemMeta();
        if (b.getType() == Material.AIR)
            return b;
        meta.setLore(t);
        ItemStack stack = b.clone();
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack quickLore(ItemStack b, String t)
    {
        ItemMeta meta = b.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(t);
        lore.add("");
        if (b.getType() == Material.AIR)
            return b;
        if (meta.hasLore())
            lore.addAll(meta.getLore());
        meta.setLore(lore);
        ItemStack stack = b.clone();
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack removeMetadata(ItemStack stack, String name)
    {
        net.minecraft.server.v1_15_R1.ItemStack nmStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tagCompound = nmStack.getTag() != null ? nmStack.getTag(): new NBTTagCompound();
        tagCompound.remove(name);
        nmStack.setTag(tagCompound);
        return CraftItemStack.asCraftMirror(nmStack);
    }

    public static boolean hasMetadata(ItemStack stack, String name)
    {
        net.minecraft.server.v1_15_R1.ItemStack nmStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tagCompound = nmStack.getTag() != null ? nmStack.getTag(): new NBTTagCompound();
        return tagCompound.getString(name) != null && !tagCompound.getString(name).equals("");
    }

    public static String getMetadata(ItemStack stack, String name)
    {
        net.minecraft.server.v1_15_R1.ItemStack nmStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tagCompound = nmStack.getTag() != null ? nmStack.getTag(): new NBTTagCompound();
        return tagCompound.getString(name);
    }

    public static HashMap<String, String> getMetadataList(ItemStack stack)
    {
        net.minecraft.server.v1_15_R1.ItemStack nmStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tagCompound = nmStack.getTag() != null ? nmStack.getTag(): new NBTTagCompound();

        try
        {
            Field field = NBTTagCompound.class.getDeclaredField("map");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Object2ObjectOpenHashMap<String, NBTBase> map = (Object2ObjectOpenHashMap<String, NBTBase>) field.get(tagCompound);

            HashMap<String, String> result = new HashMap<>();
            map.forEach((s, nbtBase) -> {
                try
                {
                    new Gson().fromJson(nbtBase.toString(), Object.class);
                    result.put(s, new GsonBuilder().serializeNulls().setPrettyPrinting().create()
                            .toJson(new Gson().fromJson(nbtBase.toString(), Object.class)));
                }
                catch (Exception ignored)
                {
                    result.put(s, nbtBase.toString());
                }
            });
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    public static ItemStack addMetaData(ItemStack stack, String key, String value)
    {
        net.minecraft.server.v1_15_R1.ItemStack nmStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tagCompound = nmStack.getTag() != null ? nmStack.getTag(): new NBTTagCompound();
        tagCompound.setString(key, value);
        nmStack.setTag(tagCompound);
        return CraftItemStack.asCraftMirror(nmStack);
    }

    public static ItemStack noDrop(ItemStack b)
    {
        return ItemUtil.addMetaData(b, "noDrop", "1b");
    }

    public static ItemStack setUnbreakable(ItemStack b)
    {
        if (b == null || b.getType() == Material.AIR)
            return b;
        ItemMeta meta = b.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        ItemStack stack = b.clone();
        stack.setItemMeta(meta);
        return stack;
    }

    @SuppressWarnings("unchecked")
    public static double getDamage(ItemStack stack)
    {
        net.minecraft.server.v1_15_R1.ItemStack craftItem = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound compound = craftItem.hasTag() ? craftItem.getTag(): new NBTTagCompound();
        if (compound == null)
            return 0;
        NBTTagList lst = ((NBTTagList) compound.get("AttributeModifiers"));
        if (lst == null)
            return 0;
        List<NBTBase> list;
        try
        {
            Field field = NBTTagList.class.getDeclaredField("list");
            field.setAccessible(true);
            list = (List<NBTBase>) field.get(lst);
        }
        catch (Exception ignored)
        {
            return 0;
        }

        AtomicDouble atomicDouble = new AtomicDouble(0);

        list.stream()
                .parallel()
                .forEach(nbtBase -> {
                    if (!(nbtBase instanceof NBTTagCompound))
                        return;
                    NBTTagCompound c = (NBTTagCompound) nbtBase;
                    String n = c.getString("AttributeName");
                    if (!n.equals("generic.attackDamage"))
                        return;
                    atomicDouble.set(c.getDouble("Amount"));
                });

        return atomicDouble.get();
    }

    public static ItemStack addGlow(ItemStack item)
    {
        net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        if (!nmsStack.hasTag())
        {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }

        if (tag == null)
            tag = nmsStack.getTag();

        NBTTagList enchant = new NBTTagList();
        Objects.requireNonNull(tag).set("ench", enchant);
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
    }
}
