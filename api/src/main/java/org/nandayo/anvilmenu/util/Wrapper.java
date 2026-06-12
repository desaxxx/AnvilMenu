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

        // Older Minecraft versions such as 1.21.11, 1.19
        if(ver[0].equals("1")) {
            int major = 0, minor = 0;
            try {
                major = Integer.parseInt(ver[1]);
            } catch (NumberFormatException ignored) {
            }
            if (ver.length > 2) {
                try {
                    minor = Integer.parseInt(ver[2]);
                } catch (NumberFormatException ignored) {
                }
            }

            return major * 100 + minor; //2111
        }

        // Newer Minecraft versions such as 26.1.2
        int year = 0, drop = 0, patch = 0;
        try {
            year = Integer.parseInt(ver[0]);
        } catch (NumberFormatException ignored) {}
        if(ver.length > 2) {
            try {
                drop = Integer.parseInt(ver[1]);
            } catch (NumberFormatException ignored) {}
        }
        if (ver.length > 3) {
            try {
                patch = Integer.parseInt(ver[2]);
            } catch (NumberFormatException ignored) {}
        }

        return year * 10000 + drop * 100 + patch; //260102
    }
}
