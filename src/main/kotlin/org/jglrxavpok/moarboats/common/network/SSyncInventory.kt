package org.jglrxavpok.moarboats.common.network

import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import org.jglrxavpok.moarboats.api.BoatModuleRegistry
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity

class SSyncInventory(): IMessage {

    var boatID: Int = 0
    var moduleLocation: ResourceLocation = ResourceLocation("moarboats:none")
    val inventoryContents = mutableListOf<ItemStack>()

    constructor(boatID: Int, moduleLocation: ResourceLocation, inventoryContents: Collection<ItemStack>): this() {
        this.boatID = boatID
        this.moduleLocation = moduleLocation
        this.inventoryContents.clear()
        this.inventoryContents.addAll(inventoryContents)
    }

    override fun fromBytes(buf: ByteBuf) {
        boatID = buf.readInt()
        moduleLocation = ResourceLocation(ByteBufUtils.readUTF8String(buf))
        inventoryContents.clear()
        val tmpList = NonNullList.withSize(buf.readInt(), ItemStack.EMPTY)
        val nbt = ByteBufUtils.readTag(buf) as NBTTagCompound
        ItemStackHelper.loadAllItems(nbt, tmpList)
        inventoryContents.addAll(tmpList)
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(boatID)
        ByteBufUtils.writeUTF8String(buf, moduleLocation.toString())
        buf.writeInt(inventoryContents.size)
        val nbt = NBTTagCompound()
        val tmpList = NonNullList.from(ItemStack.EMPTY, *inventoryContents.toTypedArray())
        ItemStackHelper.saveAllItems(nbt, tmpList)
        ByteBufUtils.writeTag(buf, nbt)
    }

    object Handler: MBMessageHandler<SSyncInventory, IMessage?> {
        override val packetClass = SSyncInventory::class
        override val receiverSide = Side.CLIENT

        override fun onMessage(message: SSyncInventory, ctx: MessageContext): IMessage? {
            val world = Minecraft.getMinecraft().world
            val boat = world.getEntityByID(message.boatID) as? ModularBoatEntity ?: return null
            val moduleLocation = message.moduleLocation
            val module = BoatModuleRegistry[moduleLocation].module
            val inventory = boat.getInventory(module)
            inventory.clear()
            for(index in 0 until inventory.sizeInventory) {
                inventory.setInventorySlotContents(index, message.inventoryContents[index])
            }
            return null
        }
    }

}