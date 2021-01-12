package jp.yukiat.stealplugin.utils;

import com.fasterxml.jackson.databind.*;
import com.mojang.authlib.*;
import com.mojang.authlib.properties.*;
import jp.yukiat.stealplugin.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.metadata.*;
import org.bukkit.scheduler.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class PlayerUtil
{
    public static ItemStack getSkullStack(StealPlugin.TextureData data)
    {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", data.getValue(), data.getSignature()));

        try
        {
            Method mtd = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(meta, profile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        skull.setItemMeta(meta);

        return skull;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getSkullStack(String p)
    {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(p);
        skull.setItemMeta(meta);

        return skull;
    }

    public static void setSkin(Player player, StealPlugin.TextureData data)
    {
        if (data.isEmpty())
            return;
        setSkin(player, data.getSignature(), data.getValue());
    }

    public static void setSkin(Player p, String value, String signature)
    {
        GameProfile gp = ((CraftPlayer) p).getProfile();
        gp.getProperties().clear();
        gp.getProperties().put("textures", new Property("textures", value, signature));

        p.getWorld().getPlayers().forEach(player -> {
            player.hidePlayer(StealPlugin.getPlugin(), p);
            player.showPlayer(StealPlugin.getPlugin(), p);
        });

    }

    public static void setDefaultSkinAsync(Player p)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                StealPlugin.TextureData data = getSkin(p.getUniqueId());
                Bukkit.getScheduler().runTask(StealPlugin.getPlugin(), () -> setSkin(p, data));

            }
        }.runTaskAsynchronously(StealPlugin.getPlugin());
    }

    public static StealPlugin.TextureData getSkin(UUID player)
    {
        String strPlayer = player.toString().replace("-", "");
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + strPlayer + "?unsigned=false";
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return StealPlugin.TextureData.empty();

            try (InputStream stream = connection.getInputStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream)))
            {
                StringBuilder o = new StringBuilder();
                String l;
                while ((l = bufferedReader.readLine()) != null)
                    o.append(l);
                JsonNode tree = new ObjectMapper().readTree(o.toString());
                for (JsonNode node : tree.get("properties"))
                    if (node.get("name").asText().equals("textures"))
                        return new StealPlugin.TextureData(node.get("value").asText(), node.get("signature").asText());
                return StealPlugin.TextureData.empty();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return StealPlugin.TextureData.empty();
        }
    }

    public static Optional<MetadataValue> getMetaData(Entity entity, String key)
    {
        AtomicReference<Optional<MetadataValue>> val = new AtomicReference<>(Optional.empty());
        entity.getMetadata(key)
                .parallelStream()
                .forEach(value -> {
                    if (value.getOwningPlugin().getName().equals(StealPlugin.getPlugin().getName()))
                        val.set(Optional.of(value));
                });
        return val.get();
    }

    public static void setMetaData(Entity entity, String key, Object value)
    {
        entity.setMetadata(key, new FixedMetadataValue(StealPlugin.getPlugin(), value));
    }

    public static boolean hasMetaData(Entity entity, String key)
    {
        return entity.hasMetadata(key);
    }

    public static void removeMetaData(Entity entity, String key)
    {
        entity.removeMetadata(key, StealPlugin.getPlugin());
    }
}
