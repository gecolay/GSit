package dev.geco.gsit.model;

public enum CrawlType {

    COMMAND("command"),
    CORRIDOR("corridor"),
    DOUBLE_SNEAK("double_sneak");

    private final String name;

    CrawlType(String name) {
        this.name = name;
    }

    public String getName() { return name; }

}