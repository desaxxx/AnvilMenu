package org.nandayo.anvilmenu.util;

import org.bukkit.Bukkit;

/**
 * @since 0.1
 */
public final class Wrapper {

    static private int minecraftVersion = -1;
    /**
     * Get the Minecraft version of the Server.
     * @return Integer value of the version. Format MC 1.wx.yz -> wxyz
     * @since 0.1
     */
    static public int getMinecraftVersion() {
        if(minecraftVersion != -1) return minecraftVersion;
        return minecraftVersion = fetchVersion();
    }

    /**
     * Fetch the Minecraft version and calculate integer value of it.
     * @return Integer value of the version. Format MC 1.wx.yz -> wxyz
     * @since 0.1
     */
    static private int fetchVersion() {
        String[] ver = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        if(ver.length < 2) {
            Util.logInternal("Could not fetch server version!");
            return 1605;
        }
        int major = 0;
        try {
            major = Integer.parseInt(ver[1]);
        } catch (NumberFormatException ignored) {}
        int minor = 0;
        if(ver.length > 2) {
            try {
                minor = Integer.parseInt(ver[2]);
            } catch (NumberFormatException ignored) {}
        }

        return major * 100 + minor;
    }
}
