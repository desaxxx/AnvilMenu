package org.nandayo.anvilmenu.nms;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.block.CraftBlock;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R5.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("unused")
public class AnvilManager_V1_21_R5 extends AnvilWrapper {

    private MethodHandle paperInventoryCloseHandle;
    private Object paperInventoryCloseReasonOpenNew;
    public AnvilManager_V1_21_R5() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> reasonClass = Class.forName("org.bukkit.event.inventory.InventoryCloseEvent$Reason");
            paperInventoryCloseReasonOpenNew = reasonClass.getField("OPEN_NEW").get(null);

            // Signature: static void handleInventoryCloseEvent(EntityHuman, Reason)
            paperInventoryCloseHandle = lookup.findStatic(
                    CraftEventFactory.class,
                    "handleInventoryCloseEvent",
                    MethodType.methodType(void.class, EntityHuman.class, reasonClass)
            );
        } catch (ReflectiveOperationException ignored) {}
    }

    private EntityPlayer handle(@NotNull Player p) {
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
        EntityPlayer player = handle(p);
        closeContainer(p);

        /* Typecast MenuAnvilWrapper to MenuAnvil */
        MenuAnvil menu = (MenuAnvil) menuWrapper;

        /* Open the MenuAnvil to the player */
        openMenu(p, menuWrapper, menu.getTitle().getString());

        return menu.getInventory();
    }

    /*
     * EntityHuman#gs()                             -> EntityHuman#getInventory()
     * ContainerAccess#a(World, BlockPosition)      -> ContainerAccess#create(World, BlockPosition)
     */
    @Override
    public MenuAnvilWrapper createMenuAnvil(@NotNull Player p, @Nullable String title) {
        EntityPlayer player = handle(p);
        return new MenuAnvil(
                player.nextContainerCounter(),
                player.gs(),
                ContainerAccess.a(((CraftWorld) p.getWorld()).getHandle(), ((CraftBlock) p.getLocation().getBlock()).getPosition()),
                title
        );
    }

    /*
     * EntityHuman#cn               -> EntityHuman#containerMenu [Container]
     * EntityPlayer#a(Container)    -> EntityPlayer#initMenu(Container)
     */
    @Override
    void openMenu(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        EntityPlayer player = handle(p);
        MenuAnvil menuAnvil = (MenuAnvil) menu;

        Container result = CraftEventFactory.callInventoryOpenEvent(player, menuAnvil);
        if(result == null) {
            // InventoryOpenEvent was canceled
            return;
        }

        player.cn = menuAnvil;
        sendOpenScreenPacket(p, menu, title);
        player.a(menuAnvil);
    }

    /*
     * EntityPlayer#g                               -> EntityPlayer#connection [PlayerConnection]
     * ServerCommonPacketListenerImpl#b(Packet<?>)  -> ServerCommonPacketListenerImpl#send(Packet<?>)
     * EntityPlayer#q                               -> EntityPlayer#doCloseContainer()
     *
     * Replicates EntityPlayer#closeContainer() with inventory close reason OPEN_NEW for Paper servers
     */
    void closeContainer(@NotNull Player p) {
        EntityPlayer player = handle(p);

        handleCloseInventoryEvent(p);
        player.g.b(new PacketPlayOutCloseWindow(player.cn.l));
        player.q();
    }

    /*
     * Try calling CraftEventFactory#handleInventoryCloseEvent(EntityHuman, InventoryCloseEvent.Reason.OPEN_NEW),
     * which exists on Paper server.
     * <br>
     * Otherwise, call CraftEventFactory#handleInventoryCloseEvent(EntityHuman), which exists on Spigot
     * server only.
     */
    void handleCloseInventoryEvent(@NotNull Player p) {
        EntityPlayer player = handle(p);

        if(paperInventoryCloseHandle != null) {
            try {
                paperInventoryCloseHandle.invoke(player, paperInventoryCloseReasonOpenNew);
                return;
            } catch (Throwable ignored) {}
        }

        CraftEventFactory.handleInventoryCloseEvent(player);
    }

    /*
     * EntityPlayer#g                               -> EntityPlayer#connection [PlayerConnection]
     * ServerCommonPacketListenerImpl#b(Packet<?>)  -> ServerCommonPacketListenerImpl#send(Packet<?>)
     * Container#l                                  -> Container#containerId [Integer]
     * Containers#i                                 -> Containers#ANVIL [Containers<ContainerAnvil>]
     * IChatBaseComponent#a(String)                 -> IChatBaseComponent#nullToEmpty(String)
     */
    @Override
    void sendOpenScreenPacket(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        EntityPlayer player = handle(p);
        MenuAnvil menuAnvil = (MenuAnvil) menu;
        player.g.b(new PacketPlayOutOpenWindow(
                menuAnvil.l,
                Containers.i,
                IChatBaseComponent.a(title)
        ));
    }


    static private class MenuAnvil extends ContainerAnvil implements MenuAnvilWrapper {

        /*
         * y                                -> cost [ContainerProperty]
         * ContainerProperty#a(Integer)     -> ContainerProperty#set(Integer)
         * IChatBaseComponent#a(String)     -> IChatBaseComponent#nullToEmpty(String)
         *
         */
        public MenuAnvil(int i, PlayerInventory playerinventory, ContainerAccess containeraccess, @Nullable String title) {
            super(i, playerinventory, containeraccess);
            checkReachable = false;
            setTitle(IChatBaseComponent.a(title));
            y.a(0);
        }

        /*
         * a(EntityHuman, ItemStack) -> onTake(EntityHuman, ItemStack)
         *
         * What it originally does:
         *   Remove experience from player
         *   Empty input slot and subtract items from 2nd slot as repair cost
         *   Call AnvilDamage and AnvilBreak event (NMS events)
         * We override it to cancel these actions.
         */
        @Override
        protected void a(EntityHuman entityHuman, ItemStack itemStack) {}

        /*
         * l()              -> createResult()
         *
         * It creates a result ItemStack based on conditions of player, items on input slots.
         * These are not needed as we only use it for getting a user input.
         *
         * b(Integer)       -> getSlot(Integer)
         * Slot#g()         -> Slot#getItem()
         * Slot#f()         -> Slot#set()
         * ItemStack#f()    -> ItemStack#isEmpty()
         * ItemStack#v()    -> ItemStack#copy()
         * d()              -> broadcastChanges()
         * b()              -> sendAllDataToRemote()
         */
        @Override
        public void l() {
            Slot resultSlot = b(2);
            if(resultSlot.g().f()) {
                resultSlot.f(b(0).g().v());
            }
            d();
            b();
        }

        /*
         * a(EntityHuman)            -> ContainerAnvilAbstract#removed(EntityHuman)
         *
         * Makes the cursor item of player and items on input slots drop or return to player on container close caused by server.
         *
         * We override it to cancel these actions.
         */
        @Override
        public void a(EntityHuman entityHuman) {}

        /*
         * a(EntityHuman, IInventory)   -> Container#clearInventory(EntityHuman, IInventory)
         *
         * Makes the items on input slots to drop or return to player.
         * This method is also called under ContainerAnvilAbstract#removed() method.
         *
         * We override it to cancel this action.
         */
        @Override
        protected void a(EntityHuman entityhuman, IInventory iinventory) {}

        @Override
        public Inventory getInventory() {
            return getBukkitView().getTopInventory();
        }
    }
}
