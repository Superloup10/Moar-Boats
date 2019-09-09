package org.jglrxavpok.moarboats.common.containers

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.inventory.*
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import org.jglrxavpok.moarboats.api.BoatModule
import org.jglrxavpok.moarboats.api.IControllable
import org.jglrxavpok.moarboats.common.modules.FurnaceEngineModule

class ContainerFurnaceEngine(playerInventory: PlayerInventory, val engine: BoatModule, val boat: IControllable): ContainerBase(playerInventory) {

    val engineInventory = boat.getInventory(engine)
    private var fuelTime = engineInventory.getField(0)
    private var fuelTotalTime = engineInventory.getField(1)

    init {
        this.addSlot(SlotEngineFuel(engineInventory, 0, 8, 8))

        addPlayerSlots(isLarge = true)
    }

    override fun addListener(listener: IContainerListener) {
        super.addListener(listener)
        listener.sendAllWindowProperties(this, engineInventory)
    }

    override fun broadcastChanges() {
        super.broadcastChanges()

        for(listener in listeners) {
            if (this.fuelTotalTime != this.engineInventory.getField(0)) {
                listener.sendWindowProperty(this, 0, this.engineInventory.getField(0))
            }

            if (this.fuelTime != this.engineInventory.getField(1)) {
                listener.sendWindowProperty(this, 1, this.engineInventory.getField(1))
            }
        }

        this.fuelTime = this.engineInventory.getField(0)
        this.fuelTotalTime = this.engineInventory.getField(1)
    }

    @OnlyIn(Dist.CLIENT)
    override fun updateProgressBar(id: Int, data: Int) {
        this.engineInventory.setField(id, data)
    }

    override fun transferStackInSlot(playerIn: PlayerEntity, index: Int): ItemStack {
        var itemstack = ItemStack.EMPTY
        val slot = this.inventorySlots[index]

        if (slot != null && slot.hasStack) {
            val itemstack1 = slot.item
            itemstack = itemstack1.copy()

            if (index != 0) {
                if (TileEntityFurnace.isItemFuel(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (index in 1..28) {
                    if (!this.mergeItemStack(itemstack1, 28, 37, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (index in 28..36 && !this.mergeItemStack(itemstack1, 1, 27, false)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.mergeItemStack(itemstack1, 1, 37, false)) {
                return ItemStack.EMPTY
            }

            if (itemstack1.isEmpty) {
                slot.putStack(ItemStack.EMPTY)
            } else {
                slot.onSlotChanged()
            }

            if (itemstack1.count == itemstack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(playerIn, itemstack1)
        }

        return itemstack
    }


    class SlotEngineFuel(inventoryIn: IInventory, slotIndex: Int, xPosition: Int, yPosition: Int) : Slot(inventoryIn, slotIndex, xPosition, yPosition) {

        override fun isItemValid(stack: ItemStack): Boolean {
            return FurnaceEngineModule.isItemFuel(stack) || isBucket(stack)
        }

        override fun getItemStackLimit(stack: ItemStack): Int {
            return if (isBucket(stack)) 1 else super.getItemStackLimit(stack)
        }

        fun isBucket(stack: ItemStack): Boolean {
            return stack.item === Items.BUCKET
        }
    }
}