package org.nandayo.anvilmenu.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

/**
 * @since 0.1
 */
@SuppressWarnings("unused")
public final class Util {

    static private final String INTERNAL_PREFIX = "[AnvilMenu] ";

    /**
     * Log messages to console with internal prefix. For internal use.
     * @param msg Messages
     * @since 0.1
     */
    @ApiStatus.Internal
    static public void logInternal(String... msg) {
        for(String s : msg) {
            Bukkit.getConsoleSender().sendMessage(HexUtil.colorize( INTERNAL_PREFIX + s));
        }
    }
}
