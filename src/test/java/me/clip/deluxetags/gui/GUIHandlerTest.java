package me.clip.deluxetags.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GUIHandlerTest {

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
