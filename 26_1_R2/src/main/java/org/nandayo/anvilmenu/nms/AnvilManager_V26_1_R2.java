package org.nandayo.anvilmenu.nms;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("unused")
public class AnvilManager_V26_1_R2 extends AnvilWrapper {

    private MethodHandle paperInventoryCloseHandle;
    private Object paperInventoryCloseReasonOpenNew;
    public AnvilManager_V26_1_R2() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> reasonClass = Class.forName("org.bukkit.event.inventory.InventoryCloseEvent$Reason");
            paperInventoryCloseReasonOpenNew = reasonClass.getField("OPEN_NEW").get(null);

            // Signature: static void handleInventoryCloseEvent(EntityHuman, Reason)
            paperInventoryCloseHandle = lookup.findStatic(
                    CraftEventFactory.class,
                    "handleInventoryCloseEvent",
                    MethodType.methodType(void.class, net.minecraft.world.entity.player.Player.class, reasonClass)
            );
        } catch (ReflectiveOperationException ignored) {}
    }

    private ServerPlayer handle(@NotNull Player p) {
        return ((CraftPlayer) p).getHandle();
    }


    @Override
    public Inventory openInventory(@NotNull Player p, @NotNull String title) {
        /* Create new MenuAnvil */
        MenuAnvil menu = (MenuAnvil) createMenuAnvil(p, title);
        return openInventory(p, menu);
    }

    @Override
    public Inventory openInventory(@NotNull Player p, @NotNull MenuAnvilWrapper menuWrapper) {
        ServerPlayer player = handle(p);
        closeContainer(p);

        /* Typecast MenuAnvilWrapper to MenuAnvil */
        MenuAnvil menu = (MenuAnvil) menuWrapper;

        /* Open the MenuAnvil to the player */
        openMenu(p, menuWrapper, menu.getTitle().getString());

        return menu.getInventory();
    }

    @Override
    public MenuAnvilWrapper createMenuAnvil(@NotNull Player p, @Nullable String title) {
        ServerPlayer player = handle(p);
        return new MenuAnvil(
                player.nextContainerCounter(),
                player.getInventory(),
                ContainerLevelAccess.create(((CraftWorld) p.getWorld()).getHandle(), ((CraftBlock) p.getLocation().getBlock()).getPosition()),
                title
        );
    }

    @Override
    void openMenu(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        ServerPlayer player = handle(p);
        MenuAnvil menuAnvil = (MenuAnvil) menu;

        AbstractContainerMenu result = CraftEventFactory.callInventoryOpenEvent(player, menuAnvil);
        if(result == null) {
            // InventoryOpenEvent was canceled
            return;
        }

        player.containerMenu = menuAnvil;
        sendOpenScreenPacket(p, menu, title);
        player.initMenu(menuAnvil);
    }

    /*
     * Replicates EntityPlayer#closeContainer() with inventory close reason OPEN_NEW for Paper servers
     */
    void closeContainer(@NotNull Player p) {
        ServerPlayer player = handle(p);

        handleCloseInventoryEvent(p);
        player.connection.send(new ClientboundContainerClosePacket(player.containerMenu.containerId));
        player.doCloseContainer();
    }

    /*
     * Try calling CraftEventFactory#handleInventoryCloseEvent(EntityHuman, InventoryCloseEvent.Reason.OPEN_NEW),
     * which exists on Paper server.
     * <br>
     * Otherwise, call CraftEventFactory#handleInventoryCloseEvent(EntityHuman), which exists on Spigot
     * server only.
     */
    void handleCloseInventoryEvent(@NotNull Player p) {
        ServerPlayer player = handle(p);

        if(paperInventoryCloseHandle != null) {
            try {
                paperInventoryCloseHandle.invoke(player, paperInventoryCloseReasonOpenNew);
                return;
            } catch (Throwable ignored) {}
        }

        CraftEventFactory.handleInventoryCloseEvent(player);
    }

    @Override
    void sendOpenScreenPacket(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        ServerPlayer player = handle(p);
        MenuAnvil menuAnvil = (MenuAnvil) menu;
        player.connection.send(new ClientboundOpenScreenPacket(
                menuAnvil.containerId,
                MenuType.ANVIL,
                Component.nullToEmpty(title)
        ));
    }


    static private class MenuAnvil extends AnvilMenu implements MenuAnvilWrapper {

        public MenuAnvil(int i, net.minecraft.world.entity.player.Inventory playerinventory, ContainerLevelAccess containeraccess, @Nullable String title) {
            super(i, playerinventory, containeraccess);
            checkReachable = false;
            setTitle(Component.nullToEmpty(title));
            cost.set(0);
        }

        /*
         * What it originally does:
         *   Remove experience from player
         *   Empty input slot and subtract items from 2nd slot as repair cost
         *   Call AnvilDamage and AnvilBreak event (NMS events)
         * We override it to cancel these actions.
         */
        @Override
        protected void onTake(net.minecraft.world.entity.player.Player player, ItemStack carried) {}

        /*
         * It creates a result ItemStack based on conditions of player, items on input slots.
         * These are not needed as we only use it for getting a user input.
         */
        @Override
        public void createResult() {
            Slot resultSlot = getSlot(2);
            if(resultSlot.getItem().isEmpty()) {
                resultSlot.set(getSlot(0).getItem().copy());
            }
            broadcastChanges();
            sendAllDataToRemote();
        }

        /*
         * Makes the cursor item of player and items on input slots drop or return to player on container close caused by server.
         *
         * We override it to cancel these actions.
         */
        @Override
        public void removed(net.minecraft.world.entity.player.Player player) {}

        /*
         * Makes the items on input slots to drop or return to player.
         * This method is also called under ContainerAnvilAbstract#removed() method.
         *
         * We override it to cancel this action.
         */
        @Override
        protected void clearContainer(net.minecraft.world.entity.player.Player player, Container container) {}

        @Override
        public Inventory getInventory() {
            return getBukkitView().getTopInventory();
        }
    }
}
