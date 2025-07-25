package org.nandayo.anvilmenu.util;

import org.bukkit.Bukkit;

@SuppressWarnings("unused")
public final class Util {

    static private final String INTERNAL_PREFIX = "[AnvilMenu] ";

    /**
     * Log messages to console with dapi prefix. For internal use.
     * @param msg Messages
     */
    static public void logInternal(String... msg) {
        for(String s : msg) {
            Bukkit.getConsoleSender().sendMessage(HexUtil.colorize( INTERNAL_PREFIX + s));
        }
    }
}
