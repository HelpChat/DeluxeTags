package me.clip.deluxetags.gui;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.clip.deluxetags.tags.DeluxeTag;
import org.junit.Test;

public class GUIHandlerTest {

    @Test
    public void appendsDefaultLoreWithoutChangingTheDescription() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description\nSecond line");
        List<String> defaultLore = Collections.singletonList("%deluxetags_available%");

        assertEquals(Arrays.asList("VIP description", "Second line", "%deluxetags_available%"),
            GUIHandler.createTagLore(tag, defaultLore));
        assertEquals("VIP description\nSecond line", tag.getDescription());
        assertEquals(Collections.singletonList("%deluxetags_available%"), defaultLore);
    }

    @Test
    public void tagLoreOverridesDefaultLore() {
        DeluxeTag tag = new DeluxeTag(1, "MVP", "[MVP]", "MVP description");
        tag.setLore(Arrays.asList("Custom GUI information", "%deluxetags_available%"));

        assertEquals(Arrays.asList("MVP description", "Custom GUI information", "%deluxetags_available%"),
            GUIHandler.createTagLore(tag, Collections.singletonList("Default GUI information")));
        assertEquals("MVP description", tag.getDescription());
    }

    @Test
    public void emptyTagLoreDisablesTheDefault() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description");
        tag.setLore(Collections.emptyList());

        assertEquals(Collections.singletonList("VIP description"),
            GUIHandler.createTagLore(tag, Collections.singletonList("%deluxetags_available%")));
    }

    @Test
    public void clearingTagLoreRestoresInheritance() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description");
        tag.setLore(Collections.emptyList());
        tag.setLore(null);

        assertEquals(Arrays.asList("VIP description", "%deluxetags_available%"),
            GUIHandler.createTagLore(tag, Collections.singletonList("%deluxetags_available%")));
    }

    @Test
    public void supportsAnEmptyDefaultAndPreservesDescriptionLineBreaks() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "VIP description\r\n\rExtra detail\n");

        assertEquals(Arrays.asList("VIP description", "", "Extra detail", ""),
            GUIHandler.createTagLore(tag, Collections.emptyList()));
    }

    @Test
    public void emptyDescriptionDoesNotAddABlankLineBeforeLore() {
        DeluxeTag tag = new DeluxeTag(1, "VIP", "[VIP]", "");

        assertEquals(Collections.singletonList("%deluxetags_available%"),
            GUIHandler.createTagLore(tag, Collections.singletonList("%deluxetags_available%")));
        assertEquals(Collections.emptyList(), GUIHandler.createTagLore(tag, Collections.emptyList()));
    }

    @Test
    public void replacesExplicitPagePlaceholders() {
        String line = "%previous_page% {previous_page} %current_page% {current_page} %next_page% {next_page}";

        assertEquals("2 2 3 3 4 4", GUIHandler.replacePageNumbers(line, 3, true));
    }

    @Test
    public void clearsUnavailableAdjacentPagePlaceholders() {
        String line = "%previous_page% {previous_page} %next_page% {next_page}";

        assertEquals("   ", GUIHandler.replacePageNumbers(line, 1, false));
    }

    @Test
    public void replacesLegacyPagePlaceholderWithNavigationTarget() {
        assertEquals("Next page: 4", GUIHandler.replacePageNumbers("Next page: %page%", 3, true, 4));
        assertEquals("Previous page: 2", GUIHandler.replacePageNumbers("Previous page: {page}", 3, true, 2));
    }

    @Test
    public void leavesLegacyPagePlaceholderAloneWithoutNavigationTarget() {
        assertEquals("Current page? %page%", GUIHandler.replacePageNumbers("Current page? %page%", 3, true));
    }
}
