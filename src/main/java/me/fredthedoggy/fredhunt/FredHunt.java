package me.fredthedoggy.fredhunt;

import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

public final class FredHunt extends JavaPlugin {

    FileConfiguration config = this.getConfig();

    HashMap<UUID, Long> cooldowns = new HashMap<>();

    NamespacedKey track_uuid = new NamespacedKey(this, "track_uuid");

    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 11552);
        this.saveDefaultConfig();
        this.config.options().copyDefaults(true);
        this.getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        this.getCommand("tracker").setExecutor(new ManhuntCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

}
