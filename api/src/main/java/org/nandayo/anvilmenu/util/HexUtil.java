package org.nandayo.anvilmenu.util;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"deprecation","unused"})
public final class HexUtil {
    private static final Pattern COLORIZE_PATTERN = Pattern.compile(
            "<(#[0-9A-F]{6}|aqua|black|blue|bold|dark_(aqua|blue|gray|green|purple|red)|gray|gold|green|italic|light_purple|obfuscated|red|reset|strikethrough|underline|white|yellow)>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LEGACY_COLOR_CODE_PATTERN = Pattern.compile("§([0-9A-FK-OR])", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGACY_HEX_COLOR_PATTERN = Pattern.compile("§x(§[0-9A-F]){6}", Pattern.CASE_INSENSITIVE);


    @NotNull
    static public String colorize(String text) {
        if (text == null || text.isEmpty()) return "";

        final Matcher matcher = COLORIZE_PATTERN.matcher(text);
        while (matcher.find()) {
            try {
                String original = matcher.group();
                String hex = matcher.group(1); // -> #ffffff
                ChatColor chatColor = ChatColor.of(hex);

                if (chatColor != null) {
                    text = text.replace(original, chatColor.toString()); // toString() -> e.g. §a
                }
            } catch (IllegalArgumentException ignored) { }
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @NotNull
    public static String colorToMiniMessage(String text) {
        if (text == null || text.isEmpty()) return "";
        // Translate & to §
        text = ChatColor.translateAlternateColorCodes('&', text);

        // Don't translate <#RRGGBB> or <namedColor>

        // Translate §x§f§f§f§f§f§f to <#ffffff>, this should be handled before #legacyToMini()
        text = legacyHexToMiniMessage(text);
        // Translate §a, §b etc. to <green>, <aqua> etc.
        text = legacyToMiniMessage(text);

        return text;
    }

    /**
     * Translates Minecraft HEX style to MiniMessage HEX format<br>
     * §x§f§f§f§f§f§f -> <#ffffff>
     * @param text Text to translate
     * @return Result
     */
    @NotNull
    static public String legacyHexToMiniMessage(String text) {
        if (text == null || text.isEmpty()) return "";

        final Matcher matcher = LEGACY_HEX_COLOR_PATTERN.matcher(text);
        while(matcher.find()) {
            String original = matcher.group(); // -> §x§f§f§f§f§f§f
            String hex = original.replace("§","").substring(1); // -> xffffff, -> ffffff
            text = text.replace(original, "<#" + hex + ">"); // -> <#fffff>
        }
        return text;
    }

    /**
     * Translates Minecraft color to MiniMessage color format<br>
     * §f -> <white>
     * @param text Text to translate
     * @return Result
     */
    @NotNull
    static public String legacyToMiniMessage(String text) {
        if (text == null || text.isEmpty()) return "";

        Matcher matcher = LEGACY_COLOR_CODE_PATTERN.matcher(text);
        while(matcher.find()) {
            String original = matcher.group();
            char code = matcher.group(1).charAt(0); // -> f

            ChatColor chatColor = ChatColor.getByChar(code); // WHITE
            if(chatColor != null) {
                String name = chatColor.getName().equals("underline") ? "underlined" : chatColor.getName(); // white
                text = text.replace(original, "<" + name + ">"); // -> <white>
            }
        }
        return text;
    }
}
