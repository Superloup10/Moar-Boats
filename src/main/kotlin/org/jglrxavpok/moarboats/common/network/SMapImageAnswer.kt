package org.jglrxavpok.moarboats.common.network

import net.minecraft.client.Minecraft
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.network.NetworkEvent
import org.jglrxavpok.moarboats.common.data.MapImageStripe

class SMapImageAnswer(): MoarBoatsPacket {

    var mapName = ""
    var stripeIndex = 0
    var textureStripe = intArrayOf()

    constructor(name: String, stripeIndex: Int, textureStripe: IntArray): this() {
        this.mapName = name
        this.stripeIndex = stripeIndex
        this.textureStripe = textureStripe
    }

    object Handler: MBMessageHandler<SMapImageAnswer, MoarBoatsPacket> {
        override val packetClass = SMapImageAnswer::class.java
        override val receiverSide = Dist.CLIENT

        override fun onMessage(message: SMapImageAnswer, ctx: NetworkEvent.Context): MoarBoatsPacket? {
            val mapID = message.mapName
            val id = "moarboats:map_preview/$mapID/${message.stripeIndex}"
            val data = MapImageStripe(id, message.stripeIndex, message.textureStripe)
            Minecraft.getInstance().level.set(data)
            return null
        }
    }
}