package org.jglrxavpok.moarboats.common.network

import io.netty.buffer.ByteBuf
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import org.jglrxavpok.moarboats.api.BoatModuleRegistry
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity
import org.jglrxavpok.moarboats.common.modules.DispensingModule

class CChangeDispenserPeriod(): IMessage {

    var boatID: Int = 0
    var moduleLocation: ResourceLocation = ResourceLocation("moarboats:none")
    var period: Double = 0.0

    constructor(boatID: Int, moduleLocation: ResourceLocation, period: Double): this() {
        this.boatID = boatID
        this.moduleLocation = moduleLocation
        this.period = period
    }

    override fun fromBytes(buf: ByteBuf) {
        boatID = buf.readInt()
        moduleLocation = ResourceLocation(ByteBufUtils.readUTF8String(buf))
        period = buf.readDouble()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(boatID)
        ByteBufUtils.writeUTF8String(buf, moduleLocation.toString())
        buf.writeDouble(period)
    }

    object Handler: MBMessageHandler<CChangeDispenserPeriod, IMessage?> {
        override val packetClass = CChangeDispenserPeriod::class
        override val receiverSide = Side.SERVER

        override fun onMessage(message: CChangeDispenserPeriod, ctx: MessageContext): IMessage? {
            val player = ctx.serverHandler.player
            val world = player.world
            val boat = world.getEntityByID(message.boatID) as? ModularBoatEntity ?: return null
            val moduleLocation = message.moduleLocation
            val module = BoatModuleRegistry[moduleLocation].module
            module as DispensingModule
            module.changePeriod(boat, message.period)
            return null
        }
    }

}