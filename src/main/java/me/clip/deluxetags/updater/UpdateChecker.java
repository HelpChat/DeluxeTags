package me.clip.deluxetags.updater;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class UpdateChecker implements Listener {

  private final int RESOURCE_ID = 4390;
  private Plugin plugin;
  private String spigotVersion, pluginVersion;
  private boolean updateAvailable;

  public UpdateChecker(Plugin i) {
    plugin = i;
    pluginVersion = i.getDescription().getVersion();
  }

  public boolean hasUpdateAvailable() {
    return updateAvailable;
  }

  public String getSpigotVersion() {
    return spigotVersion;
  }

  public void fetch() {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        HttpsURLConnection con = (HttpsURLConnection) new URL(
            "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).openConnection();
        con.setRequestMethod("GET");
        spigotVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
      } catch (Exception ex) {
        plugin.getLogger().info("Failed to check for updates on spigot.");
        return;
      }

      if (spigotVersion == null || spigotVersion.isEmpty()) {
        return;
      }

      updateAvailable = spigotIsNewer();

      if (!updateAvailable) {
        return;
      }

      Bukkit.getScheduler().runTask(plugin, () -> {
        plugin.getLogger()
            .info("An update for DeluxeTags (v" + getSpigotVersion() + ") is available at:");
        plugin.getLogger()
            .info("https://www.spigotmc.org/resources/deluxetags." + RESOURCE_ID + "/");
        Bukkit.getPluginManager().registerEvents(this, plugin);
      });
    });
  }

  private boolean spigotIsNewer() {
      if (spigotVersion == null || spigotVersion.isEmpty()) {
          return false;
      }
    String plV = toReadable(pluginVersion);
    String spV = toReadable(spigotVersion);
    return plV.compareTo(spV) < 0;
  }

  private String toReadable(String version) {
    if (version.contains("-DEV-")) {
      version = version.split("-DEV-")[0];
    }
    return version.replaceAll("\\.", "");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent e) {
    if (e.getPlayer().hasPermission("deluxetags.updates")) {
      Msg.msg(e.getPlayer(),
          "&bAn update for &5&lDeluxeTags &e(&5&lDeluxeTags &fv" + getSpigotVersion()
              + "&e)"
          , "&bis available at &ehttps://www.spigotmc.org/resources/deluxetags." + RESOURCE_ID
              + "/");
    }
  }
}
