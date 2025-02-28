package org.jglrxavpok.moarboats.common.tileentity

import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.energy.CapabilityEnergy
import org.jglrxavpok.moarboats.common.MoarBoatsConfig
import org.jglrxavpok.moarboats.common.blocks.Facing

class TileEntityEnergyUnloader: TileEntityEnergy(), ITickable {
    override val maxReceivableEnergy = 0
    override val maxExtractableEnergy = maxEnergyStored

    private var working: Boolean = false
    val blockFacing: EnumFacing get()= world.getBlockState(pos).getValue(Facing)

    override fun update() {
        if(world.isRemote)
            return
        working = false
        updateListeners()

        val facings = EnumFacing.values().toMutableList()
        facings.remove(blockFacing)
        pushEnergyToNeighbors(MoarBoatsConfig.energyUnloader.sendAmount, facings)

        val aabb = create3x3AxisAlignedBB(pos.offset(blockFacing))
        val entities = world.getEntitiesWithinAABB(Entity::class.java, aabb) { e -> e != null && e.hasCapability(CapabilityEnergy.ENERGY, null) }

        val totalEnergyToPull = minOf(MoarBoatsConfig.energyUnloader.pullAmount, maxEnergyStored-energyStored)
        val entityCount = entities.size
        if(entityCount <= 0)
            return
        val energyToExtractFromASingleNeighbor = Math.ceil(totalEnergyToPull.toDouble()/entityCount).toInt()
        var energyActuallyReceived = 0
        entities.forEach {
            val energyCapa = it.getCapability(CapabilityEnergy.ENERGY, null)
            if(energyCapa != null) {
                energyActuallyReceived += energyCapa.extractEnergy(energyToExtractFromASingleNeighbor, false)
                working = working || energyActuallyReceived > 0
            }
        }
        energy += energyActuallyReceived
        markDirty()
    }

    override fun getRedstonePower(): Int {
        return if(working) {
            val ratio = 1.0-(energyStored.toDouble()/maxEnergyStored) // signal is strongest when the buffer is empty (transfer almost finished)
            val redstonePower = (ratio * 15).toInt()
            minOf(1, redstonePower) // give a signal of at least 1 if currently working
        } else {
            0
        }
    }

    override fun isEnergyFacing(facing: EnumFacing?): Boolean {
        return facing != blockFacing
    }

    override fun canExtract() = true

    override fun getMaxEnergyStored() = MoarBoatsConfig.energyUnloader.maxEnergy

    override fun canReceive() = false
}