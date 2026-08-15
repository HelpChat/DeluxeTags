package me.clip.deluxetags.listeners;

import me.clip.deluxetags.DeluxeTags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Removes transient player state in the player's own Folia region. */
public final class QuitListener implements Listener {

  private final DeluxeTags plugin;

  public QuitListener(DeluxeTags plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(PlayerQuitEvent event) {
    plugin.getTagsHandler().removeActiveTagFromPlayer(event.getPlayer().getUniqueId());
  }
}
