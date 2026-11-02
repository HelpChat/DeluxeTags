package me.clip.deluxetags.placeholders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.utils.StringUtils;
import org.bukkit.OfflinePlayer;
import org.junit.Test;

public class TagPlaceholdersTest {
    @Test
    public void reservesOutputSuffixesForPlayerTagsRatherThanIdentifierLookups() {
        TagPlaceholders placeholders = new TagPlaceholders(null);
        // Identifier lookups would access the plugin even with a null player.
        assertEquals("", placeholders.onRequest(null, "tag"));
        assertEquals("", placeholders.onRequest(null, "tag_legacy"));
        assertEquals("", placeholders.onRequest(null, "tag_mini"));
        assertEquals("", placeholders.onRequest(null, "tag_stripped"));
        assertEquals("", placeholders.onRequest(null, "lore"));
    }

    @Test
    public void stopsLoreRecursionThroughDescriptions() {
        TagPlaceholders placeholders = new TagPlaceholders(null);
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "Description: %deluxetags_lore%") {
            @Override
            public String getDescription(OfflinePlayer player) {
                // Model PlaceholderAPI routing the nested lore token back to this expansion.
                return getDescription().replace("%deluxetags_lore%", placeholders.expandLore(() -> {
                    throw new AssertionError("Recursive lore must not be evaluated");
                }));
            }
        };
        tag.setLore(Collections.singletonList("%deluxetags_description%"));

        assertEquals("Description: ", placeholders.expandLore(() ->
            StringUtils.replaceLore("%deluxetags_lore%", tag, Collections.emptyList())
                .replace("%deluxetags_description%", tag.getDescription(null))));
    }

    @Test
    public void stopsLoreRecursionThroughDisplayTags() {
        TagPlaceholders placeholders = new TagPlaceholders(null);
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP] %deluxetags_lore%", "Description") {
            @Override
            public String getDisplayTag(OfflinePlayer player) {
                return getDisplayTag().replace("%deluxetags_lore%", placeholders.expandLore(() -> {
                    throw new AssertionError("Recursive lore must not be evaluated");
                }));
            }
        };
        tag.setLore(Collections.singletonList("%deluxetags_tag%"));

        assertEquals("[VIP] ", placeholders.expandLore(() ->
            StringUtils.replaceLore("%deluxetags_lore%", tag, Collections.emptyList())
                .replace("%deluxetags_tag%", tag.getDisplayTag(null))));
    }

    @Test
    public void expandsIndependentRequestsAfterBlockingRecursion() {
        TagPlaceholders placeholders = new TagPlaceholders(null);

        assertEquals("Outer ", placeholders.expandLore(() ->
            "Outer " + placeholders.expandLore(() -> "Recursive")));
        assertEquals("Next request", placeholders.expandLore(() -> "Next request"));
    }

    @Test
    public void clearsTheLoreGuardWhenExpansionFails() {
        TagPlaceholders placeholders = new TagPlaceholders(null);

        assertThrows(IllegalStateException.class, () -> placeholders.expandLore(() -> {
            throw new IllegalStateException("Placeholder failed");
        }));
        assertEquals("Next request", placeholders.expandLore(() -> "Next request"));
    }

    @Test
    public void independentThreadsCanExpandLoreAtTheSameTime() throws Exception {
        TagPlaceholders placeholders = new TagPlaceholders(null);
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try {
            assertEquals("Other thread", placeholders.expandLore(() -> {
                try {
                    return worker.submit(() -> placeholders.expandLore(() -> "Other thread"))
                        .get(5, TimeUnit.SECONDS);
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
            }));
        } finally {
            worker.shutdownNow();
        }
    }
}
