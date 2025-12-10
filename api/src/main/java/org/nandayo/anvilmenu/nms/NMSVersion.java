package org.nandayo.anvilmenu.nms;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.1
 */
@SuppressWarnings("unused")
public enum NMSVersion {

    V1_16_R1(1601),
    V1_16_R2(1602,1603),
    V1_16_R3(1604,1605),
    V1_17_R1(1700),
    V1_17_1_R1(1701),
    V1_18_R1(1800,1801),
    V1_18_R2(1802),
    V1_19_0_R1(1900),
    V1_19_R1(1901,1902),
    V1_19_R2(1903),
    V1_19_R3(1904),
    V1_20_R1(2000,2001),
    V1_20_R2(2002),
    V1_20_R3(2003,2004),
    V1_20_R4(2005,2006),
    V1_21_R1(2100,2101),
    V1_21_R2(2102,2103),
    V1_21_R3(2104),
    V1_21_R4(2105),
    V1_21_R5(2106,2107,2108),
    V1_21_R6(2109,2110),
    V1_21_R7(2111)
    ;

    private final int[] minecraftVersions;
    NMSVersion(int... minecraftVersions) {
        this.minecraftVersions = minecraftVersions;
    }
    static private final NMSVersion LATEST = NMSVersion.V1_21_R7;

    /**
     * Returns the name of the version without 'V'.
     * @return String
     * @since 0.1
     */
    public String removeV() {
        return name().replace("V","");
    }


    static private final @NotNull Map<Integer, NMSVersion> VERSION_MAP = new HashMap<>();
    static {
        for (NMSVersion version : NMSVersion.values()) {
            for(int i : version.minecraftVersions) {
                VERSION_MAP.put(i, version);
            }
        }
    }

    /**
     * Find the NMS version from given Minecraft version.
     * @param minecraftVersion Minecraft version with format wxyz which correspond to {@code 1.wx.yz}
     * @return NMSVersion if found, else {@link #LATEST}.
     * @since 0.1
     */
    static public NMSVersion findVersion(int minecraftVersion) {
        return VERSION_MAP.getOrDefault(minecraftVersion, LATEST);
    }
}
