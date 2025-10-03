package org.nandayo.anvilmenu.nms;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.1
 */
@SuppressWarnings("unused")
public enum NMSVersion {

    V1_16_R1(161),
    V1_16_R2(162,163),
    V1_16_R3(164,165),
    V1_17_R1(170),
    V1_17_1_R1(171),
    V1_18_R1(180,181),
    V1_18_R2(182),
    V1_19_0_R1(190),
    V1_19_R1(191,192),
    V1_19_R2(193),
    V1_19_R3(194),
    V1_20_R1(200,201),
    V1_20_R2(202),
    V1_20_R3(203,204),
    V1_20_R4(205,206),
    V1_21_R1(210,211),
    V1_21_R2(212,213),
    V1_21_R3(214),
    V1_21_R4(215),
    V1_21_R5(216,217,218),
    V1_21_R6(219)
    ;

    private final int[] minecraftVersions;
    NMSVersion(int... minecraftVersions) {
        this.minecraftVersions = minecraftVersions;
    }
    static private final NMSVersion LATEST = NMSVersion.V1_21_R6;

    /**
     * Removes the name of the version without 'V'.
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
     * @param minecraftVersion Minecraft version
     * @return NMSVersion if found, else {@link #LATEST}.
     * @since 0.1
     */
    static public NMSVersion findVersion(int minecraftVersion) {
        return VERSION_MAP.getOrDefault(minecraftVersion, LATEST);
    }
}
