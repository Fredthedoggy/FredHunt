package me.fredthedoggy.fredhunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;


public class Utils {
    public static ItemStack hasCompass(Player player, FredHunt fredHunt) {
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack == null) continue;
            if (!itemStack.getType().equals(Material.COMPASS)) continue;
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) continue;
            String itemData = itemMeta.getPersistentDataContainer().get(fredHunt.track_uuid, PersistentDataType.STRING);
            if (itemData == null) continue;
            return itemStack;
        }
        return null;
    }


    public static void giveTracker(Player player, UUID uuid, FredHunt fredHunt) {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(fredHunt.config.getString("Language.Item-Name"));
        meta.getPersistentDataContainer().set(fredHunt.track_uuid, PersistentDataType.STRING, uuid.toString());
        compass.setItemMeta(meta);
        Location targetLocation = player.getLocation();
        Player target = Bukkit.getPlayer(uuid);
        if (target != null) {
            targetLocation = target.getLocation();
        }
        targetLocation.setY(1000);
        CompassMeta compassMeta = (CompassMeta) meta;
        compassMeta.setLodestone(targetLocation);
        compassMeta.setLodestoneTracked(false);
        compass.setItemMeta(compassMeta);
        player.getInventory().addItem(compass);
    }
}
