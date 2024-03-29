package me.fredthedoggy.fredhunt;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public final class FredHunt extends JavaPlugin {

    FileConfiguration config = this.getConfig();

    Map<UUID, Long> cooldowns = new HashMap<>();
    Map<UUID, Map<UUID, Location>> lastSeen = new HashMap<>();

    NamespacedKey track_uuid = new NamespacedKey(this, "track_uuid");

    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    public boolean floodgate;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 11552);
        floodgate = Bukkit.getPluginManager().isPluginEnabled("floodgate");
        this.saveDefaultConfig();
        this.config.options().copyDefaults(true);
        this.saveConfig();
        this.getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        this.getCommand("tracker").setExecutor(new ManhuntCommand(this));
    }

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

}
