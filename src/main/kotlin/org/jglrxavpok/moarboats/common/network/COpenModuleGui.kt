package org.jglrxavpok.moarboats.common.network

import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.network.NetworkEvent
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.common.MoarBoatsGuiHandler
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity

class COpenModuleGui(): MoarBoatsPacket {
    var boatID: Int = 0
    lateinit var moduleID: ResourceLocation

    constructor(boatID: Int, id: ResourceLocation): this() {
        this.boatID = boatID
        this.moduleID = id
    }

    object Handler: MBMessageHandler<COpenModuleGui, MoarBoatsPacket> {
        override val packetClass = COpenModuleGui::class.java
        override val receiverSide = Dist.DEDICATED_SERVER

        override fun onMessage(message: COpenModuleGui, ctx: NetworkEvent.Context): MoarBoatsPacket? {
            val player = ctx.sender!!
            val boat = player.world.getEntityByID(message.boatID) as? ModularBoatEntity
            if(boat == null) {
                MoarBoats.logger.debug("$player tried to open boat menu while the boat with ID ${message.boatID} is not loaded (or doesn't exist?)")
                return null
            }
            val moduleIndex = boat.modules.indexOfFirst { it.id == message.moduleID }
            player.displayGui(MoarBoats, MoarBoatsGuiHandler.ModulesGui, player.world, message.boatID, moduleIndex, 0)
            return null
        }
    }


}