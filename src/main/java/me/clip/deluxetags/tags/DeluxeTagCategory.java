package me.clip.deluxetags.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class DeluxeTagCategory {

    public static final String ALL_IDENTIFIER = "all";
    public static final String GENERAL_IDENTIFIER = "general";

    private final String identifier;
    private final int order;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final String guiName;
    private final boolean allCategory;

    public DeluxeTagCategory(
            @NotNull final String identifier,
            final int order,
            @NotNull final Material material,
            @NotNull final String name,
            @NotNull final List<String> lore,
            @NotNull final String guiName,
            final boolean allCategory) {
        this.identifier = identifier;
        this.order = order;
        this.material = material;
        this.name = name;
        this.lore = Collections.unmodifiableList(new ArrayList<>(lore));
        this.guiName = guiName;
        this.allCategory = allCategory;
    }

    public @NotNull String getIdentifier() {
        return identifier;
    }

    public int getOrder() {
        return order;
    }

    public @NotNull Material getMaterial() {
        return material;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<String> getLore() {
        return lore;
    }

    public @NotNull String getGuiName() {
        return guiName;
    }

    public boolean isAllCategory() {
        return allCategory;
    }
}
