package jp.yukiat.stealplugin.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import jp.yukiat.stealplugin.StealPlugin;
import jp.yukiat.stealplugin.config.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PlayerUtil
{
    /**
     * 誰が見てるのかわかるやつ。
     *
     * @param player 見られてるプレイヤー。
     * @return 見てるプレイヤー。
     */
    public static Player getLookingEntity(Player player)
    {
        for (Location location : player.getLineOfSight(null, 50).parallelStream().map(Block::getLocation)
                .collect(Collectors.toCollection(ArrayList::new)))
            for (Entity entity : player.getNearbyEntities(50, 50, 50))
            {
                if (entity instanceof Player)
                    if (isLooking((Player) entity, location) && entity.getType() == EntityType.PLAYER)
                        return (Player) entity;
            }


        return null;
    }

    /**
     * 今見てるかわかるやつ。
     *
     * @param player   見られてるプレイヤー。
     * @param location あと場所。
     * @return 見られてたらtrue。
     */
    public static boolean isLooking(Player player, Location location)
    {
        BlockIterator it = new BlockIterator(player, 50);

        while (it.hasNext())
        {
            final Block block = it.next();
            if (block.getX() == location.getBlockX() &&
                    block.getY() == location.getBlockY() &&
                    block.getZ() == location.getBlockZ())
                return true;
        }
        return false;
    }

    public static ItemStack getSkullStack(Skin skin)
    {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));

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

    public static void setSkin(Player player, Skin skin)
    {
        if (skin.isEmpty())
            return;
        setSkin(player, skin.getValue(), skin.getSignature());
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
                Skin skin = getSkin(p.getUniqueId());
                Bukkit.getScheduler().runTask(StealPlugin.getPlugin(), () -> setSkin(p, skin));
            }
        }.runTaskAsynchronously(StealPlugin.getPlugin());
    }

    public static Skin getSkin(UUID player) // getSkin やのに TextureData なんか返してんじゃねぇよ
    {
        String strPlayer = player.toString().replace("-", "");
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + strPlayer + "?unsigned=false";
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return Skin.getEmptyObject();

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
                        return new Skin(node.get("value").asText(), node.get("signature").asText());
                return Skin.getEmptyObject();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Skin.getEmptyObject();
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
