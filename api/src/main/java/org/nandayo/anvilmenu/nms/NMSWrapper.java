package org.nandayo.anvilmenu.nms;

import org.nandayo.anvilmenu.util.Wrapper;

/**
 * @since 0.1
 */
@SuppressWarnings("unused")
public final class NMSWrapper {

    static private NMSVersion nmsVersion;
    /**
     * Get the {@link NMSVersion} of the Server.
     * @return NMSVersion
     * @since 0.1
     */
    static public NMSVersion getNMSVersion() {
        if (nmsVersion != null) return nmsVersion;
        return nmsVersion = NMSVersion.findVersion(Wrapper.getMinecraftVersion());
    }

    static private AnvilWrapper anvilWrapper;
    /**
     * Get the {@link AnvilWrapper} from suitable NMS Version.
     * @return AnvilWrapper
     * @since 0.1
     */
    static public AnvilWrapper getAnvilWrapper() {
        if (anvilWrapper != null) return anvilWrapper;
        String className = NMSWrapper.class.getPackageName() + ".AnvilManager_" + getNMSVersion().name();
        try {
            return anvilWrapper = (AnvilWrapper) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create AnvilManager from class name " + className);
        }
    }
}