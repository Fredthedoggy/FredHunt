package me.fredthedoggy.fredhunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ManhuntCommand implements CommandExecutor {

    private final FredHunt fredHunt;

    public ManhuntCommand(FredHunt fredHunt) {
        this.fredHunt = fredHunt;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (fredHunt.config.getBoolean("Permissions.Require-Permission") && player.hasPermission(Objects.requireNonNull(fredHunt.config.getString("Permissions.Permission"), "Invalid Config. Try Restarting?"))) {
            player.sendMessage(Objects.requireNonNull(fredHunt.config.getString("Language.Missing-Permission")));
            return true;
        }
        ItemStack current = Utils.hasCompass(player, fredHunt);
        if (current != null) {
            current.setAmount(0);
            return true;
        }
        String name = "";
        UUID uuid = player.getUniqueId();
        if (args.length > 0) {
            name = args[0];
        }
        Player target = Bukkit.getPlayer(name);
        if (target != null) {
            uuid = target.getUniqueId();
        }
        Utils.giveTracker(player, uuid, fredHunt);
        return true;
    }
}
