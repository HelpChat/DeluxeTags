package me.clip.deluxetags.updater;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import javax.net.ssl.HttpsURLConnection;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class UpdateChecker implements Listener {

  private static final String MODRINTH_URL =
      "https://api.modrinth.com/v2/project/wtpLgugo/version?include_changelog=false";
  private static final String MODRINTH_PAGE = "https://modrinth.com/plugin/deluxetags";

  private final Plugin plugin;
  private final String pluginVersion;
  private String modrinthVersion;
  private boolean updateAvailable;

  public UpdateChecker(Plugin instance) {
    plugin = instance;
    pluginVersion = instance.getDescription().getVersion();
  }

  public boolean hasUpdateAvailable() {
    return updateAvailable;
  }

  public String getModrinthVersion() {
    return modrinthVersion;
  }

  public void fetch() {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        HttpsURLConnection con = (HttpsURLConnection) new URL(MODRINTH_URL).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "DeluxeTags/" + pluginVersion);

        JsonElement json = new Gson().fromJson(
            new BufferedReader(new InputStreamReader(con.getInputStream())),
            JsonElement.class
        );

        modrinthVersion = json.getAsJsonArray()
            .get(0)
            .getAsJsonObject()
            .get("version_number")
            .getAsString();
      } catch (Exception ex) {
        plugin.getLogger().info("Failed to check for updates on Modrinth.");
        return;
      }

      if (modrinthVersion == null || modrinthVersion.isEmpty()) {
        return;
      }

      updateAvailable = modrinthIsNewer();

      Bukkit.getScheduler().runTask(plugin, () -> {
        logUpdateStatus();

        if (updateAvailable) {
          Bukkit.getPluginManager().registerEvents(this, plugin);
        }
      });
    });
  }

  private void logUpdateStatus() {
    plugin.getLogger().info("----------------------------");
    plugin.getLogger().info("     DeluxeTags Updater");
    plugin.getLogger().info(" ");

    if (updateAvailable) {
      plugin.getLogger().info("An update for DeluxeTags has been found!");
      plugin.getLogger().info("DeluxeTags " + getModrinthVersion());
      plugin.getLogger().info("You are running " + pluginVersion);
      plugin.getLogger().info(" ");
      plugin.getLogger().info("Download at " + MODRINTH_PAGE);
    } else {
      plugin.getLogger().info("You are running " + pluginVersion);
      plugin.getLogger().info("The latest version");
      plugin.getLogger().info("of DeluxeTags!");
    }

    plugin.getLogger().info(" ");
    plugin.getLogger().info("----------------------------");
  }

  private boolean modrinthIsNewer() {
    if (modrinthVersion == null || modrinthVersion.isEmpty()) {
      return false;
    }

    int[] local = toReadable(pluginVersion);
    int[] remote = toReadable(modrinthVersion);

    int max = Math.max(local.length, remote.length);
    for (int i = 0; i < max; i++) {
      int localPart = i < local.length ? local[i] : 0;
      int remotePart = i < remote.length ? remote[i] : 0;

      if (localPart < remotePart) {
        return true;
      }

      if (localPart > remotePart) {
        return false;
      }
    }

    return false;
  }

  private int[] toReadable(String version) {
    if (version.contains("-DEV")) {
      version = version.split("-DEV")[0];
    }

    return Arrays.stream(version.split("\\."))
        .map(part -> part.replaceAll("[^0-9]", ""))
        .filter(part -> !part.isEmpty())
        .mapToInt(Integer::parseInt)
        .toArray();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent e) {
    if (e.getPlayer().hasPermission("deluxetags.updates")) {
      Msg.msg(e.getPlayer(),
          "&bAn update for &5&lDeluxeTags &e(&5&lDeluxeTags &fv" + getModrinthVersion() + "&e)",
          "&bis available at &e" + MODRINTH_PAGE);
    }
  }
}