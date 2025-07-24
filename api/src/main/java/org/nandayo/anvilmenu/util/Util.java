package org.nandayo.anvilmenu.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class Util {

    static public String PREFIX = "";
    static public String INTERNAL_PREFIX = "[AnvilMenu] ";

    /**
     * Log messages to console with prefix {@link Util#PREFIX}.
     * @param msg Messages
     */
    static public void log(String... msg) {
        for(String s : msg) {
            Bukkit.getConsoleSender().sendMessage(HexUtil.colorize(PREFIX + s));
        }
    }

    /**
     * Log messages to console with dapi prefix. For internal use.
     * @param msg Messages
     */
    static public void logInternal(String... msg) {
        for(String s : msg) {
            Bukkit.getConsoleSender().sendMessage(HexUtil.colorize(PREFIX + INTERNAL_PREFIX + s));
        }
    }

    /**
     * Send a message to console or a player.
     * @param receiver Message receiver
     * @param messages Messages
     */
    static public void tell(@NotNull CommandSender receiver, @NotNull String... messages) {
        for(String m : messages) {
            receiver.sendMessage(HexUtil.colorize(Util.PREFIX + m));
        }
    }
}
