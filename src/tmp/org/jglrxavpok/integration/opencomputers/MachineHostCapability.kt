package org.jglrxavpok.moarboats.integration.opencomputers

import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity

class MachineHostCapability(val boat: ModularBoatEntity) : ICapabilityProvider {

    var host = BoatMachineHost(boat)
        private set

    override fun <T : Any?> getCapability(capability: Capability<T>, facing: Direction?): T? {
        if(capability == OpenComputersPlugin.HostCapability) {
            return this as T?
        }
        return null
    }

    override fun hasCapability(capability: Capability<*>, facing: Direction?): Boolean {
        if(capability == OpenComputersPlugin.HostCapability) {
            return true
        }
        return false
    }

    fun resetHost() {
        host = BoatMachineHost(boat)
    }

    object Storage: Capability.IStorage<MachineHostCapability> {
        override fun readNBT(capability: Capability<MachineHostCapability>?, instance: MachineHostCapability, side: Direction?, nbt: NBTBase) {
            instance.host.load(nbt as CompoundNBT)
        }

        override fun writeNBT(capability: Capability<MachineHostCapability>?, instance: MachineHostCapability, side: Direction?): NBTBase? {
            return instance.host.saveToNBT(CompoundNBT())
        }
    }

}