package org.nandayo.anvilmenu.util;

import org.bukkit.Bukkit;

public class Wrapper {

    static private int minecraftVersion = -1;

    static public int getMinecraftVersion() {
        if(minecraftVersion != -1) return minecraftVersion;
        return minecraftVersion = fetchVersion();
    }

    static private int fetchVersion() {
        String[] ver = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        if(ver.length < 2) {
            Util.logInternal("Could not fetch server version!");
            return 165;
        }
        int major = 0;
        try {
            major = Integer.parseInt(ver[1]);
        } catch (NumberFormatException ignored) {}
        int minor = 0;
        if(ver.length >= 3) {
            try {
                minor = Integer.parseInt(ver[2]);
            } catch (NumberFormatException ignored) {}
        }

        return major * 10 + minor;
    }
}
