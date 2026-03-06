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
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("unused")
public class AnvilManager_V1_21_R1 extends AnvilWrapper {

    private MethodHandle paperInventoryCloseHandle;
    private Object paperInventoryCloseReasonOpenNew;
    public AnvilManager_V1_21_R1() {
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

        MenuAnvil menu = (MenuAnvil) menuWrapper;

        /* Open the MenuAnvil to the player */
        openMenu(p, menu, menu.getTitle().getString());

        return menu.getInventory();
    }

    @Override
    public MenuAnvilWrapper createMenuAnvil(@NotNull Player p, @Nullable String title) {
        EntityPlayer player = handle(p);
        return new MenuAnvil(
                player.nextContainerCounter(),
                player.fY(), /* PlayerInventory */
                ContainerAccess.a(((CraftWorld) p.getWorld()).getHandle(), ((CraftBlock) p.getLocation().getBlock()).getPosition()),
                title
        );
    }

    @Override
    void openMenu(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        EntityPlayer player = handle(p);
        MenuAnvil menuAnvil = (MenuAnvil) menu;

        Container result = CraftEventFactory.callInventoryOpenEvent(player, menuAnvil);
        if(result == null) {
            // InventoryOpenEvent was canceled
            return;
        }

        player.cd = menuAnvil;
        sendOpenScreenPacket(p, menu, title);
        player.a(menuAnvil); /* SlotListener */
    }

    void closeContainer(@NotNull Player p) {
        EntityPlayer player = handle(p);

        handleCloseInventoryEvent(p);
        player.c.b(new PacketPlayOutCloseWindow(player.cd.j));
        player.t();
    }

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

    @Override
    void sendOpenScreenPacket(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        EntityPlayer player = handle(p);
        MenuAnvil menuAnvil = (MenuAnvil) menu;
        player.c.b(new PacketPlayOutOpenWindow(
                menuAnvil.j,
                Containers.i,
                title == null ? null : IChatBaseComponent.a(title)
        ));
    }


    static private class MenuAnvil extends ContainerAnvil implements MenuAnvilWrapper {


        public MenuAnvil(int containerId, PlayerInventory playerinventory, ContainerAccess containeraccess, @Nullable String title) {
            super(containerId, playerinventory, containeraccess);
            checkReachable = false;
            setTitle(IChatBaseComponent.a(title));
            w.a(0); /* cost.set() */
        }

        @Override
        public void m() { /* createResult() */
            Slot resultSlot = b(0); /* getSlot() */
            if(resultSlot.g().e()) { /* getItem(), isEmpty() */
                resultSlot.f(b(0).g().s()); /* set(), getSlot(i).getItem().cloneItemStack() */
            }
            d(); /* broadcastChanges() */
            b(); /* sendAllDataToRemote() */
        }

        @Override
        protected void a(EntityHuman entityHuman, ItemStack itemstack) {}

        @Override
        public void a(EntityHuman entityHuman) {}

        @Override
        protected void a(EntityHuman entityHuman, IInventory iinventory) {}

        @Override
        public Inventory getInventory() {
            return getBukkitView().getTopInventory();
        }
    }
}
