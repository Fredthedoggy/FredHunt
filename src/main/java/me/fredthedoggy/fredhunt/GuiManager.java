package me.fredthedoggy.fredhunt;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Objects;
import java.util.UUID;

public class GuiManager {
    public void openGui(FredHunt fredHunt, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        if (fredHunt.floodgate && FloodgateApi.getInstance().isFloodgateId(player.getUniqueId())) {
            SimpleForm.Builder formBuilder = SimpleForm.builder()
                    .title(Objects.requireNonNull(fredHunt.config.getString("Language.Gui.Title")))
                    .content("");
            for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                formBuilder = formBuilder.button(loopPlayer.getName(), FormImage.Type.URL, "https://crafatar.com/avatars/" + loopPlayer.getUniqueId());
            }

            formBuilder = formBuilder.responseHandler((form, responseData) -> {
                SimpleFormResponse response = form.parseResponse(responseData);
                if (!response.isCorrect()) {
                    return;
                }

                Player chosenBukkitPlayer = Bukkit.getPlayer(response.getClickedButton().getText());

                if (chosenBukkitPlayer == null) return;

                UUID chosenPlayer = chosenBukkitPlayer.getUniqueId();

                ItemStack itemStack2 = player.getInventory().getItemInMainHand();
                if (!itemStack.equals(itemStack2)) return;
                ItemStack compass = new ItemStack(Material.COMPASS, itemStack.getAmount());
                ItemMeta meta = compass.getItemMeta();
                if (meta == null) return;
                meta.setDisplayName(fredHunt.config.getString("Language.Item-Name"));
                meta.getPersistentDataContainer().set(fredHunt.track_uuid, PersistentDataType.STRING, chosenPlayer.toString());
                compass.setItemMeta(meta);
                player.getInventory().setItemInMainHand(compass);
                CompassMeta compassMeta = (CompassMeta) meta;
                compassMeta.setLodestoneTracked(false);
                compass.setItemMeta(compassMeta);
                if (fredHunt.config.getBoolean("Messages.Send-Tracker-Track-Message")) {
                    player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Now-Tracking")).replace("[player]", chosenBukkitPlayer.getName()));
                }
                if (fredHunt.config.getBoolean("Messages.Send-Player-Tracked-Message")) {
                    chosenBukkitPlayer.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Being-Tracked")).replace("[player]", player.getName()));
                }

            });
            FloodgateApi.getInstance().getPlayer(player.getUniqueId()).sendForm(formBuilder.build());
        } else {
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
        }
    }
}
