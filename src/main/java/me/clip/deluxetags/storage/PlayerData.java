package me.clip.deluxetags.storage;

public class PlayerData {

    private final String tagIdentifier;
    private final String data;

    public PlayerData(String tagIdentifier, String data) {
        this.tagIdentifier = tagIdentifier;
        this.data = data;
    }

    public String getTagIdentifier() {
        return tagIdentifier;
    }

    public String getData() {
        return data;
    }
}