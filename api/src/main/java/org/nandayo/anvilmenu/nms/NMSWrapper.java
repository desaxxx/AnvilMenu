package org.nandayo.anvilmenu.nms;

import org.nandayo.anvilmenu.util.Wrapper;

@SuppressWarnings("unused")
public class NMSWrapper {

    static private NMSVersion nmsVersion;
    static public NMSVersion getNMSVersion() {
        if (nmsVersion != null) return nmsVersion;
        return nmsVersion = NMSVersion.findVersion(Wrapper.getMinecraftVersion());
    }

    static private AnvilWrapper anvilWrapper;
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