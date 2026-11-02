package me.clip.deluxetags.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import me.clip.deluxetags.tags.DeluxeTag;
import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void lorePlaceholderUsesTheDefaultWithoutIncludingTheDescription() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description");

        assertEquals("Default GUI information\n%deluxetags_available%",
            StringUtils.replaceLore("%deluxetags_lore%", tag,
                Arrays.asList("Default GUI information", "%deluxetags_available%")));
        assertEquals("VIP description", tag.getDescription());
    }

    @Test
    public void lorePlaceholderUsesTheTagOverrideAndSupportsBothPlaceholderStyles() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description");
        tag.setLore(Arrays.asList("Custom GUI information", "%deluxetags_available%"));

        assertEquals("Custom GUI information\n%deluxetags_available% / Custom GUI information\n%deluxetags_available%",
            StringUtils.replaceLore("%deluxetags_lore% / {deluxetags_lore}", tag,
                Collections.singletonList("Default GUI information")));
    }

    @Test
    public void lorePlaceholderRespectsEmptyTagAndDefaultLore() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description");
        assertEquals("", StringUtils.replaceLore("%deluxetags_lore%", tag, Collections.emptyList()));
        tag.setLore(Collections.emptyList());

        assertEquals("", StringUtils.replaceLore("%deluxetags_lore%", tag,
            Collections.singletonList("Default GUI information")));
    }

    @Test
    public void lorePlaceholderIsEmptyWhenThereIsNoActiveTag() {
        assertEquals("", StringUtils.replaceLore("%deluxetags_lore%", null,
            Collections.singletonList("Default GUI information")));
    }

    @Test
    public void loreCannotRecursivelyExpandItself() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description");
        tag.setLore(Arrays.asList("Before %deluxetags_lore% after", "{deluxetags_lore}"));

        assertEquals("Before  after\n", StringUtils.replaceLore("%deluxetags_lore%", tag,
            Collections.singletonList("Default GUI information")));
    }
}
