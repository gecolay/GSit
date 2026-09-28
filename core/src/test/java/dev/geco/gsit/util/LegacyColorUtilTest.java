package dev.geco.gsit.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LegacyColorUtilTest {

    private final LegacyColorUtil legacyColorUtil = new LegacyColorUtil();

    @Test
    public void keepsTextWithoutCodes() {
        assertEquals("<gradient:#FFD27A:#FF8C1A>Plugin reloaded.</gradient>", legacyColorUtil.toMiniMessage("<gradient:#FFD27A:#FF8C1A>Plugin reloaded.</gradient>"));
        assertEquals("", legacyColorUtil.toMiniMessage(""));
        assertNull(legacyColorUtil.toMiniMessage(null));
    }

    @Test
    public void convertsAllColors() {
        String codes = "0123456789abcdef";
        String[] names = { "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white" };
        for(int i = 0; i < codes.length(); i++) {
            assertEquals("<" + names[i] + ">Text", legacyColorUtil.toMiniMessage("&" + codes.charAt(i) + "Text"));
            assertEquals("<" + names[i] + ">Text", legacyColorUtil.toMiniMessage("&" + Character.toUpperCase(codes.charAt(i)) + "Text"));
        }
    }

    @Test
    public void convertsAllFormats() {
        assertEquals("<obfuscated>Text", legacyColorUtil.toMiniMessage("&kText"));
        assertEquals("<bold>Text", legacyColorUtil.toMiniMessage("&lText"));
        assertEquals("<strikethrough>Text", legacyColorUtil.toMiniMessage("&mText"));
        assertEquals("<underlined>Text", legacyColorUtil.toMiniMessage("&nText"));
        assertEquals("<italic>Text", legacyColorUtil.toMiniMessage("&oText"));
        assertEquals("<bold><italic>Text", legacyColorUtil.toMiniMessage("&L&OText"));
    }

    @Test
    public void convertsSectionCodes() {
        assertEquals("<green><bold>Text", legacyColorUtil.toMiniMessage("§a§lText"));
    }

    @Test
    public void convertsReset() {
        assertEquals("<green><bold>Hi</bold></green>There", legacyColorUtil.toMiniMessage("&a&lHi&rThere"));
        assertEquals("Text", legacyColorUtil.toMiniMessage("&rText"));
    }

    @Test
    public void colorResetsFormats() {
        assertEquals("<green><bold>Hi</bold></green><red>There", legacyColorUtil.toMiniMessage("&a&lHi&cThere"));
        assertEquals("<bold>Hi</bold><#ffaa00>There", legacyColorUtil.toMiniMessage("&lHi&#FFAA00There"));
    }

    @Test
    public void convertsHexColors() {
        assertEquals("<#ffaa00>Text", legacyColorUtil.toMiniMessage("&#FFAA00Text"));
        assertEquals("<#ffaa00>Text", legacyColorUtil.toMiniMessage("&x&F&F&A&A&0&0Text"));
        assertEquals("<#ffaa00>Text", legacyColorUtil.toMiniMessage("§x§f§f§a§a§0§0Text"));
        assertEquals("<#ffaa00>Te</#ffaa00><#55ff55>xt", legacyColorUtil.toMiniMessage("&#ffaa00Te&#55FF55xt"));
    }

    @Test
    public void keepsInvalidCodes() {
        assertEquals("Fish & Chips", legacyColorUtil.toMiniMessage("Fish & Chips"));
        assertEquals("&zText", legacyColorUtil.toMiniMessage("&zText"));
        assertEquals("&#FFAAText", legacyColorUtil.toMiniMessage("&#FFAAText"));
        assertEquals("&xText", legacyColorUtil.toMiniMessage("&xText"));
        assertEquals("Text&", legacyColorUtil.toMiniMessage("Text&"));
    }

    @Test
    public void convertsMixedInput() {
        assertEquals("<gray>[</gray><gold>GSit</gold><gray>] <gradient:#FFD27A:#FF8C1A>Plugin</gradient> </gray><green>enabled", legacyColorUtil.toMiniMessage("&7[&6GSit&7] <gradient:#FFD27A:#FF8C1A>Plugin</gradient> &aenabled"));
        assertEquals("<white><lang:key.sneak/></white> <gold>to get up", legacyColorUtil.toMiniMessage("<white><lang:key.sneak/></white> &6to get up"));
    }

    @Test
    public void keepsPlaceholders() {
        assertEquals("<green>Link with</green><gold> %Link%</gold><green> successful!", legacyColorUtil.toMiniMessage("&aLink with&6 %Link%&a successful!"));
        assertEquals("[P]<red> Outdated server version! (</red><aqua>%Version%</aqua><red>)", legacyColorUtil.toMiniMessage("[P]&c Outdated server version! (&b%Version%&c)"));
    }

}
