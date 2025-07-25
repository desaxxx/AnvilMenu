package org.nandayo.anvilmenu.nms;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class AnvilManager_V1_16_R3 extends AnvilWrapper {

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
        player.closeInventory(); /* Close the open menu. */

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
                player.inventory,
                ContainerAccess.at(player.world, ((CraftBlock) p.getLocation().getBlock()).getPosition()),
                title
        );
    }

    @Override
    void openMenu(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        EntityPlayer player = handle(p);
        player.activeContainer = (MenuAnvil) menu;
        sendOpenScreenPacket(p, menu, title);
        player.syncInventory(); /* SlotListener */
    }

    @Override
    void sendOpenScreenPacket(@NotNull Player p, @NotNull MenuAnvilWrapper menu, @Nullable String title) {
        EntityPlayer player = handle(p);
        MenuAnvil menuAnvil = (MenuAnvil) menu;
        player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(
                menuAnvil.windowId,
                Containers.ANVIL,
                new ChatComponentText(title)
        ));
    }


    static private class MenuAnvil extends ContainerAnvil implements MenuAnvilWrapper {


        public MenuAnvil(int containerId, PlayerInventory playerinventory, ContainerAccess containeraccess, @Nullable String title) {
            super(containerId, playerinventory, containeraccess);
            checkReachable = false;
            setTitle(new ChatComponentText(title));
            levelCost.set(0);
        }

        @Override
        public void e() { /* createResult() */
            Slot resultSlot = getSlot(2);
            if(resultSlot.getItem().isEmpty()) {
                resultSlot.set(getSlot(0).getItem().cloneItemStack());
            }
            c(); /* broadcastChanges() */
            sendLevelCost();
        }

        /**
         * Sends ContainerData packet manually.<br>
         * Used after Container#c() since it doesn't update clientside level cost.<br>
         *<br>
         * PacketPlayOutWindowData(containerId, propertyIndex, value)<br>
         * levelCost [ContainerProperty] index is usually 0.
         * @since 0.1.1
         */
        private void sendLevelCost() {
            EntityPlayer player = (EntityPlayer) this.player;
            PacketPlayOutWindowData packet = new PacketPlayOutWindowData(this.windowId, 0, 0);
            player.playerConnection.sendPacket(packet);
        }

        /*
         * a(EntityHuman, ItemStack)                -> onTake() returns ItemStack before 1_16_R3
         */
        @Override
        protected ItemStack a(EntityHuman entityHuman, ItemStack itemstack) {
            return itemstack;
        }

        @Override
        public void b(EntityHuman entityHuman) {}

        /*
         * a(EntityHuman, World, IInventory)        -> clearContainer() contains World parameter before 1_16_R3
         */
        @Override
        protected void a(EntityHuman entityhuman, World world, IInventory iinventory) {}

        @Override
        public Inventory getInventory() {
            return getBukkitView().getTopInventory();
        }
    }
}
