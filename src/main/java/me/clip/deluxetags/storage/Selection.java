package me.clip.deluxetags.storage;

/** A database snapshot. A null identifier means no saved preference, not explicit no-tag. */
public final class Selection {
    public static final String NO_TAG = "__deluxetags_no_tag__";
    public static final Selection ABSENT = new Selection(null, 0);
    private final String identifier;
    private final long revision;

    public Selection(String identifier, long revision) {
        this.identifier = identifier;
        this.revision = revision;
    }

    public String getIdentifier() { return identifier; }
    public long getRevision() { return revision; }
}
