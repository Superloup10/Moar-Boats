package org.jglrxavpok.moarboats.api

import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.jglrxavpok.moarboats.common.entities.BasicBoatEntity
import org.jglrxavpok.moarboats.extensions.toRadians
import java.util.*

interface IControllable {

    val entityID: Int
    val modules: List<BoatModule>
    val worldRef: World
    val positionX: Double
    val positionY: Double
    val positionZ: Double
    val velocityX: Double
    val velocityY: Double
    val velocityZ: Double
    val yaw: Float
    val correspondingEntity: Entity
    val moduleRNG: Random

    fun inWater(): Boolean

    fun turnRight(multiplier: Float = 1f)

    fun turnLeft(multiplier: Float = 1f)
    fun accelerate(multiplier: Float = 1f)
    fun decelerate(multiplier: Float = 1f)
    fun blockMovement()

    fun saveState(module: BoatModule)
    fun getState(module: BoatModule): NBTTagCompound
    fun getInventory(module: BoatModule): IBoatModuleInventory

    fun calculateAnchorPosition(linkType: Int): Vec3d {
        val distanceFromCenter = 0.0625f * 17f * if(linkType == BasicBoatEntity.FrontLink) 1f else -1f
        val anchorX = positionX + MathHelper.cos((yaw + 90f).toRadians()) * distanceFromCenter
        val anchorY = positionY + 0.0625f * 16f
        val anchorZ = positionZ + MathHelper.sin((yaw + 90f).toRadians()) * distanceFromCenter
        return Vec3d(anchorX, anchorY, anchorZ)
    }
}