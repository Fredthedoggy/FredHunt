package me.fredthedoggy.fredhunt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ManhuntCommand implements CommandExecutor {

    private final FredHunt fredHunt;

    public ManhuntCommand(FredHunt fredHunt) {
        this.fredHunt = fredHunt;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        AtomicReference<Boolean> cancel = new AtomicReference<>(false);
        player.getInventory().forEach(itemStack -> {
            if (cancel.get()) return;
            if (itemStack == null) return;
            if (!itemStack.getType().equals(Material.COMPASS)) return;
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) return;
            String itemData = itemMeta.getPersistentDataContainer().get(fredHunt.key, PersistentDataType.STRING);
            if (itemData == null) return;
            cancel.set(true);
            itemStack.setAmount(0);
        });
        if (cancel.get()) return false;
        String name = "";
        UUID uuid = player.getUniqueId();
        if (args.length > 0) {
            name = args[0];
        }
        Player target = Bukkit.getPlayer(name);
        if (target != null) {
            uuid = target.getUniqueId();
        }
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        if (meta == null) return true;
        meta.setDisplayName("§aTracker Compass");
        meta.getPersistentDataContainer().set(fredHunt.key, PersistentDataType.STRING, uuid.toString());
        compass.setItemMeta(meta);
        player.getInventory().addItem(compass);
        return true;
    }
}
