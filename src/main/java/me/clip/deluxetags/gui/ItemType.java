package me.clip.deluxetags.gui;

import com.cryptomorin.xseries.XMaterial;
import java.util.Collection;
import java.util.HashMap;
import org.bukkit.Material;

public enum ItemType {
    TAG_SELECT_ITEM(XMaterial.NAME_TAG.parseMaterial()),
    DIVIDER_ITEM(XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial()),
    HAS_TAG_ITEM(XMaterial.PLAYER_HEAD.parseMaterial()),
    NO_TAG_ITEM(XMaterial.PLAYER_HEAD.parseMaterial()),
    EXIT_ITEM(XMaterial.IRON_DOOR.parseMaterial()),
    NEXT_PAGE(XMaterial.PAPER.parseMaterial()),
    PREVIOUS_PAGE(XMaterial.PAPER.parseMaterial()),
    UNKNOWN(null);

    private static final HashMap<String, ItemType> CACHED = new HashMap<>();
    private final Material fallbackMaterial;

    public static ItemType get(String name) {
        return CACHED.getOrDefault(name, UNKNOWN);
    }

    public Material getFallbackMaterial() {
        return fallbackMaterial;
    }

    public static Collection<ItemType> getCached() {
        return CACHED.values();
    }

    static {
        for (ItemType type : values()) {
            CACHED.put(type.name(), type);
        }
    }

    ItemType(Material fallback) {
        fallbackMaterial = fallback;
    }
}
