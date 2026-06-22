package me.clip.deluxetags.storage;

import java.util.UUID;

public class SavedTagEntry {

  private final UUID uuid;
  private final String tagIdentifier;
  private final long updatedAtEpochMillis;

  public SavedTagEntry(UUID uuid, String tagIdentifier, long updatedAtEpochMillis) {
    this.uuid = uuid;
    this.tagIdentifier = normalizeTagIdentifier(tagIdentifier);
    this.updatedAtEpochMillis = updatedAtEpochMillis;
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getTagIdentifier() {
    return tagIdentifier;
  }

  public long getUpdatedAtEpochMillis() {
    return updatedAtEpochMillis;
  }

  public boolean hasTagIdentifier() {
    return tagIdentifier != null && !tagIdentifier.isEmpty();
  }

  private static String normalizeTagIdentifier(String tagIdentifier) {
    if (tagIdentifier == null) {
      return null;
    }

    String trimmed = tagIdentifier.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
