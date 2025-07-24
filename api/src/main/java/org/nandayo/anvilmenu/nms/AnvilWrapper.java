package org.nandayo.anvilmenu.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AnvilWrapper {

    /**
     * Open an anvil menu to the player.
     * @param p BukkitPlayer
     * @param title Title of the menu
     * @return InventoryView
     */
    abstract public Inventory openInventory(@NotNull Player p, @NotNull String title);

    /**
     * Open an anvil menu to the player from given MenuAnvilWrapper.
     * @param p BukkitPlayer
     * @param menu AnvilMenuWrapper
     * @return InventoryView
     */
    abstract public Inventory openInventory(@NotNull Player p, @NotNull MenuAnvilWrapper menu);

    /**
     * Create a MenuAnvilWrapper.
     * @param p BukkitPlayer
     * @param title Title of the menu
     * @return new MenuAnvilWrapper
     */
    abstract public MenuAnvilWrapper createMenuAnvil(@NotNull Player p, @Nullable String title);

    /**
     * Copy of necessary part of method ServerPlayer#openMenu(MenuProvider).
     * @param p BukkitPlayer
     * @param menu MenuAnvilWrapper to open to the player
     * @param title Title of the menu
     */
    abstract void openMenu(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title);

    /**
     * send OpenScreenPacket to the player
     * @param p BukkitPlayer
     * @param menu MenuAnvilWrapper to open to the player
     * @param title Title of the menu
     */
    abstract void sendOpenScreenPacket(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title);


    public interface MenuAnvilWrapper {

        /**
         * Get the inventory of the AnvilMenu.
         * Switched from inventory view to inventory since InventoryView was an abstract class before 1.19.4,
         * and it was going to be a hassle to use reflection and all.
         * @return Inventory
         */
        Inventory getInventory();
    }
}
