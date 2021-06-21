package me.fredthedoggy.fredhunt;

import me.mattstudios.gui.components.util.ItemBuilder;
import me.mattstudios.gui.guis.GuiItem;
import me.mattstudios.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

public class CompassListener implements Listener {

    private final FredHunt fredHunt;

    public CompassListener(FredHunt fredHunt) {
        this.fredHunt = fredHunt;
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
            int fullrows = (Bukkit.getOnlinePlayers().size() / 7) + 2;
            if (Bukkit.getOnlinePlayers().size() % 7 != 0) fullrows = fullrows + 1;
            boolean paged = false;
            int rows = fullrows;
            if (fullrows > 6) {
                rows = 6;
                paged = true;
            }
            PaginatedGui playerSelector = new PaginatedGui(rows, (rows - 2) * 7, Objects.requireNonNull(fredHunt.config.getString("Language.Gui.Title")));
            playerSelector.setDefaultClickAction(event1 -> event1.setCancelled(true));
            GuiItem lightFillerItem = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName("§r").asGuiItem();
            GuiItem fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName("§r").asGuiItem();
            playerSelector.getFiller().fillBorder(fillerItem);
            for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta playerheadMeta = (SkullMeta) playerhead.getItemMeta();
                if (playerheadMeta == null) continue;
                playerheadMeta.setOwningPlayer(loopPlayer);
                playerhead.setItemMeta(playerheadMeta);
                UUID uuid = loopPlayer.getUniqueId();
                String name = loopPlayer.getName();
                playerSelector.addItem(ItemBuilder.from(playerhead).setName("§6" + loopPlayer.getName()).setLore(Objects.requireNonNull(fredHunt.config.getString("Language.Gui.Click-To-Track"))).asGuiItem(event1 -> {
                    ItemStack itemStack2 = event.getPlayer().getInventory().getItemInMainHand();
                    if (!itemStack.equals(itemStack2)) return;
                    ItemStack compass = new ItemStack(Material.COMPASS, itemStack.getAmount());
                    ItemMeta meta = compass.getItemMeta();
                    if (meta == null) return;
                    meta.setDisplayName(fredHunt.config.getString("Language.Item-Name"));
                    meta.getPersistentDataContainer().set(fredHunt.track_uuid, PersistentDataType.STRING, uuid.toString());
                    compass.setItemMeta(meta);
                    event.getPlayer().getInventory().setItemInMainHand(compass);
                    Location targetLocation = loopPlayer.getLocation();
                    targetLocation.setY(1000);
                    CompassMeta compassMeta = (CompassMeta) meta;
                    compassMeta.setLodestone(targetLocation);
                    compassMeta.setLodestoneTracked(false);
                    compass.setItemMeta(compassMeta);
                    if (fredHunt.config.getBoolean("Messages.Send-Tracker-Track-Message")) {
                        player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Now-Tracking")).replace("[player]", name));
                    }
                    if (fredHunt.config.getBoolean("Messages.Send-Player-Tracked-Message")) {
                        loopPlayer.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Being-Tracked")).replace("[player]", player.getName()));
                    }
                    playerSelector.close(player);
                }));
            }
            int pagesize = (fullrows - 2);
            if (pagesize > 4) {
                pagesize = 4;
            }
            if (paged) {
                playerSelector.setItem(6, 3, ItemBuilder.from(Material.SPECTRAL_ARROW).setName(Objects.requireNonNull(fredHunt.config.getString("Language.Gui.Back"))).asGuiItem(event1 -> playerSelector.previous()));
                playerSelector.setItem(6, 7, ItemBuilder.from(Material.SPECTRAL_ARROW).setName(Objects.requireNonNull(fredHunt.config.getString("Language.Gui.Next"))).asGuiItem(event1 -> playerSelector.next()));
            }
            int extra = (pagesize * 7) - (Bukkit.getOnlinePlayers().size() % (pagesize * 7));
            for (int y = 0; y < extra; y++) {
                playerSelector.addItem(lightFillerItem);
            }
            playerSelector.open(player);
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

    @EventHandler
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
