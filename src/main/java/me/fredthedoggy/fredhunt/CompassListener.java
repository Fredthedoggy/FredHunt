package me.fredthedoggy.fredhunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CompassListener implements Listener {

    private final FredHunt fredHunt;
    GuiManager guiManager = new GuiManager();


    public CompassListener(FredHunt fredHunt) {
        this.fredHunt = fredHunt;
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().getUID().equals(event.getTo().getWorld().getUID())) return;
        Map<UUID, Location> map = new HashMap<>();
        Map<UUID, Location> worldMap = fredHunt.lastSeen.putIfAbsent(event.getPlayer().getUniqueId(), map);
        if (worldMap == null) worldMap = map;
        worldMap.put(event.getFrom().getWorld().getUID(), event.getFrom().clone());
        worldMap.remove(event.getTo().getWorld().getUID());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        fredHunt.lastSeen.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        fredHunt.lastSeen.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (fredHunt.config.getBoolean("Join.Join-Compass")) {
            if (Utils.hasCompass(event.getPlayer(), fredHunt) == null) {
                String uuid = fredHunt.config.getString("Join.Join-Player");
                UUID track = event.getPlayer().getUniqueId();
                if (!"".equals(uuid) && uuid != null) {
                    track = UUID.fromString(uuid);
                }
                Utils.giveTracker(event.getPlayer(), track, fredHunt);
            }
        }
    }

    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) return;
        Player player = event.getPlayer();
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (!itemStack.getType().equals(Material.COMPASS)) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        String itemData = itemMeta.getPersistentDataContainer().get(fredHunt.track_uuid, PersistentDataType.STRING);
        if (itemData == null) return;
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (fredHunt.config.getBoolean("Gui.Cooldown")) {
                Long time = System.currentTimeMillis();
                if (fredHunt.cooldowns.containsKey(player.getUniqueId())) {
                    if (time < fredHunt.cooldowns.get(player.getUniqueId()) + fredHunt.config.getLong("Gui.Cooldown-time")) {
                        player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Cooldown-Message")).replace("[seconds]", String.valueOf((((fredHunt.cooldowns.get(player.getUniqueId()) - time + fredHunt.config.getLong("Gui.Cooldown-time")) / 1000) + 1))));
                        return;
                    }
                }
                fredHunt.cooldowns.put(player.getUniqueId(), time);
            }
            guiManager.openGui(fredHunt, player, itemStack, event);
            event.setCancelled(true); // Fix interfering with plugins that use compasses, like WorldEdit
        } else if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (!FredHunt.isValidUUID(itemData)) {
                player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Errors.Invalid-UUID")));
                return;
            }
            Player target = Bukkit.getPlayer(UUID.fromString(itemData));
            if (target == null) {
                player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Errors.Offline-Player")));
                return;
            }
            if (!target.getWorld().getUID().equals(player.getWorld().getUID())) {
                if (fredHunt.config.getBoolean("Tracker-Compass.Track-Portals")) {
                    Map<UUID, Location> lastLocationMap = fredHunt.lastSeen.get(target.getUniqueId());
                    if (lastLocationMap == null) {
                        player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Errors.Different-World")));
                        return;
                    }
                    Location targetLocation = lastLocationMap.get(player.getWorld().getUID());
                    if (targetLocation == null) {
                        player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Errors.Different-World")));
                        return;
                    }
                    targetLocation.setY(1000);
                    CompassMeta compassMeta = (CompassMeta) itemMeta;
                    compassMeta.setLodestone(targetLocation);
                    compassMeta.setLodestoneTracked(false);
                    itemStack.setItemMeta(compassMeta);
                    player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Portal-Tracking")).replace("[player]", target.getName()));
                    return;
                }
                player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Errors.Different-World")));
                return;
            }
            Location targetLocation = target.getLocation();
            targetLocation.setY(1000);
            CompassMeta compassMeta = (CompassMeta) itemMeta;
            compassMeta.setLodestone(targetLocation);
            compassMeta.setLodestoneTracked(false);
            itemStack.setItemMeta(compassMeta);
            player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Now-Tracking")).replace("[player]", target.getName()));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (!itemStack.getType().equals(Material.COMPASS)) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        String itemData = itemMeta.getPersistentDataContainer().get(fredHunt.track_uuid, PersistentDataType.STRING);
        if (itemData != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDie(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (ItemStack itemStack : event.getEntity().getInventory().getContents()) {
            if (itemStack == null || !itemStack.getType().equals(Material.COMPASS)) continue;
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) continue;
            String itemData = itemMeta.getPersistentDataContainer().get(fredHunt.track_uuid, PersistentDataType.STRING);
            if (itemData == null) continue;
            itemStack.setAmount(0);
            Bukkit.getScheduler().runTaskLater(fredHunt, () -> {
                ItemStack compass = new ItemStack(Material.COMPASS, 1);
                compass.setItemMeta(itemMeta);
                player.getInventory().addItem(compass);
            }, 5L);
            return;
        }
    }

}
