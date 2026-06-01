package me.clip.deluxetags.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class MsgUtilsTest {

    @Before
    public void setUp() {
        MsgUtils.setPattern(false);
        MsgUtils.setMiniMessage(false);
    }

    @Test
    public void keepsLegacyFormattingWhenMiniMessageIsDisabled() {
        assertEquals("\u00A7aLegacy", MsgUtils.color("&aLegacy"));
    }

    @Test
    public void keepsUnknownAngleBracketPlaceholdersWhenMiniMessageIsEnabled() {
        MsgUtils.setMiniMessage(true);

        assertEquals("<%1$s> \u00A7cHi \u00A7lthere", MsgUtils.color("<%1$s> <red>Hi &lthere"));
    }

    @Test
    public void supportsPlainMiniMessageColorTags() {
        MsgUtils.setMiniMessage(true);

        assertEquals("\u00A7aOnline", MsgUtils.color("<green>Online"));
    }
}