package org.jglrxavpok.moarboats.common.network

import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.common.data.LoopingOptions
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity
import org.jglrxavpok.moarboats.common.items.ItemGoldenTicket
import org.jglrxavpok.moarboats.common.items.ItemMapWithPath
import org.jglrxavpok.moarboats.common.items.ItemPath
import org.jglrxavpok.moarboats.common.modules.HelmModule

class CChangeLoopingStateItemPathBoat: CChangeLoopingStateBase {

    constructor(): super()

    var boatID: Int = -1

    constructor(loopingOptions: LoopingOptions, boatID: Int): super(loopingOptions) {
        this.boatID = boatID
    }

    override fun fromBytes(buf: ByteBuf) {
        super.fromBytes(buf)
        boatID = buf.readInt()
    }

    override fun toBytes(buf: ByteBuf) {
        super.toBytes(buf)
        buf.writeInt(boatID)
    }

    object Handler: MBMessageHandler<CChangeLoopingStateItemPathBoat, IMessage?> {
        override val packetClass = CChangeLoopingStateItemPathBoat::class
        override val receiverSide = Side.SERVER

        override fun onMessage(message: CChangeLoopingStateItemPathBoat, ctx: MessageContext): IMessage? {
            with(message) {
                val player = ctx.serverHandler.player
                val world = player.world
                val boat = world.getEntityByID(message.boatID) as? ModularBoatEntity ?: return null
                val stack = boat.getInventory(HelmModule).getStackInSlot(0)
                val item = stack.item
                if(item is ItemPath) {
                    item.setLoopingOptions(stack, message.loopingOption)
                    when(item) {
                        is ItemGoldenTicket -> return SSetGoldenItinerary(item.getData(stack))
                        is ItemMapWithPath -> return SUpdateMapWithPathInBoat(item.getWaypointData(stack, MoarBoats.getLocalMapStorage()), boatID)
                    }
                }
                return null
            }
        }

    }
}