package org.jglrxavpok.moarboats.integration.journeymap

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import journeymap.client.io.FileHandler
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.TextComponentBase
import net.minecraft.util.text.TextComponentTranslation
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.integration.*
import java.io.File

@MoarBoatsIntegration("journeymap")
class JourneyMapPlugin(): MoarBoatsPlugin, IWaypointProvider {

    private val waypointList = mutableListOf<WaypointInfo>()
    override val name = TextComponentTranslation("waypoint_provider.journeymap")
    private val gson = Gson()

    override fun preInit() {
        WaypointProviders += this
    }

    override fun getList(): List<WaypointInfo> {
        return waypointList
    }

    override fun updateList(player: EntityPlayer) {
        waypointList.clear()
        val folder = FileHandler.getWaypointDir()
        MoarBoats.logger.debug("[journeymap Plugin] Reading waypoints from ${folder.absolutePath} - folder really exists: ${folder.exists()}")
        folder.listFiles()?.forEach { file ->
            file.bufferedReader().use {
                val waypointObject = gson.fromJson(it, JsonObject::class.java)
                val enabled = waypointObject.get("enable").asBoolean
                if(!enabled)
                    return@use
                val name = waypointObject.get("name").asString
                val x = waypointObject.get("x").asInt
                val z = waypointObject.get("z").asInt
                val dimensions = waypointObject.getAsJsonArray("dimensions").map(JsonElement::getAsInt)
                val waypoint = WaypointInfo(name, x, z, null)
                if(player.dimension in dimensions) {
                    waypointList.add(waypoint)
                }
            }
        }
    }
}