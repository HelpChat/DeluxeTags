package me.clip.deluxetags.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

public class PendingSavedTagStoreTest {

  @Test
  public void storesPlayerSavesAndClearsForReplay() {
    YamlConfiguration config = new YamlConfiguration();
    PendingSavedTagStore store = new PendingSavedTagStore(config);
    UUID savedUuid = UUID.randomUUID();
    UUID clearedUuid = UUID.randomUUID();

    store.record(new SavedTagEntry(savedUuid, "vip", 100L));
    store.record(new SavedTagEntry(clearedUuid, null, 200L));

    Map<UUID, SavedTagEntry> entries = store.loadAll();
    assertEquals("vip", entries.get(savedUuid).getTagIdentifier());
    assertEquals(100L, entries.get(savedUuid).getUpdatedAtEpochMillis());
    assertNull(entries.get(clearedUuid).getTagIdentifier());
    assertEquals(200L, entries.get(clearedUuid).getUpdatedAtEpochMillis());

    store.removeAll(Arrays.asList(savedUuid, clearedUuid));

    assertTrue(store.loadAll().isEmpty());
  }

  @Test
  public void storesLatestTagWideClearForReplay() {
    YamlConfiguration config = new YamlConfiguration();
    PendingSavedTagStore store = new PendingSavedTagStore(config);

    store.recordTagClear("vip.tag", 200L);
    store.recordTagClear("vip.tag", 100L);

    Map<String, PendingSavedTagStore.PendingTagClear> tagClears = store.loadTagClears();
    assertEquals(1, tagClears.size());
    PendingSavedTagStore.PendingTagClear clear = tagClears.values().iterator().next();
    assertEquals("vip.tag", clear.getTagIdentifier());
    assertEquals(200L, clear.getUpdatedAtEpochMillis());

    store.removeTagClears(Arrays.asList(clear.getKey()));

    assertTrue(store.loadTagClears().isEmpty());
    assertFalse(config.isSet("tag_clears"));
  }
}
