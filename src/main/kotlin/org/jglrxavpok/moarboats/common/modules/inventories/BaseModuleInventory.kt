package org.jglrxavpok.moarboats.common.modules.inventories

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import org.jglrxavpok.moarboats.api.BoatModule
import org.jglrxavpok.moarboats.api.BoatModuleInventory
import org.jglrxavpok.moarboats.api.IControllable

abstract class BaseModuleInventory(slotCount: Int, inventoryName: String, boat: IControllable, module: BoatModule):
        BoatModuleInventory(inventoryName, slotCount, boat, module, NonNullList.withSize(slotCount, ItemStack.EMPTY)) {

    protected abstract fun id2key(id: Int): String?
    abstract override fun getFieldCount(): Int

    override fun getField(id: Int): Int {
        val key = id2key(id)
        if(key != null)
            return getModuleState().getInt(key)
        return -1
    }

    override fun hasCustomName(): Boolean {
        return false
    }

    override fun setChanged() {
    }

    override fun getItem(index: Int): ItemStack {
        return list[index]
    }

    override fun decrStackSize(index: Int, count: Int) = ItemStackHelper.getAndSplit(list, index, count)

    override fun clear() {
        list.clear()
    }

    override fun getContainerSize() = list.size

    override fun getName() = StringTextComponent(inventoryName)

    override fun isEmpty(): Boolean {
        return list.all { it.isEmpty }
    }

    override fun getDisplayName(): ITextComponent {
        return TranslationTextComponent("inventory.$inventoryName.name")
    }

    override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean {
        return true
    }

    override fun getmaxStackSize() = 64

    override fun isUsableByPlayer(player: PlayerEntity): Boolean {
        return true
    }

    override fun openInventory(player: PlayerEntity?) {

    }

    override fun setField(id: Int, value: Int) {
        val key = id2key(id)
        if(key != null) {
            getModuleState().putInt(key, value)
            saveModuleState()
        }
    }

    override fun closeInventory(player: PlayerEntity?) {

    }

    override fun setItem(index: Int, stack: ItemStack) {
        list[index] = stack
    }

    override fun removeStackFromSlot(index: Int): ItemStack {
        return ItemStackHelper.getAndRemove(list, index)
    }

}