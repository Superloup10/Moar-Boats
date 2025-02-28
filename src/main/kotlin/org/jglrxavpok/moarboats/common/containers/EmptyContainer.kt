package org.jglrxavpok.moarboats.common.containers

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container

open class EmptyContainer(playerInventory: InventoryPlayer, val isLarge: Boolean = false, val xStart: Int = 8): ContainerBase(playerInventory) {

    init {
        addPlayerSlots(isLarge, xStart)
    }

    override fun canInteractWith(playerIn: EntityPlayer): Boolean {
        return true
    }
}